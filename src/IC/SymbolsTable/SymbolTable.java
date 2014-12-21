package IC.SymbolsTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure representing symbol table tree node
 *
 */
public class SymbolTable {
	  /** map from String to Symbol **/	  
	  
	  private String id;
	  private SymbolTableTypes tableType;
	  
	  private Map<String,SymbolEntry> entries;
	  private SymbolTable parentSymbolTable;

	  private Map<String, SymbolTable> children;
	  
	  private List<SymbolEntry> sorted_entries; // is used for prints in printTable() method
	  private List<SymbolTable> sorted_children; // is used for prints in printTable() method
	  
	  /**
	   * Creates a new symbol table.
	   * @param id name of symbol table.
	   * @param tableType type of symbol table.
	   */
	  public SymbolTable(String id, SymbolTableTypes tableType) {
	    this.id = id;
	    this.tableType = tableType;
	    this.entries = new HashMap<String,SymbolEntry>();
	    this.children = new HashMap<String, SymbolTable>();
	    
	    this.sorted_entries = new ArrayList<SymbolEntry>();
	    this.sorted_children = new ArrayList<SymbolTable>();
	    this.parentSymbolTable = null;
	  }

	  /**
	   * 
	   * @return name of this symbol table instance.
	   */
	  public String getId() {
		return id;
	  }
	  
	  /**
	   * 
	   * @return this symbol table's name to entry mapping.
	   */
	  public Map<String,SymbolEntry> getEntries()
	  {
		  return this.entries;
	  }
	  
	  /**
	   * 
	   * @return the table type of this symbol table.
	   */
	  public SymbolTableTypes getTableType() {
		return tableType;
	  }
	  
	  /**
	   * Searches this symbol table's tree for the speicified class name
	   * @param className the name of the class to look for
	   * @return Symbol table of the class if in this scope's sub-tree, or null of not found.
	   */
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
	  
	  /**
	   * Adds this key and symbol to the table's mapping
	   * @param key entry name
	   * @param entry entry
	   */
	  public void addEntry(String key, SymbolEntry entry) {
		  this.entries.put(key, entry);
		  this.sorted_entries.add(entry);
	  }
	  
	  /**
	   * 
	   * @param key name to search in the mapping
	   * @return true iff this table has key in its entry mapping.
	   */
	  public Boolean hasEntry(String key) {
		  return this.entries.containsKey(key);
	  }
	  
	  /**
	   * 
	   * @param key
	   * @return entry associated with this key
	   */
	  public SymbolEntry getEntry(String key) {
		  return this.entries.get(key);
	  }
	  
	  /**
	   * Adds parameter symbol table to this symbol table's children
	   * @param key name of symbol table
	   * @param symbolTable new child symbol table
	   */
	  public void addTableChild(String key, SymbolTable symbolTable) {
		  this.children.put(key, symbolTable);
		  this.sorted_children.add(symbolTable);
	  }
	  
	  /**
	   * prints the table to System.out
	   */
	  public void printTable() {
		  if (this.tableType != SymbolTableTypes.STATEMENT_BLOCK)
			  System.out.println(tableType.toString() + " Symbol Table: " + id);
		  else
			  System.out.println(tableType.toString() + " Symbol Table ( located in " + 
					  this.parentSymbolTable.toString() + " )");
		  
		  for (int i = 0; i < sorted_entries.size(); i++) 
			  System.out.println("    " +  sorted_entries.get(i).toString());
		  
		  if (sorted_children.size() > 0) {
			  if (this.tableType == SymbolTableTypes.CLASS)
				  moveClassChildrenToEnd();
			  System.out.print("Children tables: ");
			  Boolean isFirstChild = true;
			  for (int i = 0; i < sorted_children.size(); i++)  {
				  if (!isFirstChild)
					 System.out.print(", ");
				  else
					  isFirstChild = false;
				  System.out.print(sorted_children.get(i).toString()); 
			  }
			  System.out.println();
		  }
		  
		  System.out.println();
		  for (int i = 0; i < sorted_children.size(); i++)
			  sorted_children.get(i).printTable();
	  }
	  
	  /**
	   * Searches this symbol table tree for a child (or this) table with id
	   * @param id name of symbol table to look for
	   * @return the child symbol table or null if not found
	   */
	  public SymbolTable findChildSymbolTable(String id) {
		  return findChildSymbolTableRecursive(this, id);
	  }
	  
	  /**
	   * 
	   * @return parent symbol table
	   */
	  public SymbolTable getParentSymbolTable() {
		  return parentSymbolTable;
	  }

	  /**
	   * Sets this table's parent table
	   * @param parentSymbolTable parent table to set
	   */
	  public void setParentSymbolTable(SymbolTable parentSymbolTable) {
		  this.parentSymbolTable = parentSymbolTable;
	  }
	  
	  @Override
	  public String toString() {
		  if (this.tableType != SymbolTableTypes.STATEMENT_BLOCK)
			  return id;
		  else
			  return "statement block in " + parentSymbolTable.toString();
	  }
	  
	  /*/**
	   * recursively look for a symbol table with name id in root table tree
	   * @param root root of table tree to search in
	   * @param id name of the table we're looking for
	   * @return symbol table child of root with name id if found, or null if not found.
	   */
	  private SymbolTable findChildSymbolTableRecursive(SymbolTable root, String id) {
		  for (String tableID : root.children.keySet()) {
			  if (id.equals(tableID))
				  return root.children.get(id);
			  else {
				  SymbolTable result = findChildSymbolTableRecursive(
						root.children.get(tableID), id);
				  if (result != null)
					  return result;
			  }
		  }
		  return null;
	  }
	  
	  private void moveClassChildrenToEnd() {
		  int classesCounter = 0;
		  for (int i = 0; i < sorted_children.size() - classesCounter; i++) {
			  if (sorted_children.get(i).tableType == SymbolTableTypes.CLASS) {
				  SymbolTable clsSymbolTable = sorted_children.get(i);
				  sorted_children.remove(clsSymbolTable);
				  sorted_children.add(clsSymbolTable);
				  classesCounter++;
			  }
		  }
	  }
}





