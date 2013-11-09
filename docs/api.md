# Skr.io: Text In, Text Out

Skr.io is a web API for text. With it, you can store text, and retrieve them at unique URLs. Text may be published (accessible without an API token), as well as changed, and transformed. Skr.io intelligently stores text based on the content-type it is POSTed with, and can display it, depending on the format, in a variety of ways.

## Authentication and URL

Authentication is done via basic auth; a token and secret will be provided to you once you have an account. All requests must be over HTTPS.

The base URL for all API requests is

    https://<token>:<secret>@api.skr.io/api/v.1/

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

You may **get** a text, returning the UUID of the text:

    curl .../text/<text-id>[.ext]?

If an `ext` is included, a transformation will optionally be done on the text, depending on the `content-type` of the stored text object.

Below is a mapping of the text object's `content-type` + an `ext` to the resulting response. If an invalid `ext` is attempted, a 400 error will be returned. If no `ext` is listed, the default transformation is applied (listed below).

1. `*` with `.txt` -> Text content is returned, "Content-type: text/plain"
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


### Update Text

You may **replace** a text's contents, returning the UUID of the text:

    curl -X PUT .../text/<text-id> -dtext="This is the new text."

NB: Currently, you can only update data with the `x-www-form-urlencoded` data at the `text` key.


### Delete Text

You may **delete** a text, returning the UUID of the text:

    curl -X DELETE .../text/<text-id>


### Publicize Text

You may **publicize** a text, making the text available to those without API tokens/secrets at the URL `https://api.skr.io/text/<text-id>`, returning the UUID of the text:

    curl -X POST .../text/<text-id>/public


### Privatize Text

You may **privatize** a text, making the text no longer available to the public, returning the UUID of the text:

    curl -X POST .../text/<text-id>/private


