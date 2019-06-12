package io.anuke.arc.util;

import io.anuke.arc.Net;
import io.anuke.arc.Net.*;
import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;
import io.anuke.arc.util.async.AsyncExecutor;
import io.anuke.arc.util.io.Streams;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Implements part of the {@link Net} API using {@link HttpURLConnection}, to be easily reused between the Android and Desktop
 * backends.
 * @author acoppes
 */
public class NetJavaImpl{
    private final AsyncExecutor asyncExecutor = new AsyncExecutor(6);

    public void http(HttpRequest request, Consumer<HttpResponse> success, Consumer<Throwable> failure){
        if(request.url == null){
            failure.accept(new ArcRuntimeException("can't process a HTTP request without URL set"));
            return;
        }

        try{
            HttpMethod method = request.method;
            URL url;

            if(method == HttpMethod.GET){
                String queryString = "";
                String value = request.content;
                if(value != null && !"".equals(value)) queryString = "?" + value;
                url = new URL(request.url + queryString);
            }else{
                url = new URL(request.url);
            }

            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            //should be enabled to upload data.
            boolean doingOutPut = method == HttpMethod.POST || method == HttpMethod.PUT;
            connection.setDoOutput(doingOutPut);
            connection.setDoInput(true);
            connection.setRequestMethod(method.toString());
            HttpURLConnection.setFollowRedirects(request.followRedirects);

            //set headers
            request.headers.each(connection::addRequestProperty);

            //timeouts
            connection.setConnectTimeout(request.timeout);
            connection.setReadTimeout(request.timeout);

            asyncExecutor.submit(() -> {
                try{
                    // Set the content for POST and PUT (GET has the information embedded in the URL)
                    if(doingOutPut){
                        // we probably need to use the content as stream here instead of using it as a string.
                        String contentAsString = request.content;
                        if(contentAsString != null){
                            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                            try{
                                writer.write(contentAsString);
                            }finally{
                                Streams.closeQuietly(writer);
                            }
                        }else{
                            InputStream contentAsStream = request.contentStream;
                            if(contentAsStream != null){
                                OutputStream os = connection.getOutputStream();
                                try{
                                    Streams.copyStream(contentAsStream, os);
                                }finally{
                                    Streams.closeQuietly(os);
                                }
                            }
                        }
                    }

                    connection.connect();

                    HttpClientResponse clientResponse = new HttpClientResponse(connection);
                    try{
                        success.accept(clientResponse);
                    }finally{
                        connection.disconnect();
                    }

                }catch(Exception e){
                    connection.disconnect();
                    failure.accept(e);
                }

                return null;
            });
        }catch(Exception e){
            failure.accept(e);
        }
    }

    static class HttpClientResponse implements HttpResponse{
        private final HttpURLConnection connection;
        private Net.HttpStatus status;

        public HttpClientResponse(HttpURLConnection connection){
            this.connection = connection;
            try{
                this.status = Net.HttpStatus.byCode(connection.getResponseCode());
            }catch(IOException e){
                this.status = Net.HttpStatus.UNNOWN_STATUS;
            }
        }

        @Override
        public byte[] getResult(){
            InputStream input = getInputStream();

            // If the response does not contain any content, input will be null.
            if(input == null){
                return Streams.EMPTY_BYTES;
            }

            try{
                return Streams.copyStreamToByteArray(input, connection.getContentLength());
            }catch(IOException e){
                return Streams.EMPTY_BYTES;
            }finally{
                Streams.closeQuietly(input);
            }
        }

        @Override
        public String getResultAsString(){
            InputStream input = getInputStream();

            // If the response does not contain any content, input will be null.
            if(input == null){
                return "";
            }

            try{
                return Streams.copyStreamToString(input, connection.getContentLength());
            }catch(IOException e){
                return "";
            }finally{
                Streams.closeQuietly(input);
            }
        }

        @Override
        public InputStream getResultAsStream(){
            return getInputStream();
        }

        @Override
        public Net.HttpStatus getStatus(){
            return status;
        }

        @Override
        public String getHeader(String name){
            return connection.getHeaderField(name);
        }

        @Override
        public ObjectMap<String, Array<String>> getHeaders(){
            //convert between the collection types
            ObjectMap<String, Array<String>> out = new ObjectMap<>();
            Map<String, List<String>> fields = connection.getHeaderFields();
            for(String key : fields.keySet()){
                out.put(key, Array.with(fields.get(key).toArray(new String[0])));
            }
            return out;
        }

        private InputStream getInputStream(){
            try{
                return connection.getInputStream();
            }catch(IOException e){
                return connection.getErrorStream();
            }
        }
    }
}
