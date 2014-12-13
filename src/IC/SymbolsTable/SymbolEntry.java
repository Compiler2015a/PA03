package IC.SymbolsTable;

public class SymbolEntry {
	
	private String id;
	private String type; //TODO Should be changed to typeEntry
	private IDSymbolsKinds kind;
	  
	public SymbolEntry(String id, String type, IDSymbolsKinds kind) {
		this.id =id;
	    this.type = type;
	    this.kind = kind;
	}
	
	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}
	
	public IDSymbolsKinds getKind() {
		return kind;
	}
}
