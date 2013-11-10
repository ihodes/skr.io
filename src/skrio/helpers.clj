(ns skrio.helpers
  (:use [ring.util.response :only (status response content-type header)])
  (:require [clojure.set :as set]
            [clojurewerkz.scrypt.core :as scrypt]
            [clojure.data.codec.base64 :as b64]))



(defn str->base64-str [s]
  (String. (b64/encode (.getBytes s)) "UTF-8"))
(defn base64-str->str [s]
  (String. (b64/decode (.getBytes s)) "UTF-8"))


(defn can-access? [user obj]
  (and user obj (= (:id user) (:user obj))))

(def not-found (status (response "Not Found") 404))

(def access-denied (status (response "Access Denied") 401))


(def remove-user #(dissoc % :user))
(defn remove-users
  [coll]
  (map remove-user coll))
(defn rename-id
  [m]
  (set/rename-keys m {:_id :id}))
(defn prepare-id
  [m]
  (-> m (set/rename-keys {:_id :id}) (update-in [:id] str)))
(defn prepare-ids
  [coll] 
  (map  prepare-id coll))
(defn truncate-text
  [text]
  (if (> (count text) 25)
    (str (apply str (take 24 text)) "…")
    text))
(defn truncate-texts
  [texts]
  (map truncate-text texts))


(defn gen-secret
  [n]
  (let [secret-char? (partial re-matches #"\w")
        int->str (comp str char)
        rand-stream (repeatedly #(+ 32 (rand-int 95)))]
    (apply str (take n (filter secret-char? (map int->str rand-stream))))))


(defn encrypt [pw] (scrypt/encrypt pw (Math/pow 2 16) 8 1))
(defn verify [plain-pw encrypted-pw] (scrypt/verify plain-pw encrypted-pw))


(def str-_id (comp str :_id))
(defn auth
  [resp]
  (header resp "WWW-Authenticate" "Basic realm=\"texts\""))
