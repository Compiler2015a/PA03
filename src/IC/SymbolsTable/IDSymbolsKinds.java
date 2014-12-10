package IC.SymbolsTable;

public enum IDSymbolsKinds {
	 //Class, Method, Variable, Field;
	
	CLASS("Class"),
	STATIC_METHOD("Static Method"),
	VIRTUAL_METHOD("Virtual Method"),
	METHOD("Method"),
	VARIABLE("Variable");
	
	 private final String repr;       

		private IDSymbolsKinds(String s) {
			repr = s;
		}

		public String toString(){
		    return repr;
		 }
}
	