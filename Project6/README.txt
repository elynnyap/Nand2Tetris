Description:
------------

This program takes a Hack assembly program in a file of .asm format, and produces a text file of .hack format, containing the translated Hack machine code.

To compile:
-----------

1. In terminal, cd into the src/ directory.

2. Then, type the command:

javac Assembler/*.java

This should compile all the files in the package.

If that doesn't work, then compile the files individually:

javac Assembler/Assembler.java
javac Assembler/Code.java
javac Assembler/Parser.java
javac Assembler/SymbolTable.java

To run:
--------

1. While you are still in the src/ directory in terminal, type the command:

java Assembler/Assembler somepath/filename.asm

- Note that "somepath/" is the path (relative to the directory where you're running java from) where the .asm file is located.

For example, if you are in /Users/yourname/code/YapE-LynnProject6/src, and you type:

java Assembler/Assembler ../../Pong.asm

Then the program will translate the file /Users/yourname/code/Pong.asm.

- If the file that you want to translate is located in the src/ directory (i.e. the same directory that you're running java from), then type:

java Assembler/Assembler Pong.asm

- If the path and/or filename you have supplied is invalid, the program will throw an exception.

- If the path and filename are valid, then the program will create a .hack file in the same directory that the original .asm is located in.

- The text file suppled must be saved using the .asm extension. Not other file extensions will be allowed.

Note:
------
- This program does not fully validate the .asm file to ensure that it only contains legal assembly language commands, i.e. it assumes that the .asm file you are supplying is syntactically correct.

- For commutative operations in the computation of C-instructions, operands can be supplied in any order. E.g. both D+1 and 1+D are valid.

- For the destination component of a C-instruction (if any), the letters can be written in any order. E.g. both AM and MA are valid.