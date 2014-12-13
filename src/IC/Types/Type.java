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

}
class IntType extends Type 
{
	public IntType()
	{
		super("IntType");
	}
}

class BoolType extends Type 
{
	public BoolType()
	{
		super("BoolType");
	}
}

class StringType extends Type 
{
	public StringType()
	{
		super("StringType");
	}
}

class DoubleType extends Type 
{
	public DoubleType()
	{
		super("DoubleType");
	}
}

class CharType extends Type 
{
	public CharType()
	{
		super("CharType");
	}
}

class FloatType extends Type 
{
	public FloatType()
	{
		super("FloatType");
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
}

class ClassType extends Type 
{   
	ICClass classAST;
	public ClassType(ICClass classAST)
	{
		super("ClassType");
		this.classAST=classAST;
	}
}
