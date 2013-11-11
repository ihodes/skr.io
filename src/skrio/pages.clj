(ns skrio.pages
  (:use hiccup.core
        hiccup.page
        compojure.response))


(defn index [req]
  (slurp "http://www.skr.io/text/52812197e4b0b1a5cf4f462d.html"))

(defn -old-index [req]
  (render
   (html (doctype :html5)
    [:html
     [:head
      [:title "Skr.io | Textual API"]
      
      [:link {:href "/public/normalize.css" :rel "stylesheet"}]
      [:link {:href "/public/style.css" :rel "stylesheet"}]
      [:script {:type "text/javascript" :src "//use.typekit.net/rim7ixs.js"} "var noop;"]
      [:script {:type "text/javascript"} "try{Typekit.load();}catch(e){}"]
      [:script {:type "text/javascript"} "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-45615886-1', 'skr.io');
  ga('send', 'pageview');"]
      ]
     
     [:body
      [:h1 "Text Online, Better<code style=\"size:.8em;color:#b9b9b9\">.txt</code>"]
      [:p [:b "Skr.io is an API for the storage, display, querying &amp; retrieval of textual data."]]
      [:p "We're a simple database in the cloud. If you need a feature else, and we don't offer it yet, send us an <a href=\"mailto:isaac@marion.io\" target=\"_blank\">email</a> and we'll see what we can do. More improvements & features are being added all of the time."]
      [:p "We expose a simple interface to your text online. With it you can store, display (in multiple formats), query (with XPath or JSONPath), render into HTML (if it is Markdown), displayed nicely with syntax highlighting and all (if any format), add and remove metadata, or simply returned as it was posted; in plain text. "]
      [:p "<b>Our API docs can be found <a style=\"font-weight:700;\" href=\"http://skr.io/text/527ea6a8e4b090f819cc2968.html\">here</a></b>. Hosted, of course, using <code>Skr.io</code>. If you like, you can change the extension on the url to <code>.render</code> or <code>.txt</code> and see what happens."]

      [:h2 "Sign Up for Beta Access...Today!"]
      
      "<form action= \"http://healthtracr.us5.list-manage.com/subscribe/post?u=e92d9c0a54fa38c0a18563aaa&amp;id=da10ced0b5\" method= \"post\" id= \"mc-embedded-subscribe-form\" name= \"mc-embedded-subscribe-form\" class= \"validate\" target= \"_blank\" novalidate>
  <input type= \"email\" value= \"\" name= \"EMAIL\" style=\"padding: 13px;width:430px;\" placeholder=\"email@example.com\">
  <input type= \"submit\" value= \"Signup for Beta, Now!\" name= \"subscribe\" style=\"background-color: #689805; border-radius: 5px; border: 2px solid #68b805; padding: 14px 36px;width:330px;font-weight:700; \">
</form>"


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
        (str "Even more information can be found in the <a href=\"http://skr.io/text/527ea6a8e4b090f819cc2968.html\">API</a> documentation, and more features are being added regularly. The documenation itself was published by posting some text formatted with <a href=\"http://daringfireball.net/projects/markdown/\">Markdown</a> with content-type <code>application/markdown</code>, and rendered so nicely by adding a <code>.html</code> extension to the URL (and, obviously, made public so that you can see it. You can check out some other formatting options by replacing the <code>.html</code> with a <code>.txt</code> or <code>.render</code> (or even no extension at all). Other content-types have other extensions which can be applied to them; c.f. the documenation. Again, feel free to email me at <a href=\"mailto:isaac@marion.io\">isaac@marion.io</a> if you have any question, comments, or requests.")]
       
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
