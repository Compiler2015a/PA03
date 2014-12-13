package IC.SymbolsTable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	  /** map from String to Symbol **/	  
	  
	  private String id;

	  public Map<String,SymbolEntry> entries;
	  public SymbolTable parentSymbolTable;
	  public Map<String, SymbolTable> children;
	  
	  public SymbolTable(String id) {
	    this.id = id;
	    this.entries = new HashMap<String,SymbolEntry>();
	    this.children = new HashMap<String, SymbolTable>();
	    this.parentSymbolTable = null;
	  }

	  public String getId() {
		return id;
	  }
}



