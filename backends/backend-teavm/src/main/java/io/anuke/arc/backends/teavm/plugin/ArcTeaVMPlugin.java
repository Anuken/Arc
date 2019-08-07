package io.anuke.arc.backends.teavm.plugin;

import org.teavm.backend.javascript.TeaVMJavaScriptHost;
import org.teavm.jso.impl.JSOPlugin;
import org.teavm.vm.spi.Before;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

@Before(JSOPlugin.class)
public class ArcTeaVMPlugin implements TeaVMPlugin {
    @Override
    public void install(TeaVMHost host) {
        host.add(new OverlayTransformer());

        TeaVMJavaScriptHost jsHost = host.getExtension(TeaVMJavaScriptHost.class);
        jsHost.add(new AssetsCopier());
    }
}
