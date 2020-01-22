package arc.ecs.injection;

public class SharedInjectionCache extends ThreadLocal<InjectionCache>{
    @Override
    protected InjectionCache initialValue(){
        return new InjectionCache();
    }
}
