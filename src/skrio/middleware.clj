(ns skrio.middleware
  (:use skrio.helpers
        [clojure.string :only (split)]
        [clojure.set :only (rename-keys)]
        [ring.util.response :only (charset)])
  (:require [ring.util.codec :as codec]
            [clojure.data.json :as json]
            [cheshire.core :as ch]))

(defn make-wrap-user
  [handler lookup]
  (fn [request]
    (let [api-token  (if-let [auth (:auth request)] (:user auth))
          api-secret (if-let [auth (:auth request)] (:pass auth))
          user (lookup api-token)]
      (if (and user (= (:api-secret user) api-secret))
        (handler (assoc request :user (rename-keys user {:_id :id})))
        (handler (assoc request :user nil))))))

(defn wrap-basic-auth
  [handler]
  (fn [request]
    (let [headers     (:headers request)
          auth-header (get headers "authorization")
          auth-text   (when auth-header (base64-str->str (nth (split auth-header #" ") 1)))
          auth-map    (when auth-text (into {} (map vector [:user :pass]
                                                    (split auth-text #":"))))]
      (handler (assoc request :auth auth-map)))))

(defn wrap-utf-8
  [handler]
  (fn [request]
    (let [response (handler request)]
      (charset response "utf-8"))))

(defn wrap-append-newline
  [handler]
  (fn [request]
    (let [response (handler request)
          body (:body response)]
      (if (instance? String body)
        (update-in response [:body] #(str % \newline))
        response))))

(defn wrap-json-response
  [handler]  
  (fn [request]
    (let [response (handler request)
          body (:body response)]
      (if (instance? clojure.lang.PersistentArrayMap body)
        (update-in response [:body] #(ch/generate-string % {:pretty true}))
        response))))

(defn wrap-string-body
  [handler]
  (fn [request]
    (handler (update-in request [:body] slurp))))

(defn wrap-body-json-request
  [handler]
  (fn [request]
    (try (let [json-map (json/read-str (:body request))]
           (handler (merge-with merge request {:json json-map})))
         (catch Exception e (handler request)))))

(defn wrap-query-string-params-request
  [handler]
  (fn [request]
    (try (let [qs-map (codec/form-decode (:query-string request))]
           (handler (merge-with merge request {:query-string-params qs-map
                                               :params qs-map})))
         (catch Exception e (handler request)))))

(defn wrap-body-params-request
  [handler]
  (fn [request]
    (try (let [p-map (codec/form-decode (:body request))]
           (handler (merge-with merge request {:body-params p-map
                                               :params p-map})))
         (catch Exception e (handler request)))))

(defn wrap-with-ppxml [handler]
  (fn [request]
    (let [response (handler request)
          ctype (get-in response [:headers "Content-Type"])]
      (if (and ctype (re-matches #"(?i).*text/html.*" ctype))
        (try (assoc response :body (ppxml (:body response)))
             (catch Exception e response))
        response))))
