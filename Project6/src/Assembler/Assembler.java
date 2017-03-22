package Assembler;

import java.io.*;
import java.util.Scanner;

/**
 * Compilation: javac Assembler.Assembler.java
 * Execution: java Assembler.Assembler filename.asm
 * Dependencies: SymbolTable.java, Code.java, Parser.java
 *
 * Takes a Hack assembly program via a file of .asm format, and produces a text file of .hack format, containing the
 * translated Hack machine code.
 *
 */

public class Assembler {

    public static void main(String[] args) {

        String filename = null;

        // parse command-line arguments
        if (args.length != 1 || !isValidFilename(args[0])) {
            System.err.println("usage: java Assembler.Assembler filename.asm");
            System.exit(1);
        } else {
            filename = args[0];
        }

        // Instantiate parser
        Parser parser = null;
        parser = getParser(filename, parser);

        // Create a symbol table resolving symbols to memory addresses
        SymbolTable symTable = new SymbolTable();

        // First pass through file: build the symbol table
        int currRomAddress = -1; // address into which the current command will be loaded

        while (parser.hasMoreCommands()) {
            parser.advance(); // go to the next line

            parser.stripSpacesAndComments(); // strip out all white spaces, tabs and comments
            if (parser.commandLength() == 0) continue; // don't process unless there's a command

            Parser.Command commandType = parser.commandType();

            if (commandType == Parser.Command.L_COMMAND) {
                symTable.addEntry(parser.symbol(), currRomAddress + 1);
            } else if (commandType == Parser.Command.A_COMMAND || commandType == Parser.Command.C_COMMAND){
                currRomAddress++;
            }

        }

        parser.close();

        // Restart the parser
        parser = getParser(filename, parser);

        // Create output .hack file
        String outFile = filename.substring(0, filename.indexOf(".asm")) + ".hack";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outFile);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        // Second pass through file: read assembly and write binary codes to file
        while (parser.hasMoreCommands()) {
            parser.advance(); // go to the next line

            parser.stripSpacesAndComments(); // strip out all white spaces, tabs and comments
            if (parser.commandLength() == 0) continue; // don't process unless there's a command

            Parser.Command commandType = parser.commandType();

            // write binary code according to the type of command
            switch (commandType) {
                case C_COMMAND:
                    String comp = Code.comp(parser.comp());
                    String dest = Code.dest(parser.dest());
                    String jump = Code.jump(parser.jump());
                    writer.print("111" + comp + dest + jump);
                    break;
                case L_COMMAND:
                    continue; // don't write anything to output for L commands
                case A_COMMAND:
                    String binary = Code.binary(getInt(parser.symbol(), symTable));
                    writer.print("0" + binary);
                    break;
            }

            if (parser.hasMoreCommands()) writer.println(""); // write the binary code for next command on new line
        }

        // close resources
        if (writer != null) {
            writer.close();
        }

        parser.close();

    }

    private static Parser getParser(String filename, Parser parser) {
        try {
            parser = new Parser(new Scanner(new FileReader(new File(filename))));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return parser;
    }

    // checks if filename is acceptable input, i.e. .asm file
    private static boolean isValidFilename(String filename) {
        return filename.endsWith(".asm");
    }

    // if the input is a string representing an int, then returns the int value
    // if the input is a string representing a symbol, then returns the symbol's memory address
    private static int getInt(String input, SymbolTable symbolTable) {
        try {
            return Integer.parseInt(input); // just return the int value if string is a number
        } catch (NumberFormatException e) { // input is a symbol, look it up in table
            if (symbolTable.contains(input)) {
                return symbolTable.getAddress(input);
            } else { // input is a variable declared for the first time, add it to the table
                int address = symbolTable.getNextAddAndIncrement();
                symbolTable.addEntry(input, address);
                return address;
            }
        }
    }
}