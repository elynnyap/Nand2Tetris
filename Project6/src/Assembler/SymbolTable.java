package Assembler;

import java.util.HashMap;

/**
 * Resolves symbols into actual memory addresses. Used in Assembler.java.
 */

public class SymbolTable {

    private HashMap<String, Integer> table;
    private int currAddress = 16; // the next available memory address

    // load the predefined symbols
    public SymbolTable() {
        table = new HashMap<>();
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);
        table.put("R0", 0);
        table.put("R1", 1);
        table.put("R2", 2);
        table.put("R3", 3);
        table.put("R4", 4);
        table.put("R5", 5);
        table.put("R6", 6);
        table.put("R7", 7);
        table.put("R8", 8);
        table.put("R9", 9);
        table.put("R10", 10);
        table.put("R11", 11);
        table.put("R12", 12);
        table.put("R13", 13);
        table.put("R14", 14);
        table.put("R15", 15);
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
    }

    // maps a new symbol to its address
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    // checks if a symbol is already in the table
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    // returns the address for a symbol in the table, or null if there's no mapping
    public int getAddress(String symbol) {
        return table.get(symbol);
    }

    // return the next available memory address and increment
    public int getNextAddAndIncrement() {
        return currAddress++;
    }
}
