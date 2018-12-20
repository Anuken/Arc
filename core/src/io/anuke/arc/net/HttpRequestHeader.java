package io.anuke.arc.net;

/**
 * A list of common request header constants of the HTTP protocol. See http://en.wikipedia.org/wiki/List_of_HTTP_header_fields.
 * @author Daniel Holderbaum
 */
public interface HttpRequestHeader{

    /**
     * Content-Types that are acceptable for the response.
     * <p>
     * Example: Accept: text/plain
     */
    String Accept = "Accept";

    /**
     * Character sets that are acceptable.
     * <p>
     * Example: Accept-Charset: utf-8
     */
    String AcceptCharset = "Accept-Charset";

    /**
     * List of acceptable encodings.
     * <p>
     * Example: Accept-Encoding: gzip, deflate
     */
    String AcceptEncoding = "Accept-Encoding";

    /**
     * List of acceptable human languages for response.
     * <p>
     * Example: Accept-Language: en-US
     */
    String AcceptLanguage = "Accept-Language";

    /**
     * Acceptable version in time.
     * <p>
     * Example: Accept-Datetime: Thu, 31 May 2007 20:35:00 GMT
     */
    String AcceptDatetime = "Accept-Datetime";

    /**
     * Authentication credentials for HTTP authentication.
     * <p>
     * Example: Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     */
    String Authorization = "Authorization";

    /**
     * Used to specify directives that must be obeyed by all caching mechanisms along the request-response chain.
     * <p>
     * Example: Cache-Control: no-cache
     */
    String CacheControl = "Cache-Control";

    /**
     * What type of connection the user-agent would prefer.
     * <p>
     * Example: Connection: keep-alive
     */
    String Connection = "Connection";

    /**
     * An HTTP cookie previously sent by the server with Set-Cookie (below).
     * <p>
     * Example: Cookie: $Version=1=""; Skin=new="";
     */
    String Cookie = "Cookie";

    /**
     * The length of the request body in octets (8-bit bytes).
     * <p>
     * Example: Content-Length: 348
     */
    String ContentLength = "Content-Length";

    /**
     * A Base64-encoded binary MD5 sum of the content of the request body.
     * <p>
     * Example: Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
     */
    String ContentMD5 = "Content-MD5";

    /**
     * The MIME type of the body of the request (used with POST and PUT requests).
     * <p>
     * Example: Content-Type: application/x-www-form-urlencoded
     */
    String ContentType = "Content-Type";

    /**
     * The date and time that the message was sent (in "HTTP-date" format as defined by RFC 7231).
     * <p>
     * Example: Date: Tue, 15 Nov 1994 08:12:31 GMT
     */
    String Date = "Date";

    /**
     * Indicates that particular server behaviors are required by the client.
     * <p>
     * Example: Expect: 100-continue
     */
    String Expect = "Expect";

    /**
     * The email address of the user making the request.
     * <p>
     * Example: From: user@example.com
     */
    String From = "From";

    /**
     * The domain name of the server (for virtual hosting), and the TCP port number on which the server is listening. The port
     * number may be omitted if the port is the standard port for the service requested.
     * <p>
     * Example: en.wikipedia.org
     */
    String Host = "Host";

    /**
     * Only perform the action if the client supplied entity matches the same entity on the server. This is mainly for methods like
     * PUT to only update a resource if it has not been modified since the user last updated it.
     * <p>
     * Example: If-Match: "737060cd8c284d8af7ad3082f209582d"
     */
    String IfMatch = "If-Match";

    /**
     * Allows a 304 Not Modified to be returned if content is unchanged.
     * <p>
     * Example: If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    String IfModifiedSince = "If-Modified-Since";

    /**
     * Allows a 304 Not Modified to be returned if content is unchanged, see HTTP ETag.
     * <p>
     * Example: If-None-Match: "737060cd8c284d8af7ad3082f209582d"
     */
    String IfNoneMatch = "If-None-Match";

    /**
     * If the entity is unchanged, send me the part(s) that I am missing=""; otherwise, send me the entire new entity.
     * <p>
     * Example: If-Range: "737060cd8c284d8af7ad3082f209582d"
     */
    String IfRange = "If-Range";

    /**
     * Only send the response if the entity has not been modified since a specific time.
     * <p>
     * Example: If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
     */
    String IfUnmodifiedSince = "If-Unmodified-Since";

    /**
     * Limit the number of times the message can be forwarded through proxies or gateways.
     * <p>
     * Example: Max-Forwards: 10
     */
    String MaxForwards = "Max-Forwards";

    /**
     * Initiates a request for cross-origin resource sharing (asks server for an 'Access-Control-Allow-Origin' response field).
     * <p>
     * Example: Origin: http://www.example-social-network.com
     */
    String Origin = "Origin";

    /**
     * Implementation-specific fields that may have various effects anywhere along the request-response chain.
     * <p>
     * Example: Pragma: no-cache
     */
    String Pragma = "Pragma";

    /**
     * Authorization credentials for connecting to a proxy.
     * <p>
     * Example: Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     */
    String ProxyAuthorization = "Proxy-Authorization";

    /**
     * Request only part of an entity. Bytes are numbered from 0.
     * <p>
     * Example: Range: bytes=500-999
     */
    String Range = "Range";

    /**
     * This is the address of the previous web page from which a link to the currently requested page was followed. (The word
     * "referrer" has been misspelled in the RFC as well as in most implementations to the point that it has become standard usage
     * and is considered correct terminology).
     * <p>
     * Example: Referer: http://en.wikipedia.org/wiki/Main_Page
     */
    String Referer = "Referer";

    /**
     * The transfer encodings the user agent is willing to accept: the same values as for the response header field
     * Transfer-Encoding can be used, plus the "trailers" value (related to the "chunked" transfer method) to notify the server it
     * expects to receive additional fields in the trailer after the last, zero-sized, chunk.
     * <p>
     * Example: TE: trailers, deflate
     */
    String TE = "TE";

    /**
     * The user agent string of the user agent.
     * <p>
     * Example: User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/21.0
     */
    String UserAgent = "User-Agent";

    /**
     * Ask the server to upgrade to another protocol.
     * <p>
     * Example: Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11
     */
    String Upgrade = "Upgrade";

    /**
     * Informs the server of proxies through which the request was sent.
     * <p>
     * Example: Via: 1.0 fred, 1.1 example.com (Apache/1.1)
     */
    String Via = "Via";

    /**
     * A general warning about possible problems with the entity body.
     * <p>
     * Example: Warning: 199 Miscellaneous warning
     */
    String Warning = "Warning";

}
