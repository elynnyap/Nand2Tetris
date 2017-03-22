package Assembler;

import java.util.Scanner;

/**
 * Reads an assembly language command, parses it, and provides convenient access to the command's components.
 * Used in Assembler.java.
 */
public class Parser {

    private Scanner in;
    private String command; // the current command

    public Parser(Scanner in) {
        this.in = in;
    }

    public enum Command {
        A_COMMAND, C_COMMAND, L_COMMAND
    }

    // checks if file has another line to process
    public boolean hasMoreCommands() {
        return in.hasNextLine();
    }

    // moves to the next line to process
    public void advance() {
        this.command = in.nextLine();
    }

    // removes all instances of spaces, tabs and comments
    public void stripSpacesAndComments() {
        if (command.contains("//")) {
            command = command.substring(0, command.indexOf("//"));
        }
        command = command.replace(" ", "");
        command = command.replace("\t", "");
    }

    public int commandLength() {
        return command.length();
    }

    // determines the type of command
    public Command commandType() {
        if (command.startsWith("@")) {
            return Command.A_COMMAND;
        } else if (command.startsWith("(")) {
            return Command.L_COMMAND;
        } else {
            return Command.C_COMMAND;
        }
    }

    // returns the symbol or decimal XXX of the current command @XXX or (XXX)
    public String symbol() {
        if (command.startsWith("@")) {
            return command.substring(1);
        } else if (command.startsWith("(")) {
            return command.substring(1, command.indexOf(")"));
        } else {
            return null;
        }
    }

    // returns the dest mnemonic in the current C-command
    // returns an empty string if dest field is empty
    public String dest() {
        if (command.contains("=")) {
            return command.split("=")[0];
        } else {
            return "";
        }
    }

    // returns the comp mnemonic in the current C-command
    public String comp() {
        if (command.contains("=")) {
            return command.split("=|;")[1];
        } else {
            return command.split(";")[0];
        }
    }

    // returns the jump mnemonic in the current C-command
    // returns an empty string if jump field is empty
    public String jump() {
        if (command.contains(";")) {
            return command.split(";")[1];
        } else {
            return "";
        }
    }

    // closes the scanner
    public void close() {
        if (in != null) in.close();
    }

}
