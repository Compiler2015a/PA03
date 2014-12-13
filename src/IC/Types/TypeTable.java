package IC.Types;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.DataTypes;

public class TypeTable {

	public TypeTable() {
		idCounter = 0;
		
		this.uniqueArrayTypes = new HashMap<Type, ArrayType>();
		this.uniqueClassTypes = new HashMap<String,ClassType>();
		this.uniqueMethodTypes = new HashMap<String,MethodType>();
		
		this.values = new HashMap<Integer, Type>();
	}
	
	public void printTable() {
		
	}
	
	private Map<Integer, Type> values;

	// Maps element types to array types
	private Map<Type,ArrayType> uniqueArrayTypes;
	private Map<String,ClassType> uniqueClassTypes;
	private Map<String,MethodType> uniqueMethodTypes;
	
	private Type intType;
	private Type boolType;
	private Type nullType;
	private Type stringType;
	private Type voidType;
	
	private int idCounter;
	
	public Map<Integer, Type> getValues() {
		return values;
	}
	
	public void addPrimitiveTypes() {
		this.intType = new IntType();
		this.boolType = new BoolType();
		this.nullType = new NullType();
		this.stringType = new StringType();
		this.voidType = new VoidType();
		values.put(1, intType);
		values.put(2, boolType);
		values.put(3, nullType);
		values.put(4, stringType);
		values.put(5, voidType);
		this.idCounter = 6;
	}
	
	public void addArrayType(IC.AST.Type typeNode) {
		Type currArrType;
		if (typeNode instanceof PrimitiveType) 
			currArrType = getPrimitiveType(typeNode.getName()); 
		else
			currArrType = uniqueClassTypes.get(typeNode.getName());
		
		for (int i = 0; i < typeNode.getDimension(); i++) 
			currArrType = addAndReturnArraySingleType(currArrType);
	}
	
	public void addClassType(ICClass classAST) {
		if (uniqueClassTypes.containsKey(classAST))
			return;
		ClassType clst = new ClassType(classAST);
		uniqueClassTypes.put(classAST.getName(), clst);
		if (classAST.hasSuperClass()) {
			if (!uniqueClassTypes.containsKey(classAST.getSuperClassName())) {
				// TODO Add error handling of extending a non existing class
			}
			clst.setSuperClassTypeId(findIdOfType(
					uniqueClassTypes.get(classAST.getSuperClassName())));
		}
		values.put(idCounter, clst);
		idCounter++;
	}
	
	public void addMethodType(Method mNode) {
		Type[] params = new Type[mNode.getFormals().size()];
		List<Formal> formals = mNode.getFormals(); 
		for (int i = 0; i < params.length; i++)
			params[i] = getTypeFromASTTypeNode(formals.get(i).getType());
		MethodType m = new MethodType(params, getTypeFromASTTypeNode(mNode.getType()));
		if (uniqueMethodTypes.containsKey(m.toString()))
			return;
		uniqueMethodTypes.put(m.toString(), m);
		values.put(idCounter, m);
		idCounter++;
	}
	
	private Type getPrimitiveType(String dataTypeName) {
		if (dataTypeName == DataTypes.INT.getDescription())
			return intType;
		if (dataTypeName == DataTypes.STRING.getDescription())
			return stringType;
		if (dataTypeName == DataTypes.VOID.getDescription())
			return voidType;
		if (dataTypeName == DataTypes.BOOLEAN.getDescription())
			return boolType;
		
		return null;
	}
	
	private Type getTypeFromASTTypeNode(IC.AST.Type typeNode) {
		if (typeNode instanceof PrimitiveType) {
			PrimitiveType pt = (PrimitiveType)typeNode;
			Type primitive = getPrimitiveType(typeNode.getName());
			if (pt.getDimension() == 0) 
				return primitive;
			else
				return getArrayType(primitive, pt.getDimension());
		}
		else {
			UserType ut = (UserType)typeNode;
			Type clsType = uniqueClassTypes.get(ut.getName());
			if (ut.getDimension() == 0)
				return clsType;
			else 
				return getArrayType(clsType, ut.getDimension());
		}
	}
	
	private ArrayType addAndReturnArraySingleType(Type elemType) {
		if (uniqueArrayTypes.containsKey(elemType))
			return uniqueArrayTypes.get(elemType);
		
		ArrayType arrt = new ArrayType(elemType);
		uniqueArrayTypes.put(elemType, arrt);
		values.put(idCounter, arrt);
		idCounter++;
		return arrt;
	}
	
	private ArrayType getArrayType(Type original, int dimention) {
		Type currArrType = original;
		for (int i = 0; i < dimention; i++) 
			currArrType = uniqueArrayTypes.get(currArrType);
		
		return (ArrayType)currArrType;
	}
	
	private int findIdOfType(Type type) {
		for (int key : values.keySet())
			if (values.get(key).equals(type))
				return key;
		
		return -1;
	}

}

