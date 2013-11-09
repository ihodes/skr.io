(ns skrio.pages
  (:use hiccup.core
        compojure.response))




(defn index [req]
  (render
   (html [:html
          [:head
           [:title "Skr.io | Textual API"]
           [:link {:href "/public/style.css" :rel "stylesheet"} ]]
          [:body
           [:h3 "We Do Text Better"]
           [:p "Skr.io is a REST-inspired API for the storage & retrieval of text."]
           [:p "Our API docs can be found <a href=\"http://skrio.herokuapp.com/text/527ea6a8e4b090f819cc2968.html\">here</a>."]
           [:p "For an invite, please <a href=\"mailto:isaac@marion.io\" target=\"_blank\">email me</a>."]
           ]])
   req))


