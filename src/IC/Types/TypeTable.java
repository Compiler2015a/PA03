package IC.Types;

import java.util.Map;

import IC.AST.ICClass;

public class TypeTable {

	// Maps element types to array types
	private static Map<Type,ArrayType> uniqueArrayTypes;
	private static Map<String,ClassType> uniqueClassTypes;
	
	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type doubleType = new DoubleType();
	public static Type charType = new CharType();
	public static Type floatType = new FloatType();
	
	// Returns unique array type object
	public static ArrayType arrayType(Type elemType) {
		if (uniqueArrayTypes.containsKey(elemType))
		{
			// array type object already created – return it
			return uniqueArrayTypes.get(elemType);
		}
		else 
		{
			// object doesn’t exist – create and return it
			ArrayType arrt = new ArrayType(elemType);
			uniqueArrayTypes.put(elemType,arrt);
			return arrt;
		}
	}
	public static ClassType classType(ICClass classAST) {
		if (uniqueClassTypes.containsKey(classAST))
		{
			// array type object already created – return it
			return uniqueClassTypes.get(classAST);
		}
		else 
		{
			// object doesn’t exist – create and return it
			ClassType clst = new ClassType(classAST);
			uniqueClassTypes.put(classAST.getName(),clst);
			return clst;
		}
	}
}

