package IC.Types;

import IC.AST.ICClass;

public abstract class Type {
	protected String name;
	
	public Type(String name)
	{
		this.name=name;
	}
	
	public boolean subTypeOf(Type t)
	{
		if(this.name.compareTo(t.name)==0)
			return true;
		return false;
	}
	
	public boolean isClassType() {
		return (this instanceof ClassType);
	}
	
}
class IntType extends Type 
{
	public IntType()
	{
		super("IntType");
	}
	
	@Override
	public String toString() {
		return "int";
	}
}

class BoolType extends Type 
{
	public BoolType()
	{
		super("BoolType");
	}
	
	@Override
	public String toString() {
		return "boolean";
	}
}

class NullType extends Type 
{
	public NullType()
	{
		super("NullType");
	}
	
	@Override
	public String toString() {
		return "null";
	}
}

class StringType extends Type 
{
	public StringType()
	{
		super("StringType");
	}
	
	@Override
	public String toString() {
		return "string";
	}
}

class VoidType extends Type 
{
	public VoidType()
	{
		super("VoidType");
	}
	
	@Override
	public String toString() {
		return "void";
	}
}

class ArrayType extends Type 
{
	private Type elemType;
	public ArrayType(Type elemType)
	{
		super("ArrayType");
		this.elemType=elemType;
	}
	
	@Override
	public String toString() {
		return elemType.toString() + "[]";
	}
}

class MethodType extends Type 
{  
	Type[] paramTypes;
	Type returnType;
	public MethodType(Type[] paramTypes,Type returnType)
	{
		super("MethodType");
		this.paramTypes=paramTypes;
		this.returnType=returnType;
	}
	
	@Override
	public String toString() {
		String paramTypesStr = "";
		for (int i = 0; i < paramTypes.length; i++) {
			if (i == 0)
				paramTypesStr += paramTypes[i].toString();
			else
				paramTypesStr += ", " + paramTypes[i].toString();
		}
		return paramTypesStr + " -> " + returnType.toString();
	}
}

class ClassType extends Type 
{   
	ICClass classAST;
	Integer superClassTypeId;


	public ClassType(ICClass classAST)
	{
		super("ClassType");
		this.classAST = classAST;
	}
	
	public Integer getSuperClassTypeId() {
		if (!classAST.hasSuperClass())
			return null;
		return superClassTypeId;
	}

	public void setSuperClassTypeId(Integer superClassTypeId) {
		this.superClassTypeId = superClassTypeId;
	}
	
	@Override
	public String toString() {
		return classAST.getName();
	}
}
