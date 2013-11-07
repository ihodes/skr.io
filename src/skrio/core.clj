(ns skrio.core
  (:use compojure.core
        skrio.middleware
        skrio.helpers
        skrio.post-processor
        [environ.core :only (env)]
        [ring.middleware.json-response :only (wrap-json-response)]
        [ring.util.response :only (status response content-type)])
  (:import  [org.bson.types ObjectId])
  (:require [skrio.pages :as pages]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [ring.adapter.jetty :as jetty]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.data.json :as json]))


(mg/connect-via-uri! (env :mongodb-url))

(def config {:token-length 64 :max-text-size 32768})



;;;;;;;;;;;;;;;
;; Texts API ;;
;;;;;;;;;;;;;;;

(defn- get-texts
  [req]
  (response 
   {:texts (let [user  (:user req)
                 texts (mc/find-maps "texts" {:user (:id user)})]
             (-> texts remove-users prepare-ids))}))

(defn- create-text
  [req]
  (let [user (:user req)
        oid (ObjectId.)
        text (:text (:params req))
        public (= "true" (:public (:params req)))]
    (if (and user (<= (count text) (:max-text-size config)))
      (do
        (mc/insert "texts" {:_id oid :user (:id user) :public public :text text})
        (try
          (status (response (str (:_id (mc/find-map-by-id "texts" oid)))) 201)
          (catch Exception e (status (response "Message Too Long") 400))))
      access-denied)))

(defn- get-text
  [text-id req]
  (try 
    (let [text (mc/find-map-by-id "texts" (ObjectId. text-id))
          user (:user req)]
      (if (can-access? user text)
        (response (:text text))
        (response (str req))))
    (catch Exception e not-found)))

(defn- get-public-text
  [text-id req]
  (try
    (let [text (mc/find-one-as-map "texts" {:_id (ObjectId. text-id) :public true})]
      (if text
        (response (:text text))
        not-found))
    (catch Exception e not-found)))

(defn- update-text
  [text-id req]
  (try
    (if-let [user (:user req)]
      (mc/update "texts" {:_id (ObjectId. text-id) :user (:id user)}
                 {"$set" {:text (:text (:params req))}}))
    (catch Exception e))
  (response text-id))

(defn- make-text-public
  [text-id req]
  (try
    (if-let [user (:user req)]
      (mc/update "texts" {:_id (ObjectId. text-id) :user (:id user)} {"$set" {:public true}}))
    (catch Exception e))
  (response text-id))

(defn- make-text-private
  [text-id req]
  (try
    (if-let [user (:user req)]
      (mc/update "texts" {:_id (ObjectId. text-id) :user (:id user)} {"$set" {:public false}}))
    (catch Exception e))
  (response text-id))

(defn- delete-text
  [text-id req]
  (try
    (if-let [user (:user req)]
      (mc/remove "texts" {:_id (ObjectId. text-id) :user (:id user)}))
    (catch Exception e))
  (response text-id))


;;;;;;;;;;;;;;
;; User API ;;
;;;;;;;;;;;;;;

(defn- create-user
  [req]
  (let [params     (:params req)
        email      (:email params)
        password   (:password params)]
    (cond (< (count password) 8) (status (response "password must be 8+ chars") 400)
          (not (re-matches #".+@.+\..+" email)) (status (response "invalid email") 400)
          :else (let [oid        (ObjectId.)
                      api-token  (gen-secret (:token-length config))
                      api-secret (gen-secret (:token-length config))
                      csrf-token (gen-secret (:token-length config))]
                  (mc/insert "users" {:_id oid :csrf-token csrf-token
                                      :api-token api-token :api-secret api-secret
                                      :email email :password (encrypt password)})
                  (try
                    (status (response (str (:_id (mc/find-map-by-id "users" oid)))) 201)
                    (catch Exception e (status (response "We're really sorry! Something is broken.")
                                               500)))))))

(defn- get-user
  [user-id req]
  (if (and (:user req) (= (str (:id (:user req))) user-id))
    (try
      (response (dissoc (prepare-id (mc/find-map-by-id "users" (ObjectId. user-id)))
                        :password :csrf-token))
      (catch Exception e (status (response "Not Found") 404)))
    (status (response "Not Found") 404)))

(defn- delete-user ;; not hooked up
  [user-id req]
  (response
   (do (try
         (mc/remove-by-id "users" (ObjectId. user-id))
         (catch Exception e))
       "User removed.")))


;;;;;;;;;;;;
;; Routes ;;
;;;;;;;;;;;;

(defroutes web
  (GET "/" [] pages/index)
  (ANY "/ping" [] (comp response str)) ;; TK disable in production
  (route/files "/public"))


(defroutes user-api
  (context "/api/v.1/user" []
    (POST "/" [] create-user))
  (context "/api/v.1/user/:user-id" [user-id]
    (GET    "/" [] (partial get-user user-id)))) 

(defroutes text-api
  (context "/api/v.1/text" []
    (GET  "/" [] get-texts)
    (POST "/" [] create-text))
  (context "/api/v.1/text/:text-id" [text-id]
    (GET    "/"        [] (partial get-text text-id))
    (PUT    "/"        [] (partial update-text text-id))
    (POST   "/private" [] (partial make-text-private text-id))
    (POST   "/public"  [] (partial make-text-public text-id))
    (DELETE "/"        [] (partial delete-text text-id))))

(defroutes public-texts
  (GET "/text/:text-id" [text-id] (partial get-public-text text-id)))

(defroutes skrio-routes
  text-api
  user-api
  public-texts
  web
  (route/not-found "Not Found"))

(def app (-> skrio-routes
             (make-wrap-user #(mc/find-one-as-map "users" {:api-token %}))
             wrap-basic-auth
             logger/wrap-with-logger
             wrap-json-response
             handler/site))


;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Main ;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
