package IC.SymbolsTable;

public enum IDSymbolsKinds {
	 //Class, Method, Variable, Field;
	
	CLASS("Class"),
	FIELD("Field"),
	STATIC_METHOD("Static Method"),
	VIRTUAL_METHOD("Virtual Method"),
	METHOD("Method"),
	PARAMETER("Parameter"),
	VARIABLE("Variable");
	
	 private final String repr;       

		private IDSymbolsKinds(String s) {
			repr = s;
		}

		public String toString(){
		    return repr;
		 }
}
	