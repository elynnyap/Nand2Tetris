package JackCompiler;

import java.io.PrintWriter;

/**
 * Reads its input from a JackTokenizer and writes its output into a VMWriter.
 * organized as a series of compilexxx() routines, where xxx is a syntactic element
 * of the Jack language.
 * Each compilexxx() routine should read the syntactic construct xxx from the input, advance()
 * the tokenizer exactly beyond xxx, and emit to the output VM code effecting the
 * semantics of xxx.
 */

public class CompilationEngine {

    private JackTokenizer tokenizer; // input tokens
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private String className;
    private String subroutineName = "";
    private int expressionCount = 0;
    private int whileLabel = 0;
    private int ifLabel = 0;

    // Creates a new compilation engine with the given input and output
    // Next routine called must be compileClass()
    public CompilationEngine(JackTokenizer tokenizer, PrintWriter outputWriter) {
        this.tokenizer = tokenizer;
        vmWriter = new VMWriter(outputWriter);
        symbolTable = new SymbolTable();
    }

    // Compiles a complete class
    public void compileClass() {

        advanceTokenizer(); // advance to the first token

        requireToken(NonTerminals.CLASS);
        advanceTokenizer(); // advance past "class" keyword

        // "class" keyword must be followed by class name
        requireToken(NonTerminals.CLASS_NAME);
        className = tokenizer.identifier();
        advanceTokenizer();

        // Class name must be followed by "{" symbol
        requireToken(JackTokenizer.TokenType.SYMBOL, "{");
        advanceTokenizer();

        // Print any class var declarations
        while (tokenMatches(NonTerminals.CLASS_VAR_DEC)){
            compileClassVarDec();
        }

        // Print any subroutine declarations
        while (tokenMatches(NonTerminals.SUBROUTINE_DEC)){
            compileSubroutine();
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, "}");
    }

    // Compiles a static declaration of a field declaration
    public void compileClassVarDec() {
        requireToken(NonTerminals.CLASS_VAR_DEC);

        boolean isStatic = false;
        boolean isField = false;

        // check if static or field
        if (tokenizer.keyWord().equals("static")) {
            isStatic = true;
        } else if (tokenizer.keyWord().equals("field")) {
            isField = true;
        }
        advanceTokenizer();

        // Get the type of var
        String type = "";
        requireToken(NonTerminals.TYPE);
        type = tokenizer.getToken();
        advanceTokenizer();

        // Get the name of var
        String name = "";
        requireToken(NonTerminals.VAR_NAME);
        name = tokenizer.identifier();
        advanceTokenizer();

        // add the identifier to the symbol table
        if (isStatic) {
            symbolTable.define(name, type, SymbolTable.Kind.STATIC);
        } else if (isField) {
            symbolTable.define(name, type, SymbolTable.Kind.FIELD);
        }

        // Print comma-separated vars
        while (tokenizer.symbol() == ',') {
            advanceTokenizer();
            requireToken(NonTerminals.VAR_NAME); // comma must be followed by varName
            name = tokenizer.identifier();
            if (isStatic) {
                symbolTable.define(name, type, SymbolTable.Kind.STATIC);
            } else if (isField) {
                symbolTable.define(name, type, SymbolTable.Kind.FIELD);
            }
            advanceTokenizer();
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, ";");
        advanceTokenizer(); // print ";" marking end of varDec
    }

    // Compiles a complete method, function, or constructor
    public void compileSubroutine() {

        symbolTable.startSubroutine();
        boolean isConstructor = false;
        boolean isFunction = false;
        boolean isMethod = false;

        requireToken(NonTerminals.SUBROUTINE_DEC);
        switch (tokenizer.identifier()) {
            case "constructor":
                isConstructor = true;
                break;
            case "function":
                isFunction = true;
                break;
            case "method":
                isMethod = true;
                break;
        }
        advanceTokenizer(); // advance past  "constructor"/"function"/"method" keyword

        if (isMethod) { // if method, then define "this" in symbol table
            symbolTable.define("this", className, SymbolTable.Kind.ARG);
        }

        requireToken(NonTerminals.TYPE, "void");
        advanceTokenizer(); // advance past return type

        // get name of subroutine
        requireToken(NonTerminals.SUBROUTINE_NAME);
        subroutineName = tokenizer.identifier();
        advanceTokenizer(); // advance past subroutine name

        compileParameterList();

        // print subroutine body
        requireToken(JackTokenizer.TokenType.SYMBOL, "{");
        advanceTokenizer(); // advance past "{" symbol

        // print any var declarations
        while (tokenMatches(NonTerminals.VAR_DEC)) {
            compileVarDec();
        }

        vmWriter.writeFunction(className + "." + subroutineName,
                                symbolTable.varCount(SymbolTable.Kind.VAR));
        subroutineName = ""; // reset subroutine name

        if (isConstructor) { // if constructor, then allocate memory for new object
            int words = symbolTable.varCount(SymbolTable.Kind.FIELD); // num of words needed to represent obj

            // generate code to allocate memory: this = alloc(n)
            vmWriter.writePush(VMWriter.Segment.CONSTANT, words);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        } else if (isMethod) {
            // point this to the current object reference
            vmWriter.writePush(VMWriter.Segment.ARGUMENT, 0); // first arg is obj ref
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        }

        compileStatements();

        requireToken(JackTokenizer.TokenType.SYMBOL, "}");
        advanceTokenizer(); // advance past "}" symbol

        // reset label numbers
        ifLabel = 0;
        whileLabel = 0;
    }

    // Compiles a (possibly empty) parameter list, not including the enclosing "()"
    public void compileParameterList() {
        requireToken(JackTokenizer.TokenType.SYMBOL, "(");
        advanceTokenizer(); // advance past opening parenthesis

        String type;
        String name;
        // Print any parameters
        while (tokenMatches(NonTerminals.TYPE)) {
            type = tokenizer.getToken();
            advanceTokenizer(); // advance past var type
            requireToken(NonTerminals.VAR_NAME);
            name = tokenizer.identifier();
            advanceTokenizer(); // advance past var name
            symbolTable.define(name, type, SymbolTable.Kind.ARG);
            if (tokenMatches(JackTokenizer.TokenType.SYMBOL, ",")) {
                advanceTokenizer(); // advance past comma
                requireToken(NonTerminals.TYPE);
            } else {
                break;
            }
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, ")");
        advanceTokenizer();
    }

    // Compiles a var declaration
    public void compileVarDec() {

        String type = "";
        String name = "";

        advanceTokenizer(); // advance past "var"
        requireToken(NonTerminals.TYPE);
        type = tokenizer.getToken();
        advanceTokenizer(); // advance past var type
        requireToken(NonTerminals.VAR_NAME);
        while (true) {
            name = tokenizer.getToken();
            advanceTokenizer(); // advance past var name
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
            if (tokenMatches(JackTokenizer.TokenType.SYMBOL, ",")) {
                advanceTokenizer(); // advance past comma
                requireToken(NonTerminals.VAR_NAME);
            } else {
                requireToken(JackTokenizer.TokenType.SYMBOL, ";");
                advanceTokenizer(); // advance past ending semicolon
                break;
            }
        }

    }

    // Compiles a sequence of statements, not including the enclosing "{}"
    public void compileStatements() {
        requireToken(NonTerminals.STATEMENT);

        // iterate while there are still statements to process
        while(tokenMatches(NonTerminals.STATEMENT)) {

            // determine what kind of statement it is
            if (tokenMatches(JackTokenizer.TokenType.KEYWORD, "let")) {
                compileLet();
            } else if (tokenMatches(JackTokenizer.TokenType.KEYWORD, "if")) {
                compileIf();
            } else if (tokenMatches(JackTokenizer.TokenType.KEYWORD, "while")) {
                compileWhile();
            } else if (tokenMatches(JackTokenizer.TokenType.KEYWORD, "do")) {
                compileDo();
            } else {
                compileReturn();
            }
        }
    }

    // Compiles a do statement
    public void compileDo() {

        advanceTokenizer(); // advance past "do" keyword
        requireToken(NonTerminals.SUBROUTINE_CALL);

        // compile subroutine
        compileSubroutineCall();

        requireToken(JackTokenizer.TokenType.SYMBOL, ";");
        advanceTokenizer(); // advance past ";" symbol

        // pop and ignore the returned value
        vmWriter.writePop(VMWriter.Segment.TEMP, 0);

    }

    // Compiles a let statement
    public void compileLet() {

        advanceTokenizer(); // advance past "let"

        requireToken(NonTerminals.VAR_NAME);
        String varName = tokenizer.identifier();
        advanceTokenizer(); // advance past identifier

        boolean isArray = false; // whether it's an array reference being set

        if (tokenMatches(JackTokenizer.TokenType.SYMBOL, "[")) { // check for square brackets for arrays
            compileArray(varName);
            isArray = true;
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, "=");
        advanceTokenizer(); // advance past equal sign

        compileExpression();

        // set the value of the variable
        if (isArray) {
            vmWriter.writePop(VMWriter.Segment.TEMP, 0); // store value of evaluated exp in temp
            vmWriter.writePop(VMWriter.Segment.POINTER, 1); // point "THAT" to the array
            vmWriter.writePush(VMWriter.Segment.TEMP, 0); // push the value back onto the stack
            vmWriter.writePop(VMWriter.Segment.THAT, 0); // pop the value into the array reference
        } else {
            compileIdentifier(varName, true);
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, ";");
        advanceTokenizer(); // print ending semicolon


    }

    // writes a push or pop for the given identifier
    private void compileIdentifier(String identifier, boolean isPop) {

        SymbolTable.Kind kind;
        int index;

        // look up identifier in symbol table
        kind = symbolTable.kindOf(identifier);
        index = symbolTable.indexOf(identifier);

        // match kind to segment
        VMWriter.Segment segment = null;
        switch (kind) {
            case ARG:
                segment = VMWriter.Segment.ARGUMENT;
                break;
            case STATIC:
                segment = VMWriter.Segment.STATIC;
                break;
            case FIELD:
                segment = VMWriter.Segment.THIS;
                break;
            case VAR:
                segment = VMWriter.Segment.LOCAL;
                break;
        }

        if (isPop) {
            vmWriter.writePop(segment, index);
        } else {
            vmWriter.writePush(segment, index);
        }
    }

    // compiles a while statement
    public void compileWhile() {

        int labelNum = whileLabel++;

        // label the start of the while loop
        vmWriter.writeLabel("WHILE_EXP" + labelNum);

        advanceTokenizer(); // advance past "while"

        requireToken(JackTokenizer.TokenType.SYMBOL, "(");
        advanceTokenizer(); // advance past opening parenthesis

        requireToken(NonTerminals.EXPRESSION);
        compileExpression(); // compile exp that's the condition of the while
        vmWriter.writeArithmetic(VMWriter.Command.NOT); // compute ~(cond) in "while (cond)"
        vmWriter.writeIf("WHILE_END" + labelNum); // go to end of loop if cond evaluates to false

        requireToken(JackTokenizer.TokenType.SYMBOL, ")");
        advanceTokenizer(); // advance past closing parenthesis

        requireToken(JackTokenizer.TokenType.SYMBOL, "{");
        advanceTokenizer(); // advance past opening curly brace

        // compile statements in body of while
        requireToken(NonTerminals.STATEMENT);
        compileStatements();

        requireToken(JackTokenizer.TokenType.SYMBOL, "}");
        advanceTokenizer(); // print "}" marking end of stmt

        // go to the top of the loop
        vmWriter.writeGoto("WHILE_EXP" + labelNum);

        // label the end of the while loop
        vmWriter.writeLabel("WHILE_END" + labelNum);

    }

    // Compiles a return statement
    public void compileReturn() {

        boolean isVoid = true; // true if return not followed by expression

        advanceTokenizer(); // advance past "return"

        // print any expressions
        while (tokenMatches(NonTerminals.EXPRESSION)) {
            isVoid = false;
            compileExpression();
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, ";");
        advanceTokenizer(); // advance past ";" marking end of stmt

        if (isVoid) {
            vmWriter.writePush(VMWriter.Segment.CONSTANT, 0);
            vmWriter.writeReturn();
        } else {
            vmWriter.writeReturn();
        }

    }

    // Compiles an if statement, possibly with a trailing else clause
    public void compileIf() {

        int labelNum = ifLabel++;

        advanceTokenizer(); // advance past "if"

        requireToken(JackTokenizer.TokenType.SYMBOL, "(");
        advanceTokenizer(); // advance past opening parenthesis

        // compile exp that is the condition of the if
        requireToken(NonTerminals.EXPRESSION);
        compileExpression();

        // if (cond) then go to true branch, else go to false ("else") branch
        vmWriter.writeIf("IF_TRUE" + labelNum);
        vmWriter.writeGoto("IF_FALSE" + labelNum);

        requireToken(JackTokenizer.TokenType.SYMBOL, ")");
        advanceTokenizer(); // advance past closing parenthesis

        requireToken(JackTokenizer.TokenType.SYMBOL, "{");
        advanceTokenizer(); // advance past opening curly brace

        // compile statement(s) in "true" branch of if
        vmWriter.writeLabel("IF_TRUE" + labelNum);
        requireToken(NonTerminals.STATEMENT);
        compileStatements();

        requireToken(JackTokenizer.TokenType.SYMBOL, "}");
        advanceTokenizer(); // advance past closing curly brace

        // check if there's an else clause
        boolean hasElseClause = false;
        if (tokenMatches(JackTokenizer.TokenType.KEYWORD, "else")) hasElseClause = true;

        if (hasElseClause) vmWriter.writeGoto("IF_END" + labelNum);

        vmWriter.writeLabel("IF_FALSE" + labelNum); // label marking start of statements in "else"

        // compile else clause if needed
        if (hasElseClause) {
            advanceTokenizer(); // advance past "else"
            requireToken(JackTokenizer.TokenType.SYMBOL, "{");
            advanceTokenizer(); // advance past opening curly brace
            compileStatements();
            requireToken(JackTokenizer.TokenType.SYMBOL, "}");
            advanceTokenizer(); // advance past closing curly brace
        }

        // write label for end of "if"
        if (hasElseClause) vmWriter.writeLabel("IF_END" + labelNum);

    }

    // Compiles a subroutine call
    public void compileSubroutineCall() {
        boolean isSubroutine; // call starts either with subroutineName or className|varName

        String funcName = tokenizer.identifier(); // store subRoutineName/className/varName
        advanceTokenizer();

        if (tokenMatches(JackTokenizer.TokenType.SYMBOL, "(")) {
            isSubroutine = true;
        } else if (tokenMatches(JackTokenizer.TokenType.SYMBOL, ".")) {
            isSubroutine = false;
        } else {
            throw new IllegalArgumentException("Expected \"(\" or \".\" instead of " + tokenizer.getToken());
        }

        advanceTokenizer(); // advance past "(" or "."

        if (isSubroutine) {
            // push arguments onto stack
            vmWriter.writePush(VMWriter.Segment.POINTER, 0); // first arg is ref to obj
            compileExpressionList();
            vmWriter.writeCall(className + "." + funcName, expressionCount + 1);
            expressionCount = 0;
        } else { // of the form "(className|varName).subRoutineName(expressionList)"

            boolean isMethodCall = false;

            // distinguish between className and varName
            if (Character.isUpperCase(funcName.charAt(0))) { // class name
                funcName += "." + tokenizer.identifier();
            } else {
                isMethodCall = true;
                compileIdentifier(funcName, false); // push the obj reference onto stack
                funcName = symbolTable.typeOf(funcName) + "." + tokenizer.identifier();
            }

            advanceTokenizer(); // advance past subroutine name
            requireToken(JackTokenizer.TokenType.SYMBOL, "(");
            advanceTokenizer(); // advance past "("
            compileExpressionList();

            if (isMethodCall) expressionCount += 1; // method calls include extra arg for obj ref
            vmWriter.writeCall(funcName, expressionCount);
            expressionCount = 0; // reset expression count
        }

        requireToken(JackTokenizer.TokenType.SYMBOL, ")");
        advanceTokenizer(); // advance past ")" symbol
    }

    // Compiles an expression
    public void compileExpression() {

        requireToken(NonTerminals.TERM);
        compileTerm(); // print first term in expression

        // if expr is of form "term op term", compile "op term"
        while (tokenMatches(NonTerminals.OP)) {
            char op = tokenizer.symbol(); // store the "op"
            advanceTokenizer(); // advance past op
            requireToken(NonTerminals.TERM); // op must be followed by another term
            compileTerm();

            // postfix notation
            switch (op) {
                case '+':
                    vmWriter.writeArithmetic(VMWriter.Command.ADD);
                    break;
                case '-':
                    vmWriter.writeArithmetic(VMWriter.Command.SUB);
                    break;
                case '*':
                    vmWriter.writeCall("Math.multiply", 2);
                    break;
                case '/':
                    vmWriter.writeCall("Math.divide", 2);
                    break;
                case '&':
                    vmWriter.writeArithmetic(VMWriter.Command.AND);
                    break;
                case '|':
                    vmWriter.writeArithmetic(VMWriter.Command.OR);
                    break;
                case '<':
                    vmWriter.writeArithmetic(VMWriter.Command.LT);
                    break;
                case '>':
                    vmWriter.writeArithmetic(VMWriter.Command.GT);
                    break;
                case '=':
                    vmWriter.writeArithmetic(VMWriter.Command.EQ);
                    break;
            }
        }

    }

    // Compiles a term
    public void compileTerm() {


        if (tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) { // If term is just an int constant
            vmWriter.writePush(VMWriter.Segment.CONSTANT, tokenizer.intVal());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) { // if term is str constant
            String str = tokenizer.stringVal();
            int strLength = str.length(); // num of chars used as arg for String.new
            vmWriter.writePush(VMWriter.Segment.CONSTANT, strLength);
            vmWriter.writeCall("String.new", 1);

            // iterate through characters to append to string
            for (char c : str.toCharArray()) {
                vmWriter.writePush(VMWriter.Segment.CONSTANT, (int) c); // cast char to ascii value
                vmWriter.writeCall("String.appendChar", 2);
            }

            advanceTokenizer();
        } else if (tokenMatches(NonTerminals.KEYWORD_CONSTANT)) { // if term is keyword constant
            switch (tokenizer.keyWord()) {
                case "true": // true is -1
                    vmWriter.writePush(VMWriter.Segment.CONSTANT, 0);
                    vmWriter.writeArithmetic(VMWriter.Command.NOT);
                    break;
                case "false":
                case "null": // false and null are 0
                    vmWriter.writePush(VMWriter.Segment.CONSTANT, 0);
                    break;
                case "this": // push "this" pointer onto stack
                    vmWriter.writePush(VMWriter.Segment.POINTER, 0);
                    break;
            }
            advanceTokenizer();
        } else if (tokenMatches(NonTerminals.UNARY_OP)) {
            // unary op followed by term
            char op = tokenizer.symbol();
            tokenizer.advance();
            compileTerm();
            switch (op) {
                case '-':
                    vmWriter.writeArithmetic(VMWriter.Command.NEG);
                    break;
                case '~':
                    vmWriter.writeArithmetic(VMWriter.Command.NOT);
                    break;
            }
        } else if (tokenMatches(JackTokenizer.TokenType.SYMBOL, "(")) {
            // expression within parentheses
            advanceTokenizer();
            compileExpression();
            requireToken(JackTokenizer.TokenType.SYMBOL, ")");
            advanceTokenizer();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            // distinguish between var, arr entry, and subroutine call
            advanceTokenizer();
            char sym = tokenizer.symbol();
            tokenizer.rewind();
            if (sym == '[') { // array entry
                String varName = tokenizer.identifier();
                advanceTokenizer(); // advance past varName
                compileArray(varName);
                vmWriter.writePop(VMWriter.Segment.POINTER, 1); // point THAT to the array reference
                vmWriter.writePush(VMWriter.Segment.THAT, 0); // push THAT onto the stack
            } else if (sym == '(' || sym == '.') { // subroutine call
                compileSubroutineCall();
            } else {
                compileIdentifier(tokenizer.identifier(), false);
                advanceTokenizer(); // varName
            }
        }

    }

    private void compileArray(String varName) {
        advanceTokenizer(); // advance past "["
        compileExpression(); // push array index onto stack
        compileIdentifier(varName, false); // push array base add onto stack
        vmWriter.writeArithmetic(VMWriter.Command.ADD); // add index to base to get add
        requireToken(JackTokenizer.TokenType.SYMBOL, "]");
        advanceTokenizer(); // advance past "]"
    }

    // Compiles a (possibly empty) comma-separated list of expressions
    public void compileExpressionList() {

        // Print any expressions in this list
        while (tokenMatches(NonTerminals.EXPRESSION)) {
            expressionCount++; // count the number of expressions in the list
            compileExpression();
            if (tokenMatches(JackTokenizer.TokenType.SYMBOL, ",")) {
                advanceTokenizer(); // advance past comma
                requireToken(NonTerminals.EXPRESSION); // comma must be followed by expr
            } else {
                break;
            }
        }

    }

    // Checks that token fulfills the expected construct and throws an error otherwise
    private void requireToken(JackTokenizer.TokenType expectedType, String value) {
        if (tokenizer.tokenType() != expectedType || !tokenizer.getToken().equals(value)) {
            throw new IllegalArgumentException("Expected token of type " + expectedType +
                    " and value " + value + " instead of the following: " + tokenizer.getToken());
        }
    }

    private void requireToken(NonTerminals category) {
        if (!tokenMatches(category)) {
            throw new IllegalArgumentException("Expected token of type " + category
                    + " instead of the following: " + tokenizer.getToken());
        }
    }

    private void requireToken(NonTerminals category, String value1) {
        if (!tokenMatches(category) && !tokenizer.getToken().matches(value1)) {
            throw new IllegalArgumentException("Expected token of type " + category
                    + " or form " + value1 + " instead of the following: " + tokenizer.getToken());
        }
    }

    // Checks whether token is of the specified construct
    // (Checks could be stronger; only validates first token of the construct)
    private boolean tokenMatches(NonTerminals category) {
        switch (category) {
            case CLASS:
                return validateToken(JackTokenizer.TokenType.KEYWORD, "class");
            case CLASS_VAR_DEC:
                return (validateToken(JackTokenizer.TokenType.KEYWORD, "static") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "field"));
            case SUBROUTINE_DEC:
                return (validateToken(JackTokenizer.TokenType.KEYWORD, "constructor") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "function") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "method"));
            case TYPE:
                return (validateToken(JackTokenizer.TokenType.KEYWORD, "int") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "char") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "boolean") ||
                        tokenMatches(NonTerminals.CLASS_NAME));
            case VAR_DEC:
                return (validateToken(JackTokenizer.TokenType.KEYWORD, "var"));
            case CLASS_NAME:
            case VAR_NAME:
            case SUBROUTINE_NAME:
                return (validateToken(JackTokenizer.TokenType.IDENTIFIER));
            case SUBROUTINE_CALL:
                return validateToken(JackTokenizer.TokenType.IDENTIFIER);
            case STATEMENT:
                return(validateToken(JackTokenizer.TokenType.KEYWORD, "let") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "if") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "while") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "do") ||
                        validateToken(JackTokenizer.TokenType.KEYWORD, "return"));
            case EXPRESSION:
                return tokenMatches(NonTerminals.TERM);
            case TERM:
                return(validateToken(JackTokenizer.TokenType.INT_CONST) ||
                        validateToken(JackTokenizer.TokenType.STRING_CONST) ||
                        tokenMatches(NonTerminals.KEYWORD_CONSTANT) ||
                        tokenMatches(NonTerminals.VAR_NAME) ||
                        tokenMatches(NonTerminals.SUBROUTINE_CALL) ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "(") ||
                        tokenMatches(NonTerminals.UNARY_OP));
            case UNARY_OP:
                return (tokenMatches(JackTokenizer.TokenType.SYMBOL, "-") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "~"));
            case OP:
                return (tokenMatches(JackTokenizer.TokenType.SYMBOL, "+") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "-") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "*") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "/") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "&") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "|") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "<") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, ">") ||
                        tokenMatches(JackTokenizer.TokenType.SYMBOL, "="));
            case KEYWORD_CONSTANT:
                return (tokenMatches(JackTokenizer.TokenType.KEYWORD, "true") ||
                        tokenMatches(JackTokenizer.TokenType.KEYWORD, "false") ||
                        tokenMatches(JackTokenizer.TokenType.KEYWORD, "null") ||
                        tokenMatches(JackTokenizer.TokenType.KEYWORD, "this"));
            default:
                return false;
        }
    }

    private boolean tokenMatches(JackTokenizer.TokenType type, String value) {
        return (tokenizer.tokenType() == type && tokenizer.getToken().equals(value));
    }

    // Checks if token matches a certain type and value and returns boolean
    private boolean validateToken(JackTokenizer.TokenType type, String value) {
        return (tokenizer.tokenType() == type && tokenizer.getToken().equals(value));
    }

    private boolean validateToken(JackTokenizer.TokenType type) {
        return (tokenizer.tokenType() == type);
    }

    // Enumerates the various types of non-terminals
    private static enum NonTerminals {
        CLASS, CLASS_VAR_DEC, TYPE, SUBROUTINE_DEC, PARAMETER_LIST, SUBROUTINE_BODY,
        VAR_DEC, CLASS_NAME, SUBROUTINE_NAME, VAR_NAME, STATEMENT, SUBROUTINE_CALL,
        EXPRESSION, TERM, OP, UNARY_OP, KEYWORD_CONSTANT
    }

    // Prints terminal value and its corresponding type, then advances tokenizer
    private void advanceTokenizer() {

        // Throw an error if unable to advance
        if (!tokenizer.hasMoreTokens()) {
            throw new IllegalStateException("No more tokens after " + tokenizer.getToken());
        }

        tokenizer.advance();
    }

}
