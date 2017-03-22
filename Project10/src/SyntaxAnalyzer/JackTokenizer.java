package SyntaxAnalyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer. Removes all comments and white space from the input stream and breaks it into Jack-language
 * tokens, as specified by the Jack grammar.
 */

public class JackTokenizer {

    // Regular expressions for the various tokens
    private static final String KEYWORDS_REGEX = "(class|constructor|function|method|field|static|var"
            + "|int|char|boolean|void|true|false|null|this|let"
            + "|do|if|else|while|return)";
    private static final String SYMBOLS_REGEX = "([{}()\\[\\].,;+-/&|<>=*~])";
    private static final String INT_CONST_REGEX = "(\\d+)";
    private static final String STRING_CONST_REGEX = "(\"(?:\\\\\"|[^\"])*\")";
    private static final String IDENTIFIER_REGEX = "([a-zA-Z][a-zA-Z0-9_]*|[_][a-zA-Z0-9_]*)";
    private static final Pattern TOKEN_PATTERN = Pattern.compile(KEYWORDS_REGEX + "|" +
                                                                 SYMBOLS_REGEX + "|" +
                                                                 INT_CONST_REGEX + "|" +
                                                                 STRING_CONST_REGEX + "|" +
                                                                 IDENTIFIER_REGEX);

    // An object representing the value of a token and its type
    class Token {

        private String value; // the actual sequence of chars constituting the token
        private TokenType type;

        public Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public TokenType getType() {
            return type;
        }
    }

    private List<Token> tokens; // tokens added to this list in the order they are parsed
    private Token currentToken; // currentToken = tokens.get(index)
    private Matcher matcher; // used to match regex for tokens against the input
    private int index; // Index of the current token

    // Lists the possible types for tokens
    public enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    // Opens the input file/stream and gets ready to tokenize it
    public JackTokenizer(Scanner in) {
        tokens = new ArrayList<>();
        this.matcher = TOKEN_PATTERN.matcher(stripComments(in));
        extractTokens(); // Extracts tokens into the ArrayList tokens
        index = -1; // currToken is initially null
    }

    private void extractTokens() {
        while (matcher.find()) {

            // Match tokens in decreasing order of definitional specificity
            if (matcher.group(1) != null) {
                tokens.add(new Token(matcher.group(1), TokenType.KEYWORD));
            } else if (matcher.group(2) != null) {
                tokens.add(new Token(matcher.group(2), TokenType.SYMBOL));
            } else if (matcher.group(3) != null) {
                tokens.add(new Token(matcher.group(3), TokenType.INT_CONST));
            } else if (matcher.group(4) != null) {
                tokens.add(new Token(matcher.group(4), TokenType.STRING_CONST));
            } else if (matcher.group(5) != null) {
                tokens.add(new Token(matcher.group(5), TokenType.IDENTIFIER));
            }
        }
    }

    // Processes a file to strip out all comments and white spaces
    private String stripComments(Scanner input) {
        String processed = "";

        // Strip out single-line comments
        while (input.hasNextLine()) {
            String currLine = input.nextLine();
            if (currLine.isEmpty() || currLine.startsWith("//")) continue;
            if (currLine.contains("//")) {
                currLine = currLine.substring(0, currLine.indexOf("//"));
            }
            if (currLine.trim().length() == 0) continue;
            processed += currLine + " ";
        }

        // Strip out block comments
        processed = processed.replaceAll("/\\*(.|[\\r\\n])*?\\*/", "");

        return processed;
    }

    // Determines if we have more tokens in the input
    public boolean hasMoreTokens() {
        return index < tokens.size() - 1;
    }

    // Gets the next token from the input and makes it the current token.
    // Should only be called if hasMoreTokens() is true. Initially there is no current token.
    public void advance() {
        index++;
        currentToken = tokens.get(index);
    }

    // Resets the pointer for the arraylist to the start
    public void reset() {
        index = -1;
        currentToken = null;
    }

    // Moves pointer back by 1
    public void rewind() {
        index -= 1;
        currentToken = tokens.get(index);
    }

    // Returns the type of the current token
    public TokenType tokenType() {
        return currentToken.getType();
    }

    // Returns the unprocessed token
    public String getToken() {
        return currentToken.getValue();
    }

    // Returns the keyword which is the current token.
    // Should be called only when tokenType() is Keyword
    public String keyWord() {
        return currentToken.getValue();
    }

    // Returns the character which is the current token. Should only be called when tokenType() is SYMBOL
    public char symbol() {
        return currentToken.getValue().charAt(0);
    }

    // Returns the identifier which is the current token. Should only be called when tokenType() is IDENTIFIER
    public String identifier() {
        return currentToken.getValue();
    }

    // Returns the integer value of the current token. Should only be called when tokenType() is INT_CONST
    public int intVal() {
        return Integer.parseInt(currentToken.getValue());
    }

    // Returns the string value of the current token, without the double quotes.
    // Should only be called when tokenType() is STRING_CONST
    public String stringVal() {
        String tokenVal = currentToken.getValue();
        return tokenVal.substring(1, tokenVal.length() - 1);
    }

}
