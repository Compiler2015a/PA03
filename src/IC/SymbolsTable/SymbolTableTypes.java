package IC.SymbolsTable;

/**
 * enum which represents the different symbol table types available
 *
 */
public enum SymbolTableTypes {
	GLOBAL("Global"),
	CLASS("Class"),
	METHOD("Method"),
	STATEMENT_BLOCK("Statement Block");
	
	private final String repr;       

	private SymbolTableTypes(String s) {
			repr = s;
	}
	
	public String toString(){
	    return repr;
	}
}
