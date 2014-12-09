package IC.SymbolsTable;

import IC.AST.Type;

public class SymbolEntry {
	  private String id;
	  private Type type;
	  private IDSymbolsKinds kind;
	  
	  public SymbolEntry(String id, Type type, IDSymbolsKinds kind) {
		  this.id =id;
		  this.type = type;
		  this.kind = kind;
	  }

	public IDSymbolsKinds getKind() {
		return kind;
	}

}
