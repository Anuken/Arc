package arc.backend.teavm;

import arc.*;
import arc.struct.*;
import arc.func.*;
import org.teavm.jso.ajax.*;
import org.teavm.jso.browser.*;

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
                    public ObjectMap<String, Array<String>> getHeaders(){
                        throw new UnsupportedOperationException("Not implemented");
                    }
                });
            }
        });
        req.send();
    }

    @Override
    public boolean openURI(String URI){
        Window.current().open(URI, "_blank");
        return true;
    }
}
