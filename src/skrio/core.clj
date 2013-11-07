(ns skrio.core
  (:use compojure.core
        environ.core
        ring.middleware.json-response
        ring.util.response)
  (:import [org.bson.types ObjectId])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.set :as set]))


(mg/connect-via-uri! (env :mongodb-url))


(def uid "MOCK USER ID")


;; Helpers

(defn remove-users
  [coll]
  (map #(dissoc % :user :public) coll))
(defn prepare-ids
  [coll] 
  (map  #(-> % (set/rename-keys {:_id :id}) (update-in [:id] str)) coll))



;; Handlers

(defn- get-texts
  [req]
  (response 
   {:texts (let [texts (mc/find-maps "texts" {:user uid})]
             (-> texts remove-users prepare-ids))}))


(defn- create-text
  [req]
  (let [oid (ObjectId.)
        text (:text (:params req))]
    (if (<= (count text) 32768)
      (mc/insert "texts" {:_id oid :user uid :public false :text text}))
    (try
      (status (response (str (:_id (mc/find-map-by-id "texts" oid)))) 201)
      (catch Exception e (status (response "Message Too Long") 400)))))


(defn- get-text
  [text-id req]
  (response
   (try
     (:text (mc/find-map-by-id "texts" (ObjectId. text-id)))
     (catch Exception e ""))))


(defn- update-text
  [text-id req]
  (response
   (do
     (try
       (mc/update-by-id "texts" (ObjectId. text-id) {:text (:text (:params req))})
       (catch Exception e))
     text-id)))


(defn- delete-text
  [text-id req]
  (response
   (do (try
         (mc/remove-by-id "texts" (ObjectId. text-id))
         (catch Exception e))
       text-id)))



;; Routes
(defroutes web
  (GET "/" [] (constantly
               (response "Welcome to Skr.io. We handle your text.\n\nMagically."))))

(defroutes text-api
  (context "/api/v.1/text" []
    (GET  "/" [] get-texts)
    (POST "/" [] create-text))
  (context "/api/v.1/text/:text-id" [text-id]
    (GET    "/" [] (partial get-text text-id))
    (PUT    "/" [] (partial update-text text-id))
    (DELETE "/" [] (partial delete-text text-id))))


(defroutes skrio-routes
  web
  text-api
  (route/not-found "Not Found"))


(def app (logger/wrap-with-logger (wrap-json-response (handler/site skrio-routes))))
