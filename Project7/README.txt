Description:
------------

This program translates a single .vm file, or a collection of .vm files contained in the same directory, into a single .asm file using Hack assembly language. This VM translator implements only the stack arithmetic and memory access commands of the VM language.

To compile:
-----------

1. In terminal, cd into the src/ directory.

2. Then, type the command:

javac VMTranslator/*.java

This should compile all the files in the package.

If that doesn't work, then compile the files individually:

javac VMTranslator/CodeWriter.java
javac VMTranslator/Parser.java
javac VMTranslator/VMTranslator.java

To run:
--------

1. While you are still in the src/ directory in terminal, type the command:

java VMTranslator/VMTranslator somepath/filename.vm

OR

java VMTranslator/VMTranslator somepath/somedirectory/

- Use the first command if you want to translate a single .vm file into an output .asm file. Use the second command if you want to translate multiple .vm files in the same directory into an output .asm file.

- If you supply a directory name rather than a file name as an argument, this directory must contain at least one .vm file in its root. E.g. if the path of your .vm file is /Users/yourname/code/files/filename.vm, and you type java VMTranslator/VMTranslator Users/yourname/code/, this will not work. The program will throw an exception and exit.

- Note that "somepath/" is the path (relative to the directory where you're running java from) where the .vm file or directory containing .vm file(s) is located.

For example, if you are in /Users/yourname/code/YapE-LynnProject6/src, and you type:

java VMTranslator/VMTranslator ../../Test.vm

Then the program will translate the file /Users/yourname/code/Test.vm.

- If the file that you want to translate is located in the src/ directory (i.e. the same directory that you're running java from), then type:

java Assembler/Assembler Test.vm

- If the path and/or file or directory name you have supplied is invalid, the program will throw an exception.

- If the path and filename are valid, then the program will create a .asm file in the same directory that the original .vm file is located in. If you supplied a directory name instead of a filename and it contains at least one .vm file, then the program will create a .asm file in the directory you supplied.

- The text file(s) supplied must be saved using the .vm extension. No other file extensions will be allowed.

Note:
------
- This program does not fully validate the .vm file to ensure that it only contains legal VM language commands, i.e. it assumes that the .vm file you are supplying is syntactically correct.