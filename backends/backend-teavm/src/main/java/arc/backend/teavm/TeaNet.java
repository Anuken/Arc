package arc.backend.teavm;

import arc.*;
import arc.func.*;
import arc.struct.*;
import org.teavm.jso.ajax.*;

import java.io.*;

public class TeaNet extends Net{

    @Override
    public void http(HttpRequest httpRequest, Cons<HttpResponse> success, Cons<Throwable> failure){
        XMLHttpRequest req = XMLHttpRequest.create();
        req.open(httpRequest.method.toString(), httpRequest.url);
        httpRequest.headers.each(req::setRequestHeader);
        req.setOnReadyStateChange(() -> {
            if(req.getReadyState() != XMLHttpRequest.DONE){
                return;
            }

            int statusGroup = req.getStatus() / 100;
            if(statusGroup != 2 && statusGroup != 3){
                failure.get(new IOException("HTTP status: " + req.getStatus() + " " + req.getStatusText()));
            }else{
                success.get(new HttpResponse(){
                    @Override
                    public byte[] getResult(){
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public String getResultAsString(){
                        return req.getResponseText();
                    }

                    @Override
                    public InputStream getResultAsStream(){
                        throw new UnsupportedOperationException("Not implemented");
                    }

                    @Override
                    public HttpStatus getStatus(){
                        return HttpStatus.byCode(req.getStatus());
                    }

                    @Override
                    public String getHeader(String name){
                        return req.getResponseHeader(name);
                    }

                    @Override
                    public ObjectMap<String, Seq<String>> getHeaders(){
                        throw new UnsupportedOperationException("Not implemented");
                    }
                });
            }
        });
        req.send();
    }
}
