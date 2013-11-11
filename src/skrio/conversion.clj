(ns skrio.conversion
  (:use skrio.helpers)
  (:require [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [cheshire.core :as ch]
            [markdown.core :as md]
            [hiccup.core :as h]
            [skrio.pages :as pages]))


(defn- pretty-string-json
  [text]
  (ch/generate-string (ch/parse-string text)
                      {:pretty true}))
(defn- pretty-syntax-json
  [text]
  (md/md-to-html-string 
   (str "\n```js\n" (pretty-string-json text) "\n```\n")))
(defn- pretty-syntax-xml
  [text]
  (md/md-to-html-string 
   (str "\n```xml\n" (ppxml (str \newline \newline text)) "\n```\n")))


(def text-conversions
  {"text/plain"           {:default [identity "text/plain"]
                           :txt     [identity "text/plain"]
                           :html    [(fn [text]
                                       (h/html [:html
                                                [:head [:title "Some Text"]]
                                                [:body [:p text]]]))
                                     "text/html"]}
   
   "text/html"            {:default [identity "text/html"]
                           :txt     [identity "text/plain"]
                           :html    [identity "text/html"]}
   
   "application/json"     {:default [#(ch/generate-string (ch/parse-string %)
                                                          {:pretty true})
                                     "application/json"]
                           :json    [#(ch/generate-string (ch/parse-string %)
                                                          {:pretty true})
                                     "application/json"]
                           :txt     [identity
                                     "text/plain"]
                           :html    [#(pages/text-page "json" (pretty-syntax-json %))
                                     "text/html"]}
   
   "application/xml"      {:default [identity "application/xml"]
                           :xml     [identity "application/xml"]
                           :txt     [identity "text/plain"]
                           :html    [#(pages/text-page "xml" (pretty-syntax-xml %))
                                     "text/html"]}
   
   "application/markdown" {:default [identity "text/plain"]
                           :txt     [identity "text/plain"]
                           :render  [(fn [text] (md/md-to-html-string (str text \newline \newline)))
                                      "text/plain"]
                           :html    [#(pages/text-page "markdown"
                                                       (md/md-to-html-string
                                                        (str % "\n\n")))
                                     "text/html"]}})


(defn convert [{text :text {content-type :content-type} :metadata} to]
  (if-let [[f r-content-type] (get-in text-conversions [content-type to])]
    {:text (f text) :content-type r-content-type}
    (throw
     (Exception. (str "Cannot convert " (name content-type) " to " (name to) ".")))))
