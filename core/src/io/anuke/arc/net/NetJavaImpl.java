package io.anuke.arc.net;

import io.anuke.arc.Net;
import io.anuke.arc.Net.HttpMethods;
import io.anuke.arc.Net.HttpRequest;
import io.anuke.arc.Net.HttpResponse;
import io.anuke.arc.Net.HttpResponseListener;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.utils.ArcRuntimeException;
import io.anuke.arc.utils.async.AsyncExecutor;
import io.anuke.arc.utils.async.AsyncTask;
import io.anuke.arc.utils.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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

    final ObjectMap<HttpRequest, HttpURLConnection> connections;
    final ObjectMap<HttpRequest, HttpResponseListener> listeners;
    private final AsyncExecutor asyncExecutor;
    public NetJavaImpl(){
        asyncExecutor = new AsyncExecutor(1);
        connections = new ObjectMap<HttpRequest, HttpURLConnection>();
        listeners = new ObjectMap<HttpRequest, HttpResponseListener>();
    }

    public void sendHttpRequest(final HttpRequest httpRequest, final HttpResponseListener httpResponseListener){
        if(httpRequest.getUrl() == null){
            httpResponseListener.failed(new ArcRuntimeException("can't process a HTTP request without URL set"));
            return;
        }

        try{
            final String method = httpRequest.getMethod();
            URL url;

            if(method.equalsIgnoreCase(HttpMethods.GET)){
                String queryString = "";
                String value = httpRequest.getContent();
                if(value != null && !"".equals(value)) queryString = "?" + value;
                url = new URL(httpRequest.getUrl() + queryString);
            }else{
                url = new URL(httpRequest.getUrl());
            }

            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            // should be enabled to upload data.
            final boolean doingOutPut = method.equalsIgnoreCase(HttpMethods.POST) || method.equalsIgnoreCase(HttpMethods.PUT);
            connection.setDoOutput(doingOutPut);
            connection.setDoInput(true);
            connection.setRequestMethod(method);
            HttpURLConnection.setFollowRedirects(httpRequest.getFollowRedirects());

            putIntoConnectionsAndListeners(httpRequest, httpResponseListener, connection);

            // Headers get set regardless of the method
            for(Map.Entry<String, String> header : httpRequest.getHeaders().entrySet())
                connection.addRequestProperty(header.getKey(), header.getValue());

            // Set Timeouts
            connection.setConnectTimeout(httpRequest.getTimeOut());
            connection.setReadTimeout(httpRequest.getTimeOut());

            asyncExecutor.submit(new AsyncTask<Void>(){
                @Override
                public Void call(){
                    try{
                        // Set the content for POST and PUT (GET has the information embedded in the URL)
                        if(doingOutPut){
                            // we probably need to use the content as stream here instead of using it as a string.
                            String contentAsString = httpRequest.getContent();
                            if(contentAsString != null){
                                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                                try{
                                    writer.write(contentAsString);
                                }finally{
                                    StreamUtils.closeQuietly(writer);
                                }
                            }else{
                                InputStream contentAsStream = httpRequest.getContentStream();
                                if(contentAsStream != null){
                                    OutputStream os = connection.getOutputStream();
                                    try{
                                        StreamUtils.copyStream(contentAsStream, os);
                                    }finally{
                                        StreamUtils.closeQuietly(os);
                                    }
                                }
                            }
                        }

                        connection.connect();

                        final HttpClientResponse clientResponse = new HttpClientResponse(connection);
                        try{
                            HttpResponseListener listener = getFromListeners(httpRequest);

                            if(listener != null){
                                listener.handleHttpResponse(clientResponse);
                            }
                            removeFromConnectionsAndListeners(httpRequest);
                        }finally{
                            connection.disconnect();
                        }
                    }catch(final Exception e){
                        connection.disconnect();
                        try{
                            httpResponseListener.failed(e);
                        }finally{
                            removeFromConnectionsAndListeners(httpRequest);
                        }
                    }

                    return null;
                }
            });
        }catch(Exception e){
            try{
                httpResponseListener.failed(e);
            }finally{
                removeFromConnectionsAndListeners(httpRequest);
            }
            return;
        }
    }

    public void cancelHttpRequest(HttpRequest httpRequest){
        HttpResponseListener httpResponseListener = getFromListeners(httpRequest);

        if(httpResponseListener != null){
            httpResponseListener.cancelled();
            removeFromConnectionsAndListeners(httpRequest);
        }
    }

    synchronized void removeFromConnectionsAndListeners(final HttpRequest httpRequest){
        connections.remove(httpRequest);
        listeners.remove(httpRequest);
    }

    synchronized void putIntoConnectionsAndListeners(final HttpRequest httpRequest,
                                                     final HttpResponseListener httpResponseListener, final HttpURLConnection connection){
        connections.put(httpRequest, connection);
        listeners.put(httpRequest, httpResponseListener);
    }

    synchronized HttpResponseListener getFromListeners(HttpRequest httpRequest){
        HttpResponseListener httpResponseListener = listeners.get(httpRequest);
        return httpResponseListener;
    }

    static class HttpClientResponse implements HttpResponse{
        private final HttpURLConnection connection;
        private HttpStatus status;

        public HttpClientResponse(HttpURLConnection connection){
            this.connection = connection;
            try{
                this.status = new HttpStatus(connection.getResponseCode());
            }catch(IOException e){
                this.status = new HttpStatus(-1);
            }
        }

        @Override
        public byte[] getResult(){
            InputStream input = getInputStream();

            // If the response does not contain any content, input will be null.
            if(input == null){
                return StreamUtils.EMPTY_BYTES;
            }

            try{
                return StreamUtils.copyStreamToByteArray(input, connection.getContentLength());
            }catch(IOException e){
                return StreamUtils.EMPTY_BYTES;
            }finally{
                StreamUtils.closeQuietly(input);
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
                return StreamUtils.copyStreamToString(input, connection.getContentLength());
            }catch(IOException e){
                return "";
            }finally{
                StreamUtils.closeQuietly(input);
            }
        }

        @Override
        public InputStream getResultAsStream(){
            return getInputStream();
        }

        @Override
        public HttpStatus getStatus(){
            return status;
        }

        @Override
        public String getHeader(String name){
            return connection.getHeaderField(name);
        }

        @Override
        public Map<String, List<String>> getHeaders(){
            return connection.getHeaderFields();
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
