package arc.util;


import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.command.CommandParamParser;
import arc.util.command.CommandParamSplitter;
import arc.util.command.CommandParams;

/**
 * Parses command syntax.
 */
public class CommandHandler {
    private final ObjectMap<String, Command> commands = new ObjectMap<>();
    private final Seq<Command> orderedCommands = new Seq<>();
    public String prefix = "";

    /**
     * Creates a command handler with a specific command prefix.
     */
    public CommandHandler(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Handles a message with no additional parameters.
     */
    public CommandResponse handleMessage(String message) {
        return handleMessage(message, null);
    }

    /**
     * Handles a message with optional extra parameters. Runs the command if successful.
     *
     * @return a response detailing whether or not the command was handled, and what went wrong, if applicable.
     */
    public CommandResponse handleMessage(String message, Object params) {
        if (message == null || (!message.startsWith(prefix)))
            return new CommandResponse(ResponseType.noCommand, null, null);

        int spaceIndex = message.indexOf(" ");
        String commandstr = spaceIndex != -1 ? message.substring(prefix.length(), spaceIndex) : message.substring(prefix.length());
        Command command = commands.get(commandstr);

        if (command != null) {
            CommandParamSplitter.SplitResponse splitResponse;
            if (spaceIndex == -1) {
                splitResponse = CommandParamSplitter.split("", 0, 0, command.params);
            } else {
                splitResponse = CommandParamSplitter.split(message, spaceIndex + 1, 0, command.params);
            }
            if (splitResponse.many) {
                return new CommandResponse(ResponseType.manyArguments, command, commandstr);
            } else if (splitResponse.few) {
                return new CommandResponse(ResponseType.fewArguments, command, commandstr);
            }

            command.runner.accept(splitResponse.args, params);
            return new CommandResponse(ResponseType.valid, command, commandstr);
        } else {
            return new CommandResponse(ResponseType.unknownCommand, null, commandstr);
        }
    }

    public void removeCommand(String text) {
        Command c = commands.get(text);
        if (c == null) return;
        commands.remove(text);
        orderedCommands.remove(c);
    }

    /**
     * Register a command which handles a zero-sized list of arguments and one parameter.
     */
    public <T> Command register(String text, String description, CommandRunner<T> runner) {
        return register(text, "", description, runner);
    }

    /**
     * Register a command which handles a list of arguments and one handler-specific parameter. <br>
     * argeter syntax is as follows: <br>
     * &lt;mandatory-arg-1&gt; &lt;mandatory-arg-2&gt; ... &lt;mandatory-arg-n&gt; [optional-arg-1] [optional-arg-2] <br>
     * Angle brackets indicate mandatory arguments. Square brackets to indicate optional arguments. <br>
     * All mandatory arguments must come before optional arguments. Arg names must not have spaces in them. <br>
     * You may also use the ... syntax after the arg name to designate that everything after it will not be split into extra arguments.
     * There may only be one such argument, and it must be at the end. For example, the syntax
     * &lt;arg1&gt [arg2...] will require a first argument, and then take any text after that and put it in the second argument, optionally.
     */
    public <T> Command register(String text, String params, String description, CommandRunner<T> runner) {
        //remove previously registered commands
        orderedCommands.remove(c -> c.text.equals(text));

        Command cmd = new Command(text, params, description, runner);
        commands.put(text.toLowerCase(), cmd);
        orderedCommands.add(cmd);
        return cmd;
    }

    public Command register(String text, String description, Cons<String[]> runner) {
        return register(text, description, (args, p) -> runner.get(args));
    }

    public Command register(String text, String params, String description, Cons<String[]> runner) {
        return register(text, params, description, (args, p) -> runner.get(args));
    }

    public Seq<Command> getCommandList() {
        return orderedCommands;
    }

    public enum ResponseType {
        noCommand, unknownCommand, fewArguments, manyArguments, valid
    }

    public interface CommandRunner<T> {
        void accept(String[] args, T parameter);
    }

    public static class Command {
        public final String text;
        public final String paramText;
        public final String description;
        public final CommandParams params;
        final CommandRunner runner;

        public Command(String text, String paramText, String description, CommandRunner runner) {
            this.text = text;
            this.paramText = paramText;
            this.runner = runner;
            this.description = description;
            params = CommandParamParser.parse(paramText);
        }
    }

    public static class CommandParam {
        public final String name;
        public final boolean optional;
        public final boolean variadic;

        public CommandParam(String name, boolean optional, boolean variadic) {
            this.name = name;
            this.optional = optional;
            this.variadic = variadic;
        }
    }

    public static class CommandResponse {
        public final ResponseType type;
        public final Command command;
        public final String runCommand;

        public CommandResponse(ResponseType type, Command command, String runCommand) {
            this.type = type;
            this.command = command;
            this.runCommand = runCommand;
        }
    }
}
