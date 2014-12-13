package IC.SymbolsTable;

import IC.Types.Type;
public class SymbolEntry {
	
	private String id;
	private Type type;
	private IDSymbolsKinds kind;
	  
	public SymbolEntry(String id, Type type, IDSymbolsKinds kind) {
		this.id =id;
	    this.type = type;
	    this.kind = kind;
	}
	
	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}
	
	public IDSymbolsKinds getKind() {
		return kind;
	}
}
