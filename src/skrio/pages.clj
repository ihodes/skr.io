(ns skrio.pages
  (:use hiccup.core
        hiccup.page
        compojure.response))


(defn index [req]
  (slurp "http://www.skr.io/text/52812197e4b0b1a5cf4f462d.html"))


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
