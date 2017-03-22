package JackCompiler;

import java.io.PrintWriter;

/**
 * Emits VM commands into a file, using the VM command syntax.
 */
public class VMWriter {

    private PrintWriter outputWriter; // output file

    // Creates a new file and prepares it for writing
    public VMWriter(PrintWriter writer) {
        outputWriter = writer;
    }

    // Enumerates all possible segments
    public enum Segment {
        CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    // Enumerates all possible commands
    public enum Command {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    // Writes a VM push command
    public void writePush(Segment segment, int index) {
        outputWriter.println("push " + segment + " " + index);
    }

    // Writes a VM pop command
    public void writePop(Segment segment, int index) {
        outputWriter.println("pop " + segment + " " + index);
    }

    // Writes a VM arithmetic command
    public void writeArithmetic(Command command) {
        outputWriter.println(command);
    }

    // Writes a VM label command
    public void writeLabel(String label) {
        outputWriter.println("label " + label);
    }

    // Writes a VM goto command
    public void writeGoto(String label) {
        outputWriter.println("goto " + label);
    }

    // Writes a VM if-goto command
    public void writeIf(String label) {
        outputWriter.println("if-goto " + label);
    }

    // Writes a VM call command
    public void writeCall(String name, int nArgs) {
        outputWriter.println("call " + name + " " + nArgs);
    }

    // Writes a VM function command
    public void writeFunction(String name, int nLocals) {
        outputWriter.println("function " + name + " " + nLocals);
    }

    // Writes a VM return command
    public void writeReturn() {
        outputWriter.println("return");
    }

    // Closes the output file
    public void close() {
        outputWriter.close();
    }
}
