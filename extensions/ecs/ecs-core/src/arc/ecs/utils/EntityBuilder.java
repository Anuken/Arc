package arc.ecs.utils;

import arc.ecs.*;

/**
 * Non-reusable entity creation helper for rapid prototyping.
 * <p>
 * Discouraged for use other than rapid prototyping and simple games.
 * Use {@link Mapper} instead, or check out the Fluid Entity
 * interface.
 * <p>
 * Example: new Builder(world)
 * .with(Pos.class, Anim.class)
 * .tag("boss")
 * .player("player1")
 * .group("enemies")
 * .build();
 * @author Daan van Yperen
 * @author Junkdog
 * @see EntityEdit for a list of alternate ways to alter composition and access components.
 */
public class EntityBuilder{

    protected final Base base;
    protected final EntityEdit edit;

    /** Begin building new entity. */
    public EntityBuilder(Base base){
        this.base = base;
        edit = base.createEntity().edit();
    }

    /** Begin building new entity based on archetype. */
    public EntityBuilder(Base base, Archetype archetype){
        this.base = base;
        edit = base.createEntity(archetype).edit();
    }

    /** Add component to entity. */
    public EntityBuilder with(Component component){
        edit.add(component);
        return this;
    }

    /** Add components to entity. */
    public EntityBuilder with(Component component1, Component component2){
        edit.add(component1);
        edit.add(component2);
        return this;
    }

    /** Add components to entity. */
    public EntityBuilder with(Component component1, Component component2, Component component3){
        edit.add(component1);
        edit.add(component2);
        edit.add(component3);
        return this;
    }

    /** Add components to entity. */
    public EntityBuilder with(Component component1, Component component2, Component component3, Component component4){
        edit.add(component1);
        edit.add(component2);
        edit.add(component3);
        edit.add(component4);
        return this;
    }

    /** Add components to entity. */
    public EntityBuilder with(Component component1, Component component2, Component component3, Component component4, Component component5){
        edit.add(component1);
        edit.add(component2);
        edit.add(component3);
        edit.add(component4);
        edit.add(component5);
        return this;
    }

    /** Add components to entity. */
    public EntityBuilder with(Component... components){
        for(Component component : components){
            edit.add(component);
        }
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component> component){
        edit.create(component);
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component> component1, Class<? extends Component> component2){
        edit.create(component1);
        edit.create(component2);
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component> component1, Class<? extends Component> component2, Class<? extends Component> component3){
        edit.create(component1);
        edit.create(component2);
        edit.create(component3);
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component> component1, Class<? extends Component> component2, Class<? extends Component> component3, Class<? extends Component> component4){
        edit.create(component1);
        edit.create(component2);
        edit.create(component3);
        edit.create(component4);
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component> component1, Class<? extends Component> component2, Class<? extends Component> component3, Class<? extends Component> component4, Class<? extends Component> component5){
        edit.create(component1);
        edit.create(component2);
        edit.create(component3);
        edit.create(component4);
        edit.create(component5);
        return this;
    }

    /** Add artemis managed components to entity. */
    public EntityBuilder with(Class<? extends Component>... components){
        for(Class<? extends Component> component : components){
            edit.create(component);
        }
        return this;
    }

    /** Assemble, add to world */
    public Entity build(){
        return edit.getEntity();
    }

    /** Fetch manager or throw RuntimeException if not registered. */
    protected <T extends BaseSystem> T resolveManager(Class<T> type){
        final T teamManager = base.getSystem(type);
        if(teamManager == null){
            throw new RuntimeException("Register " + type.getSimpleName() + " with your base.");
        }
        return teamManager;
    }
}
