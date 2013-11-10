(ns skrio.extractor
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]))



(def extraction-errors
  {:x-www-form-urlencoded "x-www-form-urlencoded data must contain `text` key."
   :json "Improperly formed JSON document."
   :xml "Improperly formed XML document."})


(defmulti extract-text :content-type)


(defmethod extract-text "application/x-www-form-urlencoded"
  [req]
  (if (contains? (:body-params req) "text")
    {:text (get (:body-params req) "text") :content-type "text/plain"}
    (throw (Exception. (:x-www-form-urlencoded extraction-errors)))))

(defmethod extract-text "application/json"
  [req]
  (let [body (:body req)]
    (try
      (json/read-str body)
      {:text body :content-type "application/json"}
      (catch Exception e (throw (Exception. (:json extraction-errors)))))))

(defmethod extract-text "application/markdown"
  [req]
  {:text (:body req) :content-type (:content-type req)})

;; TK TODO: This doesn't work (wrt checking validity of XML)
(defmethod extract-text "application/xml"
  [req]
  (let [body (:body req)]
    (try
      (xml/parse-str body)
      {:text body :content-type "application/xml"}
      (catch Exception e (throw (Exception. (:xml extraction-errors)))))))

(defmethod extract-text "text/plain"
  [req]
  {:text (:body req) :content-type "text/plain"})

(defmethod extract-text "text/html"
  [req]
  {:text (:body req) :content-type "text/html"})

(defmethod extract-text :default
  [req]
  {:text (:body req) :content-type "text/plain"})

