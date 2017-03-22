// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

// Pseudocode:
// i = SCREEN
// while (true)
//  if KBD == 1
//    RAM[i] = -1
//    if (i < KBD-1)
//      i++
//  else
//    RAM[i] = 0
//    if (i > SCREEN)
//      i--

@SCREEN
D=A // D holds address of SCREEN
@i
M=D // i points to start of SCREEN

(LOOP) // start of infinite loop
@KBD
D=M // D holds the value of KBD, i.e. whether key pressed
@BLACK
D;JGT // goto BLACK if KBD > 0 i.e. key was pressed
@WHITE
0;JMP // else goto WHITE

// control jumps here if key pressed
(BLACK)
@i
A=M // A holds the contents of i i.e. address of pixel to fill
M=-1 // RAM[i] = -1 i.e. fill current pixel with black

// check if pointer should be incremented
@i
D=M // D holds value of i
@KBD
D=D-A // D = i - KBD
@LOOP
D=D+1;JEQ // if (i - KBD + 1) == 0, pointer at max boundary, so goto LOOP

// else increment the pointer
@i
M=M+1 // i = i+1
@LOOP
0;JMP // goto LOOP to loop infinitely

// control jumps here if no key pressed
(WHITE)
@i
A=M
M=0 // RAM[i] = 0 i.e. fill current pixel with white

// check if pointer should be decremented
@i
D=M // D holds value of i
@SCREEN
D=D-A // D = i - SCREEN
@LOOP
D;JEQ // if (i - SCREEN) == 0, i.e. i at screen min boundary, goto LOOP

// else decrement the pointer
@i
M=M-1 // i = i-1
@LOOP
0;JMP // goto LOOP to loop infinitely