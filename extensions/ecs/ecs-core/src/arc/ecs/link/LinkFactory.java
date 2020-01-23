package arc.ecs.link;

import arc.ecs.*;
import arc.ecs.annotations.*;
import arc.ecs.utils.*;

import java.lang.reflect.*;

import static arc.ecs.annotations.LinkPolicy.Policy.SKIP;
import static arc.ecs.utils.ArReflect.isGenericType;

class LinkFactory{
    private static final int NULL_REFERENCE = 0;
    private static final int SINGLE_REFERENCE = 1;
    private static final int MULTI_REFERENCE = 2;

    private final Bag<LinkSite> links = new Bag<>();
    private final Base base;

    private final ReflexiveMutators reflexiveMutators;

    public LinkFactory(Base base){
        this.base = base;
        reflexiveMutators = new ReflexiveMutators(base);
    }

    static int getReferenceTypeId(Field f){
        Class type = f.getType();
        if(Entity.class == type)
            return SINGLE_REFERENCE;
        if(isGenericType(f, Bag.class, Entity.class))
            return MULTI_REFERENCE;

        boolean explicitEntityId = f.getDeclaredAnnotation(EntityId.class) != null;
        if(int.class == type && explicitEntityId)
            return SINGLE_REFERENCE;
        if(IntBag.class == type && explicitEntityId)
            return MULTI_REFERENCE;

        return NULL_REFERENCE;
    }

    Bag<LinkSite> create(ComponentType ct){
        Class<?> type = ct.getType();
        Field[] fields = type.getDeclaredFields();

        links.clear();
        for(Field f : fields){
            int referenceTypeId = getReferenceTypeId(f);
            if(referenceTypeId != NULL_REFERENCE && (SKIP != getPolicy(f))){
                if(SINGLE_REFERENCE == referenceTypeId){
                    UniLinkSite ls = new UniLinkSite(base, ct, f);
                    if(!configureMutator(ls))
                        reflexiveMutators.withMutator(ls);

                    links.add(ls);
                }else if(MULTI_REFERENCE == referenceTypeId){
                    MultiLinkSite ls = new MultiLinkSite(base, ct, f);
                    if(!configureMutator(ls))
                        reflexiveMutators.withMutator(ls);

                    links.add(ls);
                }
            }
        }

        return links;
    }

    static LinkPolicy.Policy getPolicy(Field f){
        LinkPolicy lp = f.getDeclaredAnnotation(LinkPolicy.class);
        return lp == null ? null : lp.value();
    }

    private boolean configureMutator(UniLinkSite linkSite){
        UniFieldMutator mutator = MutatorUtil.getGeneratedMutator(linkSite);
        if(mutator != null){
            mutator.setBase(base);
            linkSite.fieldMutator = mutator;
            return true;
        }else{
            return false;
        }
    }

    private boolean configureMutator(MultiLinkSite linkSite){
        MultiFieldMutator mutator = MutatorUtil.getGeneratedMutator(linkSite);
        if(mutator != null){
            mutator.setBase(base);
            linkSite.fieldMutator = mutator;
            return true;
        }else{
            return false;
        }
    }

    static class ReflexiveMutators{
        final EntityFieldMutator entityField;
        final IntFieldMutator intField;
        final IntBagFieldMutator intBagField;
        final EntityBagFieldMutator entityBagField;

        public ReflexiveMutators(Base base){
            entityField = new EntityFieldMutator();
            entityField.setBase(base);

            intField = new IntFieldMutator();
            intField.setBase(base);

            intBagField = new IntBagFieldMutator();
            intBagField.setBase(base);

            entityBagField = new EntityBagFieldMutator();
            entityBagField.setBase(base);
        }

        UniLinkSite withMutator(UniLinkSite linkSite){
            if(linkSite.fieldMutator != null)
                return linkSite;

            Class type = linkSite.field.getType();
            if(Entity.class == type){
                linkSite.fieldMutator = entityField;
            }else if(int.class == type){
                linkSite.fieldMutator = intField;
            }else{
                throw new RuntimeException("unexpected '" + type + "', on " + linkSite.type);
            }

            return linkSite;
        }

        MultiLinkSite withMutator(MultiLinkSite linkSite){
            if(linkSite.fieldMutator != null)
                return linkSite;

            Class type = linkSite.field.getType();
            if(IntBag.class == type){
                linkSite.fieldMutator = intBagField;
            }else if(Bag.class == type){
                linkSite.fieldMutator = entityBagField;
            }else{
                throw new RuntimeException("unexpected '" + type + "', on " + linkSite.type);
            }

            return linkSite;
        }
    }
}
