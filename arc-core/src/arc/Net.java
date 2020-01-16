package arc;

import arc.struct.*;
import arc.func.Cons;
import arc.util.*;

import java.io.InputStream;

/**
 * Provides methods to perform networking operations, such as simple HTTP get and post requests, and TCP server/client socket
 * communication.</p>
 * <p>
 * To perform an HTTP request create a {@link HttpRequest} with the HTTP method (see {@link HttpMethod} for common methods) and
 * invoke {@link #httpPost(String, String, Cons, Cons)} with it and a listener. After the HTTP
 * request was processed, the listener is called with a {@link HttpResponse} with the HTTP response values and
 * an status code to determine if the request was successful or not.</p>
 * <p>
 * @author mzechner
 * @author noblemaster
 * @author arielsan
 */
public class Net{
    private NetJavaImpl impl = new NetJavaImpl();

    /** Whether to block on HTTP requests. Default is false. */
    public void setBlock(boolean block){
        impl.setBlock(block);
    }

    /**
     * Process the specified {@link HttpRequest} and reports the {@link HttpResponse} to the specified listener.
     * .
     * @param httpRequest The {@link HttpRequest} to be performed.
     * @param success The listener to call once the HTTP response is ready to be processed.
     * @param failure The listener to call if the request fails.
     */
    public void http(HttpRequest httpRequest, Cons<HttpResponse> success, Cons<Throwable> failure){
        impl.http(httpRequest, success, failure);
    }

    /** Sends a basic HTTP GET request.*/
    public void httpGet(String url, Cons<HttpResponse> success, Cons<Throwable> failure){
        http(new HttpRequest().method(HttpMethod.GET).url(url), success, failure);
    }

    /** Sends a basic HTTP POST request.*/
    public void httpPost(String url, String content, Cons<HttpResponse> success, Cons<Throwable> failure){
        http(new HttpRequest().method(HttpMethod.POST).content(content).url(url), success, failure);
    }

    /**
     * Launches the default browser to display a URI. If the default browser is not able to handle the specified URI, the
     * application registered for handling URIs of the specified type is invoked. The application is determined from the protocol
     * and path of the URI. A best effort is made to open the given URI; however, since external applications are involved, no guarantee
     * can be made as to whether the URI was actually opened. If it is known that the URI was not opened, false will be returned;
     * otherwise, true will be returned.
     * @param URI the URI to be opened.
     * @return false if it is known the uri was not opened, true otherwise.
     */
    public boolean openURI(String URI){
        return false;
    }

    /** HTTP response interface with methods to get the response data as a byte[], a {@link String} or an {@link InputStream}. */
    public interface HttpResponse{
        /**
         * Returns the data of the HTTP response as a byte[].
         * <p>
         * <b>Note</b>: This method may only be called once per response.
         * </p>
         * @return the result as a byte[] or null in case of a timeout or if the operation was canceled/terminated abnormally. The
         * timeout is specified when creating the HTTP request, with {@link HttpRequest#timeout(int)}
         */
        byte[] getResult();

        /**
         * Returns the data of the HTTP response as a {@link String}.
         * <p>
         * <b>Note</b>: This method may only be called once per response.
         * </p>
         * @return the result as a string or null in case of a timeout or if the operation was canceled/terminated abnormally. The
         * timeout is specified when creating the HTTP request, with {@link HttpRequest#timeout(int)}
         */
        String getResultAsString();

        /**
         * Returns the data of the HTTP response as an {@link InputStream}. <b><br>
         * Warning:</b> Do not store a reference to this InputStream. The underlying HTTP connection will be closed after that
         * callback finishes executing. Reading from the InputStream after it's connection has been closed will lead to exception.
         * @return An {@link InputStream} with the {@link HttpResponse} data.
         */
        InputStream getResultAsStream();

        /** Returns the {@link HttpStatus} containing the statusCode of the HTTP response. */
        HttpStatus getStatus();

        /**
         * Returns the value of the header with the given name as a {@link String}, or null if the header is not set.
         */
        String getHeader(String name);

        /**
         * Returns a Map of the headers. The keys are Strings that represent the header name. Each values is a List of Strings that
         * represent the corresponding header values.
         */
        ObjectMap<String, Array<String>> getHeaders();
    }

    /** Provides all HTTP methods to use when creating a {@link HttpRequest}.*/
    public enum HttpMethod{
        GET, POST, PUT, DELETE, HEAD, CONNECT, OPTIONS, TRACE
    }

    public static class HttpRequest{
        /** The HTTP method. */
        public HttpMethod method;
        /** The URL to send this request to.*/
        public String url;
        public ObjectMap<String, String> headers = new ObjectMap<>();
        /**The time to wait for the HTTP request to be processed, use 0 to block until it is done. The timeout is used for both
         * the timeout when establishing TCP connection, and the timeout until the first byte of data is received.*/
        public int timeout = 2000;

        /**The content to be used in the HTTP request: A string encoded in the corresponding Content-Encoding set in the headers, with the data to send with the
         * HTTP request. For example, in case of HTTP GET, the content is used as the query string of the GET while on a
         * HTTP POST it is used to send the POST data.*/
        public String content;

        /**The content as a stream to be used for a POST for example, to transmit custom data.*/
        public InputStream contentStream;
        /**Length of the content stream.*/
        public long contentLength;

        /**Sets whether 301 and 302 redirects are followed. By default true. Can't be changed in the web backend because this uses
         * XmlHttpRequests which always redirect.*/
        public boolean followRedirects = true;
        /** Whether a cross-origin request will include credentials. By default false. */
        public boolean includeCredentials = false;

        public HttpRequest method(HttpMethod method){
            this.method = method;
            return this;
        }

        public HttpRequest url(String url){
            this.url = url;
            return this;
        }

        public HttpRequest timeout(int timeout){
            this.timeout = timeout;
            return this;
        }

        public HttpRequest redirects(boolean followRedirects){
            this.followRedirects = followRedirects;
            return this;
        }

        public HttpRequest credentials(boolean includeCredentials){
            this.includeCredentials = includeCredentials;
            return this;
        }

        public HttpRequest header(String name, String value){
            headers.put(name, value);
            return this;
        }

        public HttpRequest content(String content){
            this.content = content;
            return this;
        }

        public HttpRequest content(InputStream contentStream, long contentLength){
            this.contentStream = contentStream;
            this.contentLength = contentLength;
            return this;
        }
    }

    /** Defines the status of an HTTP request.*/
    public enum HttpStatus{
        UNKNOWN_STATUS(-1),

        CONTINUE(100),
        SWITCHING_PROTOCOLS(101),
        PROCESSING(102),

        OK(200),
        CREATED(201),
        ACCEPTED(202),

        NON_AUTHORITATIVE_INFORMATION(203),
        NO_CONTENT(204),
        RESET_CONTENT(205),
        PARTIAL_CONTENT(206),
        MULTI_STATUS(207),
        MULTIPLE_CHOICES(300),
        MOVED_PERMANENTLY(301),
        MOVED_TEMPORARILY(302),

        SEE_OTHER(303),
        NOT_MODIFIED(304),
        USE_PROXY(305),
        TEMPORARY_REDIRECT(307),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        PAYMENT_REQUIRED(402),

        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        NOT_ACCEPTABLE(406),
        PROXY_AUTHENTICATION_REQUIRED(407),
        REQUEST_TIMEOUT(408),
        CONFLICT(409),
        GONE(410),
        LENGTH_REQUIRED(411),
        PRECONDITION_FAILED(412),
        REQUEST_TOO_LONG(413),
        REQUEST_URI_TOO_LONG(414),
        UNSUPPORTED_MEDIA_TYPE(415),
        REQUESTED_RANGE_NOT_SATISFIABLE(416),
        EXPECTATION_FAILED(417),
        INSUFFICIENT_SPACE_ON_RESOURCE(419),
        METHOD_FAILURE(420),
        UNPROCESSABLE_ENTITY(422),

        LOCKED(423),
        FAILED_DEPENDENCY(424),
        INTERNAL_SERVER_ERROR(500),
        NOT_IMPLEMENTED(501),
        BAD_GATEWAY(502),

        SERVICE_UNAVAILABLE(503),
        GATEWAY_TIMEOUT(504),
        HTTP_VERSION_NOT_SUPPORTED(505),
        INSUFFICIENT_STORAGE(507);

        private static IntMap<HttpStatus> byCode;

        public final int code;

        HttpStatus(int code){
            this.code = code;
        }

        /** Find an HTTP status enum by code.*/
        public static HttpStatus byCode(int code){
            if(byCode == null){
                byCode = new IntMap<>();
                for(HttpStatus status : HttpStatus.values()){
                    byCode.put(status.code, status);
                }
            }
            return byCode.get(code, UNKNOWN_STATUS);
        }
    }
}
