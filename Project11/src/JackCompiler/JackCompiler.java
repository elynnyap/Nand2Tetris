package JackCompiler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Operates on a given source, where source is either a file name of the form Xxx.jack
 * or a directory name containing one or more files. For each Xxx.jack input file, the compiler
 * creates a JackTokenizer and an output Xxx.vm file. Next, the compiler uses the CompilationEngine,
 * SymbolTable, and VMWriter modules to write the output file.
 */

public class JackCompiler {
    public static void main(String[] args) {

        // Parse command-line argument
        if (args.length != 1) {
            printCommandLineErrorAndExit();
        }

        // examine the supplied command line argument to determine if it's a valid file or directory
        File file = new File(args[0]);
        boolean exists = file.exists(); // Check if file exists
        boolean isDirectory = file.isDirectory(); // Check if it's a directory
        boolean isFile = file.isFile(); // Check if it's a regular file

        // ArrayList to hold all .jack files to be analyzed
        ArrayList<File> filesToProcess = new ArrayList<>();

        // Path for output files
        String outPath = null;

        if (!exists) {
            System.err.println(args[0] + " is not a valid file or directory");
            System.exit(1);
        } else if (isFile && args[0].endsWith(".jack")) { // single .jack file supplied
            filesToProcess.add(file);
            outPath = file.getAbsolutePath();
            outPath = outPath.substring(0, outPath.indexOf(file.getName()));
        } else if (isDirectory) { // directory supplied, scan it for all .jack files
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.getName().endsWith(".jack")) {
                    filesToProcess.add(f);
                }
            }
            outPath = file.getAbsolutePath() + "/";

            // throw error and exit if directory supplied contains no .vm files
            if (filesToProcess.size() == 0) {
                System.err.println("No .jack files to parse in " + args[0]);
                System.exit(1);
            }
        } else {
            printCommandLineErrorAndExit();
        }

        // Process each file
        for (File jackFile : filesToProcess) {

            JackTokenizer tokenizer;

            // Create a JackTokenizer from the input file / directory
            try {
                tokenizer = getTokenizer(jackFile);
            } catch (IOException e) {
                System.err.println("Unable to read file " + jackFile.getName());
                continue;
            }

            // Create output files and prepare for writing
            String inFileName = jackFile.getName().substring(0, jackFile.getName().indexOf(".jack"));
            String outFileName = outPath + inFileName + ".vm";

            PrintWriter outputWriter;

            try {
                outputWriter = new PrintWriter(outFileName);
            } catch (IOException e) {
                System.err.println("Unable to write output file for " + jackFile.getName());
                continue;
            }

            // Use the CompilationEngine to compile the input JackTokenizer into the output file
            CompilationEngine compilationEngine = new CompilationEngine(tokenizer, outputWriter);
            compilationEngine.compileClass();

            // Close resources
            if (outputWriter != null) {
                System.out.println("Created a compiled file " + outFileName);
                outputWriter.close();
            }

        }
    }

    private static JackTokenizer getTokenizer(File file) throws IOException {
        return new JackTokenizer(new Scanner(new FileReader(file)));
    }

    private static void printCommandLineErrorAndExit() {
        System.err.println("Usage: java JackCompiler <filename/directory>");
        System.exit(1);
    }
}
