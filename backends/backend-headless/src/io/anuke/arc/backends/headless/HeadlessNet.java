package io.anuke.arc.backends.headless;

import io.anuke.arc.*;

/**
 * Headless implementation of the {@link Net} API, based on LWJGL implementation
 * @author acoppes
 * @author Jon Renner
 */
public class HeadlessNet extends Net{
    @Override
    public boolean openURI(String URI){
        return false; //unsupported
    }
}
