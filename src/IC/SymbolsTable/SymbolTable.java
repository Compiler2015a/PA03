package IC.SymbolsTable;

import java.util.HashMap;
import java.util.Map;

import IC.Types.Type;


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
	  
	  public IDSymbolsKinds getType()
	  {
		  return parentSymbolTable.entries.get(id).getKind();
	  }
	  
	  public boolean isTypeOf(String ancestor, String descendant) {
			SymbolTable table = this;
			while (table.parentSymbolTable != null)
				table = table.parentSymbolTable;
			SymbolTable scopeAncestor = table.getClassScope(ancestor);
			if (scopeAncestor == null)
				return false;
			SymbolTable scopeDescendant = scopeAncestor.getClassScope(descendant);
			if (scopeDescendant == null)
				return false;
			return true;
			
		}

	  public SymbolTable getClassScope(String className) {
			if (id.equals(className)) 
			{
				return this;
			}
			
			for (SymbolTable subTable : children.values()) {
				SymbolTable table = subTable.getClassScope(className);
				if (table != null)
					return table;
			}
			
			return null;
		}
	  
}




