package JackCompiler;

import java.util.HashMap;

/**
 * Associates the identifier names in the program with identifier properties needed for compilation:
 * type, kind, and running index. Has two nested scopes: class and subroutine.
 */
public class SymbolTable {

    HashMap<String, IdentifierInfo> classTable; // hashtable for class scope
    HashMap<String, IdentifierInfo> subroutineTable; // hashtable for subroutine scope

    // current running indices for each kind of identifier
    private int staticIndex;
    private int fieldIndex;
    private int argIndex;
    private int varIndex;

    // Creates a new empty symbol table
    public SymbolTable() {
        classTable = new HashMap<String, IdentifierInfo>(); // hashtable for subroutine scope
        subroutineTable = new HashMap<String, IdentifierInfo>(); // hashtable for class scope

        // initialize running indices to start at 0
        staticIndex = 0;
        fieldIndex = 0;
        argIndex = 0;
        varIndex = 0;
    }

    // Object to represent the details of an identifier in the symbol table
    class IdentifierInfo {

        private String type;
        private Kind kind;
        private int index;

        public IdentifierInfo(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }

        public String getType() {
            return type;
        }

        public Kind getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }
    }

    // Enumerates all possible kinds for an identifier
    public enum Kind {
        STATIC, FIELD, ARG, VAR, NONE
    }

    // Starts a new subroutine scope, i.e. resets the subroutine's symbol table
    public void startSubroutine() {
        subroutineTable = new HashMap<String, IdentifierInfo>();
        argIndex = 0;
        varIndex = 0;
    }

    // Defines a new identifier of a given name, type and kind and assigns it a running index
    // STATIC and FIELD identifiers have a class scope,
    // while ARG and VAR identifiers have a subroutine scope
    public void define(String name, String type, Kind kind) {
        HashMap<String, IdentifierInfo> scope; // table that identifier should be added to

        switch (kind) {
            case STATIC:
                classTable.put(name, new IdentifierInfo(type, kind, staticIndex++));
                break;
            case FIELD:
                classTable.put(name, new IdentifierInfo(type, kind, fieldIndex++));
                break;
            case ARG:
                subroutineTable.put(name, new IdentifierInfo(type, kind, argIndex++));
                break;
            case VAR:
                subroutineTable.put(name, new IdentifierInfo(type, kind, varIndex++));
                break;
        }
    }

    // Returns the number of variables of the given kind already defined in the current scope
    public int varCount(Kind kind) {

        switch (kind) {
            case STATIC:
                return staticIndex;
            case FIELD:
                return fieldIndex;
            case ARG:
                return argIndex;
            case VAR:
                return varIndex;
            default:
                return 0;
        }

    }

    // Returns the kind of the named identifier in the current scope
    // If the identifier is unknown in the current scope, returns NONE
    public Kind kindOf(String name) {

        IdentifierInfo id = getIdentifierInfo(name);

        return (id == null ? Kind.NONE : id.getKind());
    }

    // Returns the type of the named identifier in the current scope
    public String typeOf(String name) {

        IdentifierInfo id = getIdentifierInfo(name);

        return (id == null ? null : id.getType());
    }

    // Returns the index assigned to the named identifier
    public int indexOf(String name) {

        IdentifierInfo id = getIdentifierInfo(name);

        return (id == null ? -1 : id.getIndex());
    }

    private IdentifierInfo getIdentifierInfo(String name) {
        // search subroutine scope first
        IdentifierInfo id = subroutineTable.get(name);

        // if not found in subroutine scope, search class scope
        if (id == null) id = classTable.get(name);
        return id;
    }
}
