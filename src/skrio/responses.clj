(ns skrio.responses
  (:use [ring.util.response :only (status response)]))



(defn respond
  ([msg] (response msg))
  ([code msg] (status (response msg) code)))

;; This is where respond-XXX functions are defined.
(defn def-json-response!
  [code type]
  (letfn [(merge-msg [m msg] (if (coll? msg)
                               (merge m msg)
                               (merge m {:message msg})))
          (resp
            ([]     (-> {:error type} response (status code)))
            ([addl] (-> {:error type}
                        (merge-msg addl)
                        response
                        (status code))))]
    (intern *ns* (symbol (str "respond-json-" code)) resp)))

(def http-status-codes
  [;; 100s
   [100 "Continue"] [101 "Switching Protocols"]
   ;; 200s
   [200 "OK"] [201 "Created"] [202 "Accepted"] [203 "Non-Authoritative Information"]
   [204 "No Content"] [205 "Reset Content"] [206 "Partial Content"]
   ;; 300s
   [300 "Multiple Choices"] [301 "Moved Permanently"] [302 "Found"]
   [303 "See Other"] [304 "Not Modified"] [305 "Use Proxy"] [306 "(Unused)"]
   [307 "Temporary Redirect"] 
   ;; 400s
   [400 "Bad Request"] [401 "Unauthorized"] 
   [402 "Payment Required"] [403 "Forbidden"] [404 "Not Found"]
   [405 "Method Not Allowed"] [406 "Not Acceptable"] 
   [407 "Proxy Authentication Required"] [408 "Request Timeout"]
   [409 "Conflict"] [410 "Gone"] [411 "Length Required"]
   [412 "Precondition Failed"] [413 "Request Entity Too Large"]
   [414 "Request-URI Too Long"] [415 "Unsupported Media Type"]
   [416 "Requested Range Not Satisfiable"] [417 "Expectation Failed"]
   ;; 500s
   [500 "Internal Server Error"] [501 "Not Implemented"] [502 "Bad Gateway"]
   [503 "Service Unavailable"] [504 "Gateway Timeout"]
   [505 "HTTP Version Not Supported"]])

(doseq [[code name] http-status-codes]
  (def-json-response! code name))
