package VMTranslator;

import java.util.Scanner;

/**
 * Handles parsing of a single .vm file, and encapsulates access to the input code.
 * Reads VM commands, parses them, and provides convenient access to their components.
 * Also removes white space and comments.
 */
public class Parser {

    private Scanner in;
    private String command; // the current command
    private String filename; // filename of the .vm file being parsed

    private static final String[] ARITHMETIC_CMDS = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};

    public Parser(Scanner in) {
        this.in = in;
    }

    // all possible vm commands
    public enum Command {
        C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
    }

    // checks if file has another line to process
    public boolean hasMoreCommands() {
        return in.hasNextLine();
    }

    // removes all instances of new lines, tabs and comments
    public void stripComments() {
        if (command.contains("//")) {
            command = command.substring(0, command.indexOf("//"));
        }
        command = command.replace("\n", "");
        command = command.replace("\t", "");
    }

    // returns length of command
    public int commandLength() {
        return command.length();
    }

    // reads the next command from the input
    public void advance() {
        this.command = in.nextLine();
    }

    // returns the type of the current VM command
    public Command commandType() {
        if (command.startsWith("push")) {
            return Command.C_PUSH;
        } else if (command.startsWith("pop")) {
            return Command.C_POP;
        } else if (isArithmeticCmd()) {
            return Command.C_ARITHMETIC;
        } else if (command.startsWith("label")) {
            return Command.C_LABEL;
        } else if (command.startsWith("goto")) {
            return Command.C_GOTO;
        } else if (command.startsWith("if-goto")) {
            return Command.C_IF;
        } else if (command.startsWith("function")) {
            return Command.C_FUNCTION;
        } else if (command.startsWith("call")) {
            return Command.C_CALL;
        } else if (command.startsWith("return")) {
            return Command.C_RETURN;
        } else {
            return null;
        }
    }

    // returns the command
    public String command() {
        return command.split("\\s+")[0];
    }

    // returns the first argument of the current command
    public String arg1() {
        return command.split("\\s+")[1];
    }

    // returns the second argument of the current command
    // called only if current command is C_PUSH, C_POP, C_FUNCTION or C_CALL
    public String arg2() {
        return command.split("\\s+")[2];
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    // closes the scanner
    public void close() {
        in.close();
    }

    // checks if command is arithmetic
    private boolean isArithmeticCmd() {
        for (String cmd : ARITHMETIC_CMDS) {
            if (command.startsWith(cmd)) return true;
        }
        return false;
    }
}
