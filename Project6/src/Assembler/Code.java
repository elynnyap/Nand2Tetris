package Assembler;

import java.util.HashMap;

/**
 * Translates Hack assembly language mnemonics into binary codes. Used in Assembler.java.
 */

public class Code {

    // create a hashtable mapping comp mnemonics to binary codes
    static final HashMap<String, String> compCodes = new HashMap<String, String>() {{
        put("0", "0101010");
        put("1", "0111111");
        put("-1", "0111010");
        put("D", "0001100");
        put("A", "0110000");
        put("M", "1110000");
        put("!D", "0001101");
        put("!A", "0110001");
        put("!M", "1110001");
        put("-D", "0001111");
        put("-A", "0110011");
        put("-M", "1110011");
        put("D+1", "0011111");
        put("1+D", "0011111");
        put("A+1", "0110111");
        put("1+A", "0110111");
        put("M+1", "1110111");
        put("1+M", "1110111");
        put("D-1", "0001110");
        put("A-1", "0110010");
        put("M-1", "1110010");
        put("D+A", "0000010");
        put("A+D", "0000010");
        put("D+M", "1000010");
        put("M+D", "1000010");
        put("D-A", "0010011");
        put("D-M", "1010011");
        put("A-D", "0000111");
        put("M-D", "1000111");
        put("D&A", "0000000");
        put("A&D", "0000000");
        put("D&M", "1000000");
        put("M&D", "1000000");
        put("D|A", "0010101");
        put("A|D", "0010101");
        put("D|M", "1010101");
        put("M|D", "1010101");
    }};

    // returns dest mnemonic in its binary representation (3 bits)
    public static String dest(String input) {
        StringBuilder binary = new StringBuilder();

        checkDest(input, binary, "A");
        checkDest(input, binary, "D");
        checkDest(input, binary, "M");

        return binary.toString();
    }

    private static void checkDest(String input, StringBuilder binary, String a) {
        if (input.contains(a)) {
            binary.append("1");
        } else {
            binary.append("0");
        }
    }

    // returns comp mnemonic in its binary representation (7 bits)
    public static String comp(String input) {
        if (!compCodes.containsKey(input)) {
            throw new IllegalArgumentException("Invalid comp mnemonic: " + input);
        } else {
            return compCodes.get(input);
        }
    }

    // returns jump mnemonic in its binary representation (3 bits)
    public static String jump(String input) {
        switch (input) {
            case "":
                return "000";
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            default:
                return "111";
        }
    }

    // returns decimal value in its 15-bit binary representation
    public static String binary(int decimal) throws IllegalArgumentException {

        if (decimal > 65535) {
            throw new IllegalArgumentException("Number too big to load into A-register");
        }

        StringBuilder sb = new StringBuilder();

        while (decimal > 0) {
            sb.append(decimal % 2);
            decimal /= 2;
        }

        // pad with zeroes until number is 15 bits
        while (sb.length() < 15) {
            sb.append("0");
        }

        sb.reverse();

        return sb.toString();
    }
}
