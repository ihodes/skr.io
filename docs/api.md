# Skr.io: Text In, Text Out

Skr.io is a web API for text. With it, you can store texts, and retrieve them at a unique URLs. Text may be published (accessible without an API token), as well as changed, and transformed. Skr.io intelligently stores text based on the content-type it is POSTed with, and can display it, depending on the format, in a variety of ways.

## Authentication and URL

Authentication is done via basic auth; a token and secret will be provided to you once you have an account. All requests must be over HTTPS.

The base URL for all API requests is

    http://<token>:<secret>@skrio.herokuapp.com/api/v.1/

(This will become https://api.skr.io/ soon...)


## Errors

Errors are denoated by standard HTTP error codes. Errors are returned as JSON objects with the HTTP Status Code name in the "error" key, and any additional message will be located in a "message" key.

## API

The sole object of the Skr.io API is the `text`, which contains the text content, a globally unique ID, a content-type, a public flag, and any metadata you choose to add.

### List Texts

You may **list** texts, returning a JSON object containing all texts:

    curl -X GET .../text

The object is of the form

    { "texts" : [ text* ] }

Where a `text` looks like:

    { "id":            "<text-id>",
      "text":          "Some text here.\nThis is great.",
      "content-type":  "<interpreted context-type>",
      "public":        <boolean, default `false`>,
      "meta":          <JSON object, default `null`> }


### Create Text

You may **create** a text, returning the UUID of the text:

    curl -X POST .../text/<text-id> -dtext="Nothing to see here."

Various content-types are accepted, and are stored with the text object. The POST body is taken to be the textual data that will be stored, except in the special case of `application/x-www-urlencoded` data, in which case the value of the `text` key is what is stored.

1. `"Content-type: text/plain"                        -> "text/plain"`
2. `"Content-type: application/x-www-form-urlencoded" -> "text/plain"`
  - The text is then taken from the value of the (required) `text` key.
3. `"Content-type: text/html"                         -> "text/html"`
4. `"Content-type: application/json                   -> "application/json"`
  - The POST body must be properly formed JSON, or the POST will fail.
5. `"Content-type: application/xml"                   -> "application/json"`
  - The POST body must be properly formed XML, or the POST will fail.
6. `"Content-type: application/markdown"              -> "application/markdown"`
7. `"Content-type: <anything else>"                   -> "text/plain"`

NB: If you are using CURL, you need to pay special attention to escaping going on. For example,  --data-binary should be used in lieu of -d when posting a file, and $'text..' syntax should be used to post newline and other escape character data if writing strings on the command line.

NB: Currently the max size of a text object which can be uploaded is 2^15 characters. This will change in the future.

### Get Text

You may **get** a text, returning the text:

    curl .../text/<text-id>[.ext]?

If an `ext` is included, a transformation will optionally be done on the text, depending on the `content-type` of the stored text object.

Below is a mapping of the text object's `content-type` + an `ext` to the resulting response. If an invalid `ext` is attempted, a 400 error will be returned. If no `ext` is listed, the default transformation is applied (listed below).

1. `*`
  - `.ext` = `.txt` -> Text content is returned, "Content-type: text/plain"
2. `text/plain`
  - `.ext` = `.default` -> Text content is returned, "Content-type: text/plain"
  - `.ext` = `.html` -> Nicely formatted HTML with text is returned, "Content-type: text/html"
3. `text/html`
  - `.ext` = `.default` -> Text content is returned, "Content-type: text/html"
  - `.ext` = `.html` -> Text content is returned, "Content-type: text/html"
4. `application/json`
  - `.ext` = `.default` -> Text content is returned, "Content-type: application/json"
  - `.ext` = `.html` -> Nicely formatted HTML with formatted, highlighted JSON is returned, "Content-type: text/html"
5. `application/xml`
  - `.ext` = `.default` -> Text content is returned, "Content-type: application/xml"
  - `.ext` = `.html` -> Nicely formatted HTML with formatted, highlighted XML is returned, "Content-type: text/html"
5. `application/markdown`
  - `.ext` = `.default` -> Text content is returned, "Content-type: text/plain"
  - `.ext` = `.html` -> Nicely formatted, rendered Markdown HTML is returned, "Content-type: text/html"
  - `.ext` = `.render` -> Rendered Markdown HTML is returned, "Content-type: text/plain"

### Get Public Text

Anyone (no authentication required) may **get** a *public* text, returning the text:

    curl http://skr.io/text/<text-id>[.ext]?

The same `.ext` rules as above apply here.

NB: The URL here is different.


### Update Text

You may **replace** a text's contents, returning the UUID of the text:

    curl -X PUT .../text/<text-id> -dtext="This is the new text."

If you'd like to post e.g. a file with `--data-binary`, and cannot use the `text` key, send any other content-type (e.g. `-H"Content-type: text/plain"`) and your text will be updated with the entire PUT body instead.


### Delete Text

You may **delete** a text, returning the UUID of the text:

    curl -X DELETE .../text/<text-id>


### Get Metadata for Text

You may get the **metadata** of a text:

    curl .../text/<text-id>/meta

This returns a JSON object with default values for `"name"` ("untitled") and `"created-on"` (<epoch time text was created on>), plus any additional values which can be added.


### Set Metadata for Text

You may set the **metadata** of a text:

    curl -X POST .../text/<text-id>/meta -d"<JSON>"

The JSON object should include only the keys and values which should be updated; existing key-vals will remain in the metadata as-is.

NB: The `Content-type` header is not required here; skr.io will assume that the `content-type` is `application/json` and ignore the header.

NB: Metadata cannot be nested; no JSON Objects are allows within the metadata object (for now.)

### Remove Metadata for Text

You may remove **metadata** of a text:

    curl -X DELETE .../text/<text-id>/meta -dkeyname="<name>"

Removes the key/value of any key in the metadata with name `name`.

### Publicize Text

You may **publicize** a text, making the text available to those without API tokens/secrets at the URL `https://api.skr.io/text/<text-id>`, returning the UUID of the text:

    curl -X POST .../text/<text-id>/public

This is a convenience method for setting the `public` property to true on the text's  metadata.

### Privatize Text

You may **privatize** a text, making the text no longer available to the public, returning the UUID of the text:

    curl -X POST .../text/<text-id>/private

This is a convenience method for setting the `public` property to false on the text's  metadata.

### Set Content-type

You may set the **metadata** of a text:

    curl -X POST .../text/<text-id>/content-type -dcontent-type="<content-type>"

This is a convenience method for setting the `content-type` property on the text's  metadata.

<content-type> may be one of any (and only) "application/x-www-form-urlencoded", "application/json", "application/xml", "application/markdown", "text/html", "text/plain".

Changing this will change which extensions your text object can have applied to it (c.f. get/get public methods), so be careful with this.

### Querying JSON and XML Documents

To **query** a text, returning the value of the key or node:

    curl http://skr.io/api/v.1/text/<text-id>?q=<query>

Texts with Content-types application/json or application/xml may be queried with a subset [JSONPath](http://goessner.net/articles/JsonPath/) or [XPath](http://en.wikipedia.org/wiki/XPath) respectively.

XPath queries may only select text (not attributes), and JSONPath queries may not use array slicing, or array indices, or script expressions. (All of these features are in the works).


