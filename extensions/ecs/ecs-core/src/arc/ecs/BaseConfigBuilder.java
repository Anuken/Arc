package arc.ecs;

import arc.ecs.annotations.*;
import arc.ecs.injection.*;
import arc.ecs.utils.*;
import arc.struct.Array;

import java.lang.reflect.*;

/**
 * World builder.
 * <p>
 * Allows convenient var-arg addition of systems, managers. Supports plugins.
 * @author Daan van Yperen
 * @see BaseConfig
 */
public class BaseConfigBuilder{
    private Bag<ConfigurationElement<? extends BaseSystem>> systems;
    private Bag<ConfigurationElement<? extends FieldResolver>> fieldResolvers;
    private Bag<ConfigurationElement<? extends ArtemisPlugin>> plugins;
    private boolean alwaysDelayComponentRemoval = false;

    private ArtemisPlugin activePlugin;
    private final InjectionCache cache;
    private SystemInvoker invocationStrategy;

    public BaseConfigBuilder(){
        reset();
        cache = InjectionCache.sharedCache.get();
    }

    /**
     * Assemble world with systems.
     * <p/>
     * Deprecated: World Configuration
     */
    public BaseConfig build(){
        appendPlugins();
        final BaseConfig config = new BaseConfig();
        registerSystems(config);
        registerFieldResolvers(config);
        registerInvocationStrategies(config);
        config.setAlwaysDelayComponentRemoval(alwaysDelayComponentRemoval);
        reset();
        return config;
    }

    private void registerInvocationStrategies(BaseConfig config){
        if(invocationStrategy != null){
            config.setInvocationStrategy(invocationStrategy);
        }
    }

    /**
     * Append plugin configurations.
     * Supports plugins registering plugins.
     */
    private void appendPlugins(){
        int i = 0;

        while(i < plugins.size()){
            activePlugin = plugins.get(i).item;
            activePlugin.setup(this);
            i++;
        }
        activePlugin = null;
    }

    /**
     * add custom field handler with resolvers.
     */
    protected void registerFieldResolvers(BaseConfig config){

        if(fieldResolvers.size() > 0){
            fieldResolvers.sort();
            // instance default field handler
            final FieldHandler fieldHandler = new FieldHandler(InjectionCache.sharedCache.get());

            for(ConfigurationElement<? extends FieldResolver> configurationElement : fieldResolvers){
                fieldHandler.addFieldResolver(configurationElement.item);
            }

            config.setInjector(new CachedInjector().setFieldHandler(fieldHandler));
        }
    }

    /**
     * add systems to config.
     */
    private void registerSystems(BaseConfig config){
        systems.sort();
        for(ConfigurationElement<? extends BaseSystem> configurationElement : systems){
            config.setSystem(configurationElement.item);
        }
    }

    /**
     * Reset builder
     */
    private void reset(){
        invocationStrategy = null;
        systems = new Bag<>();
        fieldResolvers = new Bag<>();
        plugins = new Bag<>();
        alwaysDelayComponentRemoval = false;
    }

    /**
     * Delay component removal until all subscriptions have been notified.
     * <p>
     * Extends the lifecycle of ALL component types, ensuring removed instances are retrievable until
     * all {@link EntitySubscription.SubscriptionListener#removed(IntBag) listeners} have been notified - regardless
     * of removal method.
     * <p>
     * Has a slight performance cost.
     * @param value When {@code true}, component removal for all components will be delayed until all subscriptions
     * have been notified. When {@code false}, only components with {@code @DelayedComponentRemoval}
     * will be delayed. Components without the annotation will not be retrievable in listeners.
     */
    public BaseConfigBuilder alwaysDelayComponentRemoval(boolean value){
        this.alwaysDelayComponentRemoval = value;
        return this;
    }

    /**
     * Add field resolver.
     * @return this
     */
    public BaseConfigBuilder register(FieldResolver... fieldResolvers){
        for(FieldResolver fieldResolver : fieldResolvers){
            this.fieldResolvers.add(ConfigurationElement.of(fieldResolver));
        }
        return this;
    }

    /**
     * Add system invocation strategy.
     * @param strategy strategy to invoke.
     * @return this
     */
    public BaseConfigBuilder register(SystemInvoker strategy){
        this.invocationStrategy = strategy;
        return this;
    }

    /**
     * Specify dependency on systems/plugins.
     * <p/>
     * Managers track priority separate from system priority, and are always added before systems.
     * <p>
     * Artemis will consider abstract plugin dependencies fulfilled when a concrete subclass has been registered
     * beforehand.
     * @param types required systems.
     * @return this
     */
    public final BaseConfigBuilder dependsOn(Class... types){
        return dependsOn(Priority.NORMAL, types);
    }

    /**
     * Specify dependency on systems/plugins.
     * <p/>
     * @param types required systems.
     * @param priority Higher priority are registered first. Not supported for plugins.
     * @return this
     * @throws BaseConfigException if unsupported classes are passed or plugins are given a priority.
     */
    @SuppressWarnings("unchecked")
    public final BaseConfigBuilder dependsOn(int priority, Class... types){
        for(Class type : types){
            try{
                if(cache.getFieldClassType(type) == ClassType.SYSTEM){
                    dependsOnSystem(priority, type);
                }else{
                    if(ArtemisPlugin.class.isAssignableFrom(type)){
                        if(priority != Priority.NORMAL){
                            throw new BaseConfigException("Priority not supported on plugins.");
                        }
                        dependsOnPlugin(type);
                    }else{
                        throw new BaseConfigException("Unsupported type. Only supports systems.");
                    }
                }
            }catch(Exception e){
                throw new BaseConfigException("Unable to instance " + type + " via reflection.", e);
            }
        }
        return this;
    }

    protected void dependsOnSystem(int priority, Class<? extends BaseSystem> type) throws Exception{
        if(!containsType(systems, type)){
            this.systems.add(ConfigurationElement.of(type.newInstance(), priority));
        }
    }

    private void dependsOnPlugin(Class<? extends ArtemisPlugin> type) throws Exception{

        if(Modifier.isAbstract(type.getModifiers())){
            if(!anyAssignableTo(plugins, type)){
                throw new BaseConfigException("Implementation of " + type + " expected but not found. Did you forget to include a plugin? (for example: logging-libgdx for logging-api)");
            }
        }else{
            if(!containsType(plugins, type)){
                this.plugins.add(ConfigurationElement.of(type.newInstance()));
            }
        }
    }

    /**
     * Register active system(s).
     * Only one instance of each class is allowed.
     * Use {@link #dependsOn} from within plugins whenever possible.
     * @param systems systems to add, order is preserved.
     * @param priority priority of added systems, higher priority are added before lower priority.
     * @return this
     * @throws BaseConfigException if registering the same class twice.
     */
    public BaseConfigBuilder with(int priority, BaseSystem... systems){
        addSystems(priority, systems);
        return this;
    }

    /**
     * Register active system(s).
     * Only one instance of each class is allowed.
     * Use {@link #dependsOn} from within plugins whenever possible.
     * @param systems systems to add, order is preserved.
     * @return this
     * @throws BaseConfigException if registering the same class twice.
     */
    public BaseConfigBuilder with(BaseSystem... systems){
        addSystems(Priority.NORMAL, systems);
        return this;
    }

    public BaseConfigBuilder with(Array<BaseSystem> systems){
        addSystems(Priority.NORMAL, systems.toArray(BaseSystem.class));
        return this;
    }


    /**
     * Add plugins to world.
     * <p/>
     * Upon build plugins will be called to register dependencies.
     * <p/>
     * Only one instance of each class is allowed.
     * Use {@link #dependsOn} from within plugins whenever possible.
     * @param plugins Plugins to add.
     * @return this
     * @throws BaseConfigException if type is added more than once.
     */
    public BaseConfigBuilder with(ArtemisPlugin... plugins){
        addPlugins(plugins);
        return this;
    }

    /**
     * helper to queue systems for registration.
     */
    private void addSystems(int priority, BaseSystem[] systems){
        for(BaseSystem system : systems){

            if(containsType(this.systems, system.getClass())){
                throw new BaseConfigException("System of type " + system.getClass() + " registered twice. Only once allowed.");
            }

            this.systems.add(new ConfigurationElement<>(system, priority));
        }
    }

    /**
     * Check if bag of registerables contains any of passed type.
     * @param items bag of registerables.
     * @param type type to check for.
     * @return {@code true} if found {@code false} if none.
     */
    @SuppressWarnings("unchecked")
    private boolean containsType(Bag items, Class type){
        for(ConfigurationElement<?> registration : (Bag<ConfigurationElement<?>>)items){
            if(registration.itemType == type){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if bag of registerables contains any of passed type.
     * @param items bag of possible subtypes.
     * @param type supertype to check for.
     * @return {@code true} if found {@code false} if none.
     */
    @SuppressWarnings("unchecked")
    private boolean anyAssignableTo(Bag items, Class type){
        for(ConfigurationElement<?> registration : (Bag<ConfigurationElement<?>>)items){
            if(type.isAssignableFrom(registration.itemType)){
                return true;
            }
        }
        return false;
    }

    /**
     * Add new plugins.
     */
    private void addPlugins(ArtemisPlugin[] plugins){
        for(ArtemisPlugin plugin : plugins){

            if(containsType(this.plugins, plugin.getClass())){
                throw new BaseConfigException("Plugin of type " + plugin.getClass() + " registered twice. Only once allowed.");
            }

            this.plugins.add(ConfigurationElement.of(plugin));
        }
    }

    /**
     * Guideline constants for priority, higher values has more priority. Will probably change.
     */
    @UnstableApi
    public static abstract class Priority{
        public static final int LOWEST = Integer.MIN_VALUE;
        public static final int LOW = -10000;
        public static final int OPERATIONS = -1000;
        public static final int NORMAL = 0;
        public static final int HIGH = 10000;
        public static final int HIGHEST = Integer.MAX_VALUE;
    }

    /**
     * World configuration failed.
     * @author Daan van Yperen
     */
    public static class BaseConfigException extends RuntimeException{
        public BaseConfigException(String msg){
            super(msg);
        }

        public BaseConfigException(String msg, Throwable e){
            super(msg, e);
        }
    }
}
