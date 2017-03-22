Description:
------------

This program is a syntax analyzer that parses Jack programs according to the Jack grammar, producing an XML file that renders the program's structure using marked-up text. It operates on a single .jack file or a directory containing one or more .jack files to produce an .xml output file for each input file.

To compile:
-----------

1. In terminal, cd into the src/ directory.

2. Then, type the command:

javac SyntaxAnalyzer/*.java

This should compile all the files in the package.

If that doesn't work, then compile the files individually:

javac SyntaxAnalyzer/CompilationEngine.java
javac SyntaxAnalyzer/JackAnalyzer.java
javac SyntaxAnalyzer/JackTokenizer.java

JackAnalyzer is the driving class for the program.

To run:
--------

1. While you are still in the src/ directory in terminal, type the command:

java SyntaxAnalyzer/JackAnalyzer somepath/filename.jack

OR

java SyntaxAnalyzer/JackAnalyzer somepath/somedirectory/

- Use the first command if you want to parse a single .jack file. Use the second command if you want to parse multiple .jack files in the same directory.

- If you supply a directory name rather than a file name as an argument, this directory must contain at least one .jack file in its root. E.g. if the path of your .jack file is /Users/yourname/code/files/filename.jack, and you type java SyntaxAnalyzer/JackAnalyzer Users/yourname/code/, this will not work. The program will throw an exception and exit.

- Note that "somepath/" is the path (relative to the directory where you're running java from) where the .jack file or directory containing .jack file(s) is located.

For example, if you are in /Users/yourname/code/YapE-LynnProject10/src, and you type:

java SyntaxAnalyzer/JackAnalyzer ../../Test.jack

Then the program will translate the file /Users/yourname/code/Test.jack.

- If the file that you want to translate is located in the src/ directory (i.e. the same directory that you're running java from), then type:

java SyntaxAnalyzer/JackAnalyzer Test.jack

- If the path and/or file or directory name you have supplied is invalid, the program will throw an exception.

- If the path and filename are valid, then the program will create the output .xml file in the same directory as the original .jack file. If you supplied a directory name instead of a filename and it contains at least one .jack file, then the program will create one .xml file per .jack file in the directory you supplied.

- The text file(s) supplied must be saved using the .jack extension. No other file extensions will be allowed.

Note:
------
- This program will throw an exception and exit if the input .jack file contains any grammatically incorrect constructs.