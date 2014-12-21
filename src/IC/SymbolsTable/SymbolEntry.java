package IC.SymbolsTable;

import IC.Types.Type;

/**
 * Data structure representing a symbol entry in a symbol table.
 */
public class SymbolEntry {
	
	private String id;
	private Type type;
	private IDSymbolsKinds kind;
	  
	/**
	 * symbol entry constructor
	 * @param id id/name of symbol
	 * @param type type of symbol
	 * @param kind kind of symbol
	 */
	public SymbolEntry(String id, Type type, IDSymbolsKinds kind) {
		this.id =id;
	    this.type = type;
	    this.kind = kind;
	}
	
	/**
	 * 
	 * @return id of symbol
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return type of symbol
	 */
	public Type getType() {
		return type;
	}
	
	/** 
	 * @return kind of symbol
	 */
	public IDSymbolsKinds getKind() {
		return kind;
	}
	
	@Override
	public String toString() {
		if (kind == IDSymbolsKinds.CLASS)
			return kind.toString() + ": " + id;
		if (kind.isMethodKind())
			return kind.toString() + ": " + id + " {" + type.toString() + "}";
		
		return kind.toString() + ": " + type.toString() + " " + id;
	}
}
