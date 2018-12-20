package io.anuke.arc.utils;


import io.anuke.arc.collection.Array;
import io.anuke.arc.collection.ObjectMap;
import io.anuke.arc.function.Consumer;

/** Parses command syntax. */
public class CommandHandler{
    private final ObjectMap<String, Command> commands = new ObjectMap<>();
    private final Array<Command> orderedCommands = new Array<>();
    private final String prefix;

    public CommandHandler(String prefix){
        this.prefix = prefix;
    }

    public Response handleMessage(String message){
        if(message == null || (!message.startsWith(prefix)))
            return new Response(ResponseType.noCommand, null, null);

        message = message.substring(prefix.length());

        String commandstr = message.contains(" ") ? message.substring(0, message.indexOf(" ")) : message;
        String argstr = message.contains(" ") ? message.substring(commandstr.length() + 1) : "";

        Array<String> result = new Array<>();

        Command command = commands.get(commandstr);

        if(command != null){
            int index = 0;
            boolean satisfied = false;

            while(true){
                if(index >= command.params.length && !argstr.isEmpty()){
                    return new Response(ResponseType.manyArguments, command, commandstr);
                }else if(argstr.isEmpty()) break;

                if(command.params[index].optional || index >= command.params.length - 1 || command.params[index + 1].optional){
                    satisfied = true;
                }

                if(command.params[index].variadic){
                    result.add(argstr);
                    break;
                }

                int next = argstr.indexOf(" ");
                if(next == -1){
                    if(!satisfied){
                        return new Response(ResponseType.fewArguments, command, commandstr);
                    }
                    result.add(argstr);
                    break;
                }else{
                    String arg = argstr.substring(0, next);
                    argstr = argstr.substring(arg.length() + 1);
                    result.add(arg);
                }

                index++;
            }

            if(!satisfied && command.params.length > 0 && !command.params[0].optional){
                return new Response(ResponseType.fewArguments, command, commandstr);
            }

            command.runner.accept(result.toArray(String.class));

            return new Response(ResponseType.valid, command, commandstr);
        }else{
            return new Response(ResponseType.unknownCommand, null, commandstr);
        }
    }

    public Command register(String text, String description, Consumer<String[]> runner){
        Command cmd = new Command(text, "", description, runner);
        commands.put(text.toLowerCase(), cmd);
        orderedCommands.add(cmd);
        return cmd;
    }

    public Command register(String text, String params, String description, Consumer<String[]> runner){
        Command cmd = new Command(text, params, description, runner);
        commands.put(text.toLowerCase(), cmd);
        orderedCommands.add(cmd);
        return cmd;
    }

    public Iterable<Command> getCommandList(){
        return orderedCommands;
    }

    public enum ResponseType{
        noCommand, unknownCommand, fewArguments, manyArguments, valid
    }

    public static class Command{
        public final String text;
        public final String paramText;
        public final String description;
        public final CommandParam[] params;
        public final Consumer<String[]> runner;

        public Command(String text, String paramText, String description, Consumer<String[]> runner){
            this.text = text;
            this.paramText = paramText;
            this.runner = runner;
            this.description = description;

            String[] psplit = paramText.split(" ");
            if(paramText.length() == 0){
                params = new CommandParam[0];
            }else{
                params = new CommandParam[psplit.length];

                boolean hadOptional = false;

                for(int i = 0; i < params.length; i++){
                    String param = psplit[i];

                    if(param.length() <= 2) throw new IllegalArgumentException("Malformed param '" + param + "'");

                    char l = param.charAt(0), r = param.charAt(param.length() - 1);
                    boolean optional, variadic = false;

                    if(l == '<' && r == '>'){
                        if(hadOptional)
                            throw new IllegalArgumentException("Can't have non-optional param after optional param!");
                        optional = false;
                    }else if(l == '[' && r == ']'){
                        optional = true;
                    }else{
                        throw new IllegalArgumentException("Malformed param '" + param + "'");
                    }

                    if(optional) hadOptional = true;

                    String fname = param.substring(1, param.length() - 1);
                    if(fname.endsWith("...")){
                        if(i != params.length - 1)
                            throw new IllegalArgumentException("A variadic parameter should be the last parameter!");

                        fname = fname.substring(0, fname.length() - 3);
                        variadic = true;
                    }

                    params[i] = new CommandParam(fname, optional, variadic);

                }
            }
        }
    }

    public static class CommandParam{
        public final String name;
        public final boolean optional;
        public final boolean variadic;

        public CommandParam(String name, boolean optional, boolean variadic){
            this.name = name;
            this.optional = optional;
            this.variadic = variadic;
        }
    }

    public static class Response{
        public final ResponseType type;
        public final Command command;
        public final String runCommand;

        public Response(ResponseType type, Command command, String runCommand){
            this.type = type;
            this.command = command;
            this.runCommand = runCommand;
        }
    }
}
