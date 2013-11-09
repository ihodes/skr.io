(ns skrio.conversion
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [markdown.core :as md]
            [hiccup.core :as h]))



(def text-conversions
  {:text/plain           {:default [identity "text/plain"]
                          :txt     [identity "text/plain"]
                          :html    [(fn [text]
                                      (h/html [:html
                                               [:head [:title "Some Text"]]
                                               [:body [:p text]]]))
                                    "text/html"]}

   :text/html            {:default [identity "text/html"]
                          :txt     [identity "text/plain"]
                          :html    [identity "text/html"]}

   :application/json     {:default [identity "application/json"]
                          :json    [identity "application/json"]
                          :txt     [identity "text/plain"]
                          :html    [(fn [text]
                                      (h/html [:html
                                               [:head [:title "Some JSON"]]
                                               [:body [:pre text]]]))
                                    "text/html"]}

   :application/xml      {:default [identity "application/xml"]
                          :xml     [identity "application/xml"]
                          :txt     [identity "text/plain"]
                          :html    [identity "text/html"]}

   :application/markdown {:default [identity "text/plain"]
                          :txt     [identity "text/plain"]
                          :render  [(fn [text] (md/md-to-html-string (str text \newline \newline)))
                                    "text/plain"]
                          :html    [(fn [text] 
                                      (h/html [:html
                                               [:head [:title "Markdown"]
                                                [:link {:href "/public/normalize.css"
                                                        :rel "stylesheet"} ]
                                                [:link {:href "/public/markdown.css"
                                                        :rel "stylesheet"} ]
                                                [:link {:href "/public/syntax.css"
                                                        :rel "stylesheet"} ]]
                                               [:body (md/md-to-html-string 
                                                       (str text \newline \newline))]]))
                                    "text/html"]}})


(defn convert [{:keys [text content-type]} to]
  (if-let [[f r-content-type] (get-in text-conversions [(keyword content-type) to])]
    {:text (f text) :content-type r-content-type}
    (throw
     (Exception. (str "Cannot convert " (name content-type) " to " (name to) ".")))))
