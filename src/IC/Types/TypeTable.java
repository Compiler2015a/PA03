package IC.Types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
		
		this.values = new HashMap<Type, Integer>();
	}
	
	public void printTable() {
		System.out.println("\t" + values.get(intType) + ": Primitive type: " + intType.toString());
		System.out.println("\t" + values.get(boolType) + ": Primitive type: " + boolType.toString());
		System.out.println("\t" + values.get(nullType) + ": Primitive type: " + nullType.toString());
		System.out.println("\t" + values.get(stringType) + ": Primitive type: " + stringType.toString());
		System.out.println("\t" + values.get(voidType) + ": Primitive type: " + voidType.toString());
		
		List<Map.Entry<String,ClassType>> sorted_uniqueClassTypes =
	            new ArrayList<Map.Entry<String,ClassType>>( uniqueClassTypes.entrySet() );
		Collections.sort(sorted_uniqueClassTypes, new Comparator<Map.Entry<String,ClassType>>() {
	           public int compare( Map.Entry<String,ClassType> o1, Map.Entry<String,ClassType> o2 )
	            {
	                return Integer.compare(values.get(o1.getValue()), values.get(o2.getValue()));
	            }
		});
		for (Map.Entry<String,ClassType> entry : sorted_uniqueClassTypes) 
			System.out.println("\t" + values.get(entry.getValue()) + ": Class: " + entry.getValue().toString());
		
		List<Map.Entry<Type,ArrayType>> sorted_uniqueArrayTypes =
	            new ArrayList<Map.Entry<Type,ArrayType>>( uniqueArrayTypes.entrySet() );
		Collections.sort(sorted_uniqueArrayTypes, new Comparator<Map.Entry<Type,ArrayType>>() {
	           public int compare( Map.Entry<Type,ArrayType> o1, Map.Entry<Type,ArrayType> o2 )
	            {
	                return Integer.compare(values.get(o1.getValue()), values.get(o2.getValue()));
	            }
		});
		
		for (Map.Entry<Type,ArrayType> entry : sorted_uniqueArrayTypes) 
			System.out.println("\t" + values.get(entry.getValue()) + ": Array type: " + entry.getValue().toString());
		
		List<Map.Entry<String,MethodType>> sorted_uniqueMethodTypes =
	            new ArrayList<Map.Entry<String,MethodType>>( uniqueMethodTypes.entrySet() );
		Collections.sort(sorted_uniqueMethodTypes, new Comparator<Map.Entry<String,MethodType>>() {
	           public int compare( Map.Entry<String,MethodType> o1, Map.Entry<String,MethodType> o2 )
	            {
	                return Integer.compare(values.get(o1.getValue()), values.get(o2.getValue()));
	            }
		});
		
		for (Map.Entry<String,MethodType> entry : sorted_uniqueMethodTypes) 
			System.out.println("\t" + values.get(entry.getValue()) + ": Method type: {" + entry.getValue().toString() + "}");
	}
	
	private Map<Type, Integer> values;

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
	
	public Map<Type, Integer> getValues() {
		return values;
	}
	
	public void addPrimitiveTypes() {
		this.intType = new IntType();
		this.boolType = new BoolType();
		this.nullType = new NullType();
		this.stringType = new StringType();
		this.voidType = new VoidType();
		values.put(intType, 1);
		values.put(boolType, 2);
		values.put(nullType, 3);
		values.put(stringType, 4);
		values.put(voidType, 5);
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
			clst.setSuperClassTypeId(values.get(
					uniqueClassTypes.get(classAST.getSuperClassName())));
		}
		values.put(clst, idCounter);
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
		values.put(m, idCounter);
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
		values.put(arrt, idCounter);
		idCounter++;
		return arrt;
	}
	
	private ArrayType getArrayType(Type original, int dimention) {
		Type currArrType = original;
		for (int i = 0; i < dimention; i++) 
			currArrType = uniqueArrayTypes.get(currArrType);
		
		return (ArrayType)currArrType;
	}
}

