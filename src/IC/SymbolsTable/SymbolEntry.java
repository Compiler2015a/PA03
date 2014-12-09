package IC.SymbolsTable;

import IC.AST.Type;

public class SymbolEntry {
	  private String id;
	  private Type type;
	  private Kind kind;
	  
	  public SymbolEntry(String id, Type type, Kind kind) {
		  this.id =id;
		  this.type = type;
		  this.kind = kind;
	  }
	  
	  public enum Kind {
		  Class, Method, Varable
	  }
}
