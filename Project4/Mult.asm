// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

// Idea: Initialize R2 to 0, then add R0 to R2 a total
// of R1 times.

// Pseudocode:
// i = R1
// R2 = 0
// while (i != 0)
//    R2 += R0
//    i -= 1

@R1
D=M // D holds the value of R1
@i // i counts how many more times R0 must be added to R2
M=D // initialize i to R1
@R2
M=0 // set R2 to 0
(LOOP)
@i
D=M // D holds the value of i
@END
D;JEQ // goto END if we have added R0 to R2 R1 times, i.e. R2=R0*R1
@R0
D=M // D holds the value of R0
@R2
M=M+D // R2 = R2 + R0
@i
M=M-1 // decrement i
@LOOP
0;JMP // goto LOOP to check if R2=R0*R1
(END)
@END
0;JMP