(ns skrio.pages
  (:use hiccup.core
        hiccup.page
        compojure.response))




(defn index [req]
  (render
   (html (doctype :html5)
    [:html
     [:head
      [:title "Skr.io | Textual API"]
      
      [:link {:href "/public/normalize.css" :rel "stylesheet"}]
      [:link {:href "/public/style.css" :rel "stylesheet"}]
      [:script {:type "text/javascript" :src "//use.typekit.net/rim7ixs.js"} "var noop;"]
      [:script {:type "text/javascript"} "try{Typekit.load();}catch(e){}"]
      ]
     
     [:body
      [:h1 "Text Online, Better<code style=\"size:.8em;color:#b9b9b9\">.txt</code>"]
      [:p [:b "Skr.io is a REST-inspired API for the storage &amp; retrieval of text."]]
      [:p "We're a simple database in the cloud; as much as you like, or as simple as can be. If you need something else, and we don't offer it yet, send us an <a href=\"mailto:isaac@marion.io\" target=\"_blank\">email</a> and we'll see what we can do."]
      [:p "We expose a simple interface to your text online. With it you can store, display (in multiple formats), query (with XPath or JSONPath, if the text is JSON or XML), render into HTML (if it is Markdown), displayed nicely with syntax highlighting and all (if any format), add and remove metadata, or simply returned as it was posted; in plain text. "]
      [:p "<b>Our API docs can be found <a style=\"font-weight:700;\" href=\"http://skrio.herokuapp.com/text/527ea6a8e4b090f819cc2968.html\">here</a></b>. Hosted, of course, using <code>Skr.io</code>. If you like, you can change the extension on the url to <code>.render</code> or <code>.txt</code> and see what happens."]
      
      [:h2 "How does this work?"]
      [:p "In about a minute or two, you'll see just what you can do with <code>Skr.io</code>."]
      [:p "Most of these commands require an API token and secret; you can authenticate using basic auth. You can get a key and secret by signing up; it's free and easy. You should do it and follow along with real credentials."]
      

      [:div {:class "codez"}
       [:div {:class "explain col"}
        (str "To post a simple bit of plain text, you may just post it to the API.<br><br>It will return the ID of the text object. With it, you can rule the world.")]
       [:pre {:class "example col"}
        (str "curl http://&lt;token&gt;:&lt;secret&gt;@skr.io/api/v.1/text -dtext=\"Here's our text.\"")]
       [:div {:style "clear: both; "}]
       [:p {:class "explain col"}
        (str "That's boring, though, so let's just POST some JSON instead.")]
       [:pre {:class "example col"}
        (str "curl -H\"content-type: application/json\" http://&lt;token&gt;:&lt;secret&gt;@skr.io/api/v.1/text \\\n\t-d=\"{\"apple\":\"pie\", \"turkey\":{\"and\":\"gravy\", \"numbers\":[1,2,3,4]}}\"")]
       [:p {:class "explain col"}
        (str "Which returns, let's say, the id of <code>xyz123</code>. With this ID, we can do some cool things. First of all, let's make this text public, allowing others to see it who don't have our API key and secret.")]

       [:pre {:class "example col"}
        (str "curl -X POST http://&lt;token&gt;:&lt;secret&gt;@skr.io/api/v.1/text/xyz123/public")]
       [:p {:class "explain col"}
        (str "Now just anyone can see this text at http://skr.io/text/xyz123. It's easy enough to make it private again, too, if you wish.")]

       [:p {:class "explain col"}
        (str "Let's query this tiny bit of JSON, with <a href=\"http://goessner.net/articles/JsonPath/\">JSONPath</a>. If this were XML, you could query it with XPath instead.")]
       [:pre {:class "example col"}
        (str "curl http://&lt;token&gt;:&lt;secret&gt;@skr.io/api/v.1/text/xyz123?q=$.apple")]

       [:p {:class "explain col"}
        (str "...which should, naturally, return \"pie\". This queries can get quite complex, including predicates and other shenanigans. This is a very powerful way to query your text.")]

       [:p {:class "explain col"}
        (str "Even more information can be found in the <a href=\"http://skrio.herokuapp.com/text/527ea6a8e4b090f819cc2968.html\">API</a> documentation, and more features are being added regularly. The documenation itself was published by posting some text formatted with <a href=\"http://daringfireball.net/projects/markdown/\">Markdown</a> with content-type <code>application/markdown</code>, and rendered so nicely by adding a <code>.html</code> extension to the URL (and, obviously, made public so that you can see it. You can check out some other formatting options by replacing the <code>.html</code> with a <code>.txt</code> or <code>.render</code> (or even no extension at all). Other content-types have other extensions which can be applied to them; c.f. the documenation. Again, feel free to email me at <a href=\"mailto:isaac@marion.io\">isaac@marion.io</a> if you have any question, comments, or requests.")]
       
       ]

      ]])
   req))




(defn text-page
  [title body]
  (html (doctype :html5)
   [:html
    [:head [:title title]
     [:link {:href "/public/normalize.css" :rel "stylesheet" :type "text/css"} ]
     [:link {:href "/public/markdown.css" :rel "stylesheet" :type "text/css"} ]
     [:link {:href "/public/sh/shCore.css" :rel "stylesheet" :type "text/css"} ]
     [:script {:src "/public/sh/shCore.js" :type "text/javascript"} ]
     [:script {:src "/public/sh/shAutoloader.js" :type "text/javascript"} ]
     [:link {:href "/public/sh/shThemeEclipse.css" :rel "stylesheet" :type "text/css"}]
     ]
    [:body body]
    [:script {:type "text/javascript"}
     (str """
function path() {
 var args = arguments,
      result = [];
       
  for (var i = 0; i < args.length; i++)
      result.push(args [i].replace ('@', '/public/sh/'));

  return result;
};
 
SyntaxHighlighter.autoloader.apply(null, path (
  'applescript            @shBrushAppleScript.js',
  'actionscript3 as3      @shBrushAS3.js',
  'bash shell             @shBrushBash.js',
  'clojure clj            @shBrushClojure.js',
  'coldfusion cf          @shBrushColdFusion.js',
  'cpp c                  @shBrushCpp.js',
  'c# c-sharp csharp      @shBrushCSharp.js',
  'css                    @shBrushCss.js',
  'delphi pascal          @shBrushDelphi.js',
  'diff patch pas         @shBrushDiff.js',
  'erl erlang             @shBrushErlang.js',
  'groovy                 @shBrushGroovy.js',
  'java                   @shBrushJava.js',
  'jfx javafx             @shBrushJavaFX.js',
  'js json jscript javascript  @shBrushJScript.js',
  'perl pl                @shBrushPerl.js',
  'php                    @shBrushPhp.js',
  'text plain txt         @shBrushPlain.js',
  'py python              @shBrushPython.js',
  'ruby rails ror rb      @shBrushRuby.js',
  'sass scss              @shBrushSass.js',
  'scala                  @shBrushScala.js',
  'scheme scm             @shBrushScheme.js',
  'sql                    @shBrushSql.js',
  'vb vbnet               @shBrushVb.js',
  'xml xhtml xslt html erb  @shBrushXml.js'));
 """
 "SyntaxHighlighter.all();"
 "SyntaxHighlighter.defaults['gutter'] = false;"
 "SyntaxHighlighter.defaults['toolbar'] = false;"
 "SyntaxHighlighter.defaults['tab-size'] = 2;")]]))
