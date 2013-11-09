(ns skrio.middleware
  (:use skrio.helpers
        [clojure.string :only (split)]
        [clojure.set :only (rename-keys)]
        [clojure.data.codec.base64 :only (encode decode)]
        [ring.util.response :only (charset)]))

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
