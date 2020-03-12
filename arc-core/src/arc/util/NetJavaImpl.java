package arc.util;

import arc.Net;
import arc.Net.*;
import arc.struct.Array;
import arc.struct.ObjectMap;
import arc.func.Cons;
import arc.util.async.AsyncExecutor;
import arc.util.io.Streams;

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
    private boolean block;

    public void http(HttpRequest request, Cons<HttpResponse> success, Cons<Throwable> failure){
        if(request.url == null){
            failure.get(new ArcRuntimeException("can't process a HTTP request without URL set"));
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

            run(() -> {
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
                                Streams.close(writer);
                            }
                        }else{
                            InputStream contentAsStream = request.contentStream;
                            if(contentAsStream != null){
                                OutputStream os = connection.getOutputStream();
                                try{
                                    Streams.copy(contentAsStream, os);
                                }finally{
                                    Streams.close(os);
                                }
                            }
                        }
                    }

                    connection.connect();

                    HttpClientResponse clientResponse = new HttpClientResponse(connection);
                    try{
                        success.get(clientResponse);
                    }finally{
                        connection.disconnect();
                    }

                }catch(Throwable e){
                    connection.disconnect();
                    failure.get(e);
                }
            });
        }catch(Throwable e){
            failure.get(e);
        }
    }

    public void setBlock(boolean block){
        this.block = block;
    }

    private void run(Runnable run){
        if(block){
            run.run();
        }else{
            asyncExecutor.submit(run);
        }
    }

    static class HttpClientResponse implements HttpResponse{
        private final HttpURLConnection connection;
        private Net.HttpStatus status;

        public HttpClientResponse(HttpURLConnection connection) throws IOException{
            this.connection = connection;
            this.status = Net.HttpStatus.byCode(connection.getResponseCode());
        }

        @Override
        public byte[] getResult(){
            InputStream input = getInputStream();

            // If the response does not contain any content, input will be null.
            if(input == null){
                return Streams.EMPTY_BYTES;
            }

            try{
                return Streams.copyBytes(input, connection.getContentLength());
            }catch(IOException e){
                return Streams.EMPTY_BYTES;
            }finally{
                Streams.close(input);
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
                return Streams.copyString(input, connection.getContentLength());
            }catch(IOException e){
                return "";
            }finally{
                Streams.close(input);
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
            //convert between the struct types
            ObjectMap<String, Array<String>> out = new ObjectMap<>();
            Map<String, List<String>> fields = connection.getHeaderFields();
            for(String key : fields.keySet()){
                if(key != null){
                    out.put(key, Array.with(fields.get(key).toArray(new String[0])));
                }
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
