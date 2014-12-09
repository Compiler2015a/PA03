package IC.SymbolsTable;

import IC.AST.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
	  /** map from String to Symbol **/
	  private Map<String,SymbolEntry> entries;
	  private List<SymbolTable> children;
	  private String id;
	  private SymbolTable parentSymbolTable;
	  public SymbolTable(String id) {
	    this.id = id;
	    entries = new HashMap<String,SymbolEntry>();
	    children = new ArrayList<SymbolTable>();
	  }
	  
	  public class SymbolEntry {
		  private String id;
		  private Type type;
		  private Kind kind;
	  }
	  
	  public enum Kind {
		  Class, Method, Varable
	  }

}



