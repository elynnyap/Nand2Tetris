package VMTranslator;

import java.io.PrintWriter;

/**
 * Translates VM commands into Hack assembly code.
 */

public class CodeWriter {

    private PrintWriter writer;
    private String fileName; // filename of current VM file being translated
    private int logicalFlag; // number to append to symbols for logical operations

    // gets ready to write into output file
    public CodeWriter(PrintWriter writer) {
        this.writer = writer;
        logicalFlag = 0;
    }

    // informs the code writer that the translation of a new VM file is started
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // writes the assembly code that is the translation of the given arithmetic command
    public void writeArithmetic(String command) {
        switch (command) {
            case "add":
                printBinaryArthTemplate();
                writer.println("M=M+D");
                break;
            case "sub":
                printBinaryArthTemplate();
                writer.println("M=M-D");
                break;
            case "neg":
                printUnaryArthTemplate();
                writer.println("M=-M");
                break;
            case "eq":
                printLogicalTemplate("JEQ");
                break;
            case "gt":
                printLogicalTemplate("JGT");
                break;
            case "lt":
                printLogicalTemplate("JLT");
                break;
            case "and":
                printBinaryArthTemplate();
                writer.println("M=M&D");
                break;
            case "or":
                printBinaryArthTemplate();
                writer.println("M=M|D");
                break;
            case "not":
                printUnaryArthTemplate();
                writer.println("M=!M");
                break;
        }
    }

    private void printUnaryArthTemplate() {
        writer.println("@SP");
        writer.println("A=M-1");
    }

    private void printBinaryArthTemplate() {
        printStoreTopStackAndDec();
        writer.println("A=A-1");
    }

    private void printLogicalTemplate(String operator) {
        printStoreTopStackAndDec();
        writer.println("A=A-1");
        writer.println("D=M-D");
        writer.println("@" + fileName + ".TRUE" + logicalFlag);
        writer.println("D;" + operator);
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("@" + fileName + ".CONTINUE" + logicalFlag);
        writer.println("0;JMP");
        writer.println("(" + fileName + ".TRUE" + logicalFlag + ")");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=-1");
        writer.println("(" + fileName + ".CONTINUE" + logicalFlag + ")");
        logicalFlag++;
    }

    // writes the assembly code that is the translation of the given command,
    // where command is either C_PUSH or C_POP
    public void writePushPop(Parser.Command command, String segment, int index) {

        switch (segment) {
            case "constant":
                if (command == Parser.Command.C_PUSH) {
                    writer.println("@" + index);
                    writer.println("D=A");
                    printPushAndInc();
                }
                break;
            case "local":
                if (command == Parser.Command.C_PUSH) {
                    printPushBaseAdd(index, "@LCL");
                } else if (command == Parser.Command.C_POP) {
                    printPopBaseAdd(index, "@LCL");
                }
                break;
            case "this":
                if (command == Parser.Command.C_PUSH) {
                    printPushBaseAdd(index, "@THIS");
                } else if (command == Parser.Command.C_POP) {
                    printPopBaseAdd(index, "@THIS");
                }
                break;
            case "that":
                if (command == Parser.Command.C_PUSH) {
                    printPushBaseAdd(index, "@THAT");
                } else if (command == Parser.Command.C_POP) {
                    printPopBaseAdd(index, "@THAT");
                }
                break;
            case "argument":
                if (command == Parser.Command.C_PUSH) {
                    printPushBaseAdd(index, "@ARG");
                } else if (command == Parser.Command.C_POP) {
                    printPopBaseAdd(index, "@ARG");
                }
                break;
            case "static":
                String asmRepresentation = "@" + fileName + "." + index;
                if (command == Parser.Command.C_PUSH) {
                    printPushMappedAdd(asmRepresentation);
                } else if (command == Parser.Command.C_POP) {
                    printPopMappedAdd(asmRepresentation);
                }
            break;
            case "pointer":
                if (command == Parser.Command.C_PUSH) {
                    if (index == 0) {
                        printPushMappedAdd("@THIS");
                    } else if (index == 1) {
                        printPushMappedAdd("@THAT");
                    }
                } else if (command == Parser.Command.C_POP) {
                    if (index == 0) {
                        printPopMappedAdd("@THIS");
                    } else if (index == 1) {
                        printPopMappedAdd("@THAT");
                    }
                }
                break;
            case "temp":
                if (command == Parser.Command.C_PUSH) {
                    int location = 5 + index;
                    printPushMappedAdd("@" + location);
                } else if (command == Parser.Command.C_POP) {
                    int location = 5 + index;
                    printPopMappedAdd("@" + location);
                }
                break;
        }

    }

    // pops from the stack for symbols that point to base addresses of virtual segments
    private void printPopBaseAdd(int index, String x) {
        writer.println(x);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("D=D+A");
        writer.println("@R13");
        writer.println("M=D");
        printStoreTopStackAndDec();
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    // pushes onto the stack for symbols that point to base addresses of virtual segments
    private void printPushBaseAdd(int index, String x) {
        writer.println(x);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("A=D+A");
        writer.println("D=M");
        printPushAndInc();
    }

    // pops from the stack for segments mapped directly onto RAM
    private void printPopMappedAdd(String location) {
        printStoreTopStackAndDec();
        writer.println(location);
        writer.println("M=D");
    }

    // pushes onto the stack for segments mapped directly onto RAM
    private void printPushMappedAdd(String location) {
        writer.println(location);
        writer.println("D=M");
        printPushAndInc();
    }

    // pushes the value in D onto the stack and increments stack pointer
    private void printPushAndInc() {
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
        writer.println("@SP");
        writer.println("M=M+1");
    }

    // gets the value at the top of the stack, stores it in D, and decrements stack pointer
    private void printStoreTopStackAndDec() {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
    }

    public void close() {
        writer.close();
    }
}
