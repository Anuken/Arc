package arc.ecs.fluid.generator.collect;

import arc.ecs.*;
import arc.ecs.fluid.generator.strategy.e.*;

import java.net.*;
import java.util.*;

/**
 * Collector for classes on classpath.
 * @author Daan van Yperen
 */
public abstract class AbstractClassCollectStrategy{

    private final Set<URL> urls;

    /**
     * @param urls locations to search.
     */
    AbstractClassCollectStrategy(Set<URL> urls){
        this.urls = urls;
    }

    /**
     * Collect all components within a set of URLs
     * @return Set of all components on classloader.
     */
    public abstract Collection<Class<? extends Component>> allComponents();

    /**
     * Collect all components within a set of URLs
     * @return Set of all components on classloader.
     */
    public abstract Collection<Class<? extends FieldProxyStrategy>> allFieldProxyStrategies();
}
