package IC.Types;

import IC.AST.ICClass;

public abstract class Type {
	protected String name;
	
	public Type(String name) 
	{
		this.name=name;
	}
	
	public abstract boolean nullAssignable();
	public abstract boolean nullComparable();
	public abstract Type clone();
	public boolean subTypeOf(Type t)
	{
		if(this.name.compareTo(t.name)==0)
			return true;
		return false;
	}
	
	public boolean isClassType() {
		return (this instanceof ClassType);
	}
	
	public boolean isArrayType() {
		return (this instanceof ArrayType);
	}
	
	public String getName()
	{
		return this.name;
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
	
	@Override
	public boolean nullAssignable() {
		return false;
	}

	@Override
	public boolean nullComparable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Type clone() {
		Type other = new IntType();
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		return false;
	}

	@Override
	public boolean nullComparable() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Type clone() {
		Type other = new BoolType();
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		return true;
	}

	@Override
	public boolean nullComparable() {
		return true;
	}
	@Override
	public Type clone() {
		Type other = new NullType();
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		return true;
	}

	@Override
	public boolean nullComparable() {
		return true;
	}
	@Override
	public Type clone() {
		Type other = new StringType();
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		return false;
	}

	@Override
	public boolean nullComparable() {
		return true;
	}
	
	@Override
	public Type clone() {
		Type other = new VoidType();
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		return true;
	}

	@Override
	public boolean nullComparable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public Type clone() {
		Type other = new ArrayType(this.elemType);
		other.name=this.name;
		return other;
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
	
	@Override
	public boolean nullAssignable() {
		System.out.println("Type file = "+ returnType.name);
		return returnType.name.equals("string") || returnType.name.equals("ArrayType") || returnType.name.equals("ClassType");
	}

	@Override
	public boolean nullComparable() {
		return returnType.name.equals("string") || returnType.name.equals("ArrayType") || returnType.name.equals("ClassType");
	}
	
	@Override
	public Type clone() {
		Type other = new MethodType(this.paramTypes,this.returnType);
		other.name=this.name;
		return other;
	}
}

class ClassType extends Type 
{   
	ICClass classAST;
	String superClass;


	public ClassType(ICClass classAST)
	{
		super("ClassType");
		this.classAST = classAST;
		this.superClass = null;
		if (classAST.hasSuperClass())
			this.superClass = classAST.getSuperClassName();
	}
	
	public String getSuperClassName() {
		return superClass;
	}

	public Boolean hasSuperClass() {
		return (superClass != null);
	}
	
	@Override
	public String toString() {
		return classAST.getName();
	}
	
	@Override
	public boolean nullAssignable() {
		return true;
	}

	@Override
	public boolean nullComparable() {
		return true;
	}
	
	@Override
	public Type clone() {
		Type other = new ClassType(this.classAST);
		other.name=this.name;
		return other;
	}
}
