package arc.util.command;

import arc.util.*;

public class CommandParams{
    public final CommandHandler.CommandParam[] params;
    public final int variadicIndex;
    public final int requiredAmount;

    public CommandParams(CommandHandler.CommandParam[] params){
        this.params = params;
        variadicIndex = Structs.indexOf(params, it -> it.variadic);
        requiredAmount = Structs.count(params, it -> !it.optional);


    }
}
