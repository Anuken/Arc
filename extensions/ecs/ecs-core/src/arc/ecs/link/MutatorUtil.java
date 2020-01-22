package arc.ecs.link;

final class MutatorUtil{
    private MutatorUtil(){
    }

    static <T> T getGeneratedMutator(LinkSite linkSite){
        Class[] possibleMutators = linkSite.field.getDeclaringClass().getDeclaredClasses();
        String mutatorName = "Mutator_" + linkSite.field.getName();
        for(Class possibleMutator : possibleMutators){
            if(mutatorName.equals(possibleMutator.getSimpleName())){
                try{
                    return (T)possibleMutator.newInstance();
                }catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }

        return null;
    }

}
