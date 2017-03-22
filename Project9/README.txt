Game Description
----------------
This program is a simulation of Conway's Game of Life.

Background here: https://en.wikipedia.org/wiki/Conway's_Game_of_Life.

The simulation begins with a two-dimensional grid of square cells. Each cell
is either 'dead' (black) or 'alive' (white).

At each step in time, the following transitions occur:

1. Any live cell with fewer than two live neighbours dies, as if caused by 
underpopulation.
2. Any live cell with two or three live neighbours lives on to the next 
generation.
3. Any live cell with more than three live neighbours dies, as if by 
overpopulation.
4. Any dead cell with exactly three live neighbours becomes a live cell, 
as if by reproduction.

How to Compile And Run
-----------------------
1. Compile all the Jack files in the src/ folder by using the JackCompiler.

2. Once a .vm file has been created for each .jack file, load the directory
into the VMEmulator to run the program.

3. Set the speed to Fast and select "No animation" under "Animate:"

4. Run the program.

Instructions for Play
----------------------
The program prompts the user for a number between 1-9 indicating how
heavily the board should be populated (1 being least populated and 9 being
most populated.)

A 56x28 grid will then be shown on the screen.

During the simulation, the player can press the 'z' key to restart or
the 'q' key to quit.

If the 'z' key is pressed, the player can then choose another number (or the
same number) between 1-9. A new simulation then begins running.

If the 'q' key is pressed, the entire program stops running.

Notes
-----
Because of lags (and depending on the speed of your computer), the 'z' or 'q' key 
should be held down for a bit longer (instead of just quickly tapping on them) in 
order for the program to register the input. You may have to hold the key down or
press it more than once.

If the simulation reaches a state where cells all stay the same (or have all died) then
you will need to press 'z' to restart manually.

The file Random.jack, used to generate random numbers in the program, is attributed 
to Mark Armbrust and obtained from this source: 
https://gist.github.com/ybakos/7ca67fcfd07477a9550b

I ran this idea past Marty and she agreed that it was sufficiently complex.