package arc.backend.teavm.plugin;

import org.teavm.backend.javascript.*;
import org.teavm.jso.impl.*;
import org.teavm.vm.spi.*;

@Before(JSOPlugin.class)
public class ArcTeaVMPlugin implements TeaVMPlugin{
    @Override
    public void install(TeaVMHost host){
        host.add(new OverlayTransformer());

        TeaVMJavaScriptHost jsHost = host.getExtension(TeaVMJavaScriptHost.class);
        jsHost.add(new AssetsCopier());
    }
}
