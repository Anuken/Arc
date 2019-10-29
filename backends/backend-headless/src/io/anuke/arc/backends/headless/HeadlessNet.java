package io.anuke.arc.backends.headless;

import io.anuke.arc.Net;
import io.anuke.arc.func.Cons;
import io.anuke.arc.util.NetJavaImpl;

/**
 * Headless implementation of the {@link Net} API, based on LWJGL implementation
 * @author acoppes
 * @author Jon Renner
 */
public class HeadlessNet implements Net{
    NetJavaImpl impl = new NetJavaImpl();

    @Override
    public void http(HttpRequest httpRequest, Cons<HttpResponse> success, Cons<Throwable> failure){
        impl.http(httpRequest, success, failure);
    }

    @Override
    public boolean openURI(String URI){
        return false; //unsupported
    }
}
