(ns skrio.core
  (:use compojure.core
        skrio.middleware
        skrio.helpers
        skrio.extractor
        skrio.conversion
        skrio.responses
        [environ.core :only (env)]
        [ring.util.response :only (status response content-type header)])
  (:import  [org.bson.types ObjectId])
  (:require [skrio.pages :as pages]
            [clojure.string :as cstring]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [ring.middleware.json :as ring-json]
            [ring.adapter.jetty :as jetty]
            [monger.core :as mg]
            [monger.collection :as mc]))


(mg/connect-via-uri! (env :mongodb-url))



(def config {:token-length 64 :max-text-size 32768})
(def errors
  {:text-length-error (str "Text length must be " (:max-text-size config) " or less.")
   :other "Error."})

(def str-_id (comp str :_id))
(defn auth
  [resp]
  (header resp "WWW-Authenticate" "Basic realm=\"texts\""))


;;;;;;;;;;;;;;;
;; Texts API ;;
;;;;;;;;;;;;;;;


(defn- get-texts
  [req]
  (response
   {:texts (let [texts (mc/find-maps "texts" {:user (get-in req [:user :id])})]
             (-> texts remove-users prepare-ids))}))

(defn- create-text
  [req]
  (let [user (:user req)
        oid  (ObjectId.)
        wtf "why\nnot"]
    (if (not user)
      (auth (respond-json-401))
      (try
        (let [{text :text content-type :content-type} (extract-text req)]
          (if (> (count text) (:max-text-size config))
            (respond-json-413 (:text-length-error errors))
            (do
              #_(mc/insert "texts" {:_id oid :user (:id user) :public false
                                  :text text :content-type content-type})
              (mc/insert "texts" {:_id oid :text text :user (:id user)
                                  :public false :content-type content-type})
              (respond 201 (str-_id (mc/find-map-by-id "texts" oid))))))
        (catch Exception e (respond-json-400 (str (.getMessage e))))))))


(defn- extract-id [s] (zipmap [:id :to] (cstring/split s #"\.")))
(defn- get-text
  [text-id req]
  (try
    (let [{text-id :id to :to :or {to "txt"}} (extract-id text-id)
          text-o (mc/find-map-by-id "texts" (ObjectId. text-id))
          user (:user req)]
      (cond
       (not text-o)                    (respond-json-404)
       (not (can-access? user text-o)) (auth (respond-json-401))
       :else 
       (try
         (let [{text :text type :content-type} (convert text-o (keyword to))]
           (content-type (response text) type))
         (catch Exception e (respond-json-400)))))))

(defn- get-public-text
  [text-id req]
  (try
    (let [{text-id :id to :to :or {to "txt"}} (extract-id text-id)
          text (mc/find-one-as-map "texts" {:_id (ObjectId. text-id) :public true})
          {text :text type :content-type} (convert text (keyword to))]
      (if text
        (content-type (response text) type)
        (respond-json-404)))
    (catch Exception e (respond-json-404))))

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
      (mc/update "texts"
                 {:_id (ObjectId. text-id) :user (:id user)}
                 {"$set" {:public true}}))
    (catch Exception e))
  (response text-id))

(defn- make-text-private
  [text-id req]
  (try
    (if-let [user (:user req)]
      (mc/update "texts"
                 {:_id (ObjectId. text-id) :user (:id user)}
                 {"$set" {:public false}}))
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
  [{{:keys [email password]} :params} req]
  (cond (< (count password) 8) (respond-json-400 "password must be 8+ chars")
        (not (re-matches #".+@.+\..+" email)) (respond-json-400 "invalid email")
        :else (let [oid (ObjectId.)
                    [tok sec csrf] (repeatedly (gen-secret (:token-length config)))]
                (try
                  (mc/insert "users" {:_id oid :csrf-token csrf
                                      :api-token tok :api-secret sec
                                      :email email :password (encrypt password)})
                  (respond 201 (str oid))
                  (catch Exception e (respond-json-500))))))

(defn- get-user
  [user-id req]
  (if (and (:user req) (= (str (:id (:user req))) user-id))
    (try
      (response (dissoc (prepare-id (mc/find-map-by-id "users" (ObjectId. user-id)))
                        :password :csrf-token))
      (catch Exception e (respond-json-404)))
    (respond-json-404)))

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
  (context  "/api/v.1/user" []
    (POST   "/" [] create-user))
  (context  "/api/v.1/user/:user-id" [user-id]
    (GET    "/" [] (partial get-user user-id)))) 

(defroutes text-api
  (context  "/api/v.1/text" []
    (GET    "/"        [] get-texts)
    (POST   "/"        [] create-text))
  (context  "/api/v.1/text/:text-id" [text-id]
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
  (route/not-found (respond-json-404)))

(def app (-> skrio-routes
             (make-wrap-user #(mc/find-one-as-map "users" {:api-token %}))
             wrap-basic-auth
             logger/wrap-with-logger
             ring-json/wrap-json-response
             wrap-utf-8
             handler/site))


;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Main ;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
