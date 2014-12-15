package IC.Types;

import java.util.ArrayList;
import java.util.List;

import IC.DataTypes;
import IC.LiteralTypes;
import IC.AST.*;
import IC.SemanticAnalysis.SemanticErrorThrower;
import IC.SymbolsTable.IDSymbolsKinds;
import IC.SymbolsTable.SymbolEntry;
import IC.SymbolsTable.SymbolTable;

public class TypeValidator implements Visitor{

	private int loopNesting;
	@Override
	public Object visit(Program program) {
		loopNesting = 0;
		for (ICClass cls : program.getClasses()) {
			cls.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		
		
		for (Method method : icClass.getMethods()) {
			
			method.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(Field field) {
		return null;
	}
	
	public Object visitMethod(Method method) {
		
		for (Statement statement : method.getStatements()) {
			statement.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		return visitMethod(method);
	}

	@Override
	public Object visit(StaticMethod method) {
		return visitMethod(method);
	}

	@Override
	public Object visit(LibraryMethod method) {
		return visitMethod(method);
	}

	@Override
	public Object visit(Formal formal) {
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return type;
	}

	@Override
	public Object visit(UserType type) {
		return type;
	}
	
	private boolean isTypeAssignmentValid(Type typeTo, Type typeFrom, SymbolTable scope) {
		// check if type can be assigned null
		if (typeTo.nullAssignable() && typeFrom.equals("void")) 
			return true;
		// check if the types are equal
		if (typeTo.getName().equals(typeFrom.getName())) 
			return true;
		// check hierarchy (don't allow object array subtyping)
		if (scope.isTypeOf(typeTo.getName(), typeFrom.getName()) &&
				!typeFrom.isArrayType())
			return true;
		return false;
	}

	@Override
	public Object visit(Assignment assignment) {
		Type typeTo = (Type)assignment.getVariable().accept(this);
		Type typeFrom = (Type)assignment.getAssignment().accept(this);
		if (typeTo==null || typeFrom == null)
			throw new TypeException("Assignment variable and value must be of non-void type", assignment.getLine());
		
		return typeTo;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		IC.AST.Type typeInFact = new PrimitiveType(returnStatement.getLine(), DataTypes.VOID); 
		if (returnStatement.getValue() != null) {
			typeInFact = (IC.AST.Type)returnStatement.getValue().accept(this);
			if (typeInFact == null)
				throw new TypeException("Return value must be of non-void type", returnStatement.getLine());
		}
		SymbolTable scope = returnStatement.getSymbolsTable();
		while (scope.getType() != IDSymbolsKinds.STATIC_METHOD &&
				scope.getType() != IDSymbolsKinds.VIRTUAL_METHOD) {
			scope = scope.getParentSymbolTable();
		}
		// get the method symbol from the class scope, and infer the type
		IDSymbolsKinds typeExpected = scope.getType();
		
		if (typeInFact.equals(typeExpected) == false)
			throw new TypeException(String.format(
					"Return statement is not of type %s", typeExpected), returnStatement.getLine());
			
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().setSymbolsTable(ifStatement.getSymbolsTable());
		IC.AST.Type typeCondition = (IC.AST.Type)ifStatement.getCondition().accept(this);
		
		if (typeCondition == null || typeCondition.getName().equals("boolean") == false)
			throw new TypeException("Non boolean condition for if statement", ifStatement.getLine());
		ifStatement.getOperation().setSymbolsTable(ifStatement.getSymbolsTable());
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
		{
			ifStatement.getElseOperation().setSymbolsTable(ifStatement.getSymbolsTable());
			ifStatement.getElseOperation().accept(this);
		}
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		Type typeCondition = (Type)whileStatement.getCondition().accept(this);
		if (typeCondition == null || typeCondition.equals("boolean") == false)
			throw new TypeException("Non boolean condition for while statement", whileStatement.getLine());
		loopNesting++;
		whileStatement.getOperation().accept(this);
		loopNesting--;
		return null;
	}
	
	private boolean isBreakContinueValid() {
		return loopNesting > 0;
	}

	@Override
	public Object visit(Break breakStatement) {
		if (isBreakContinueValid() == false)
			throw new TypeException("Use of 'break' statement outside of loop not allowed", 
					breakStatement.getLine());
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		if (isBreakContinueValid() == false)
			throw new TypeException("Use of 'continue' statement outside of loop not allowed", 
					continueStatement.getLine());
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for (Statement statement : statementsBlock.getStatements()) {
			statement.accept(this);
		}
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		if (localVariable.getInitValue() != null) {
			Type type = (Type)localVariable.getInitValue().accept(this);
			if (type == null) 
				throw new TypeException("Initializing value must be of non-void type", localVariable.getLine());
			if (isTypeAssignmentValid(localVariable.getEntryType(), type, localVariable.getSymbolsTable()) == false) {
				throw new TypeException("Value assigned to local variable type mismatch", localVariable.getLine());
			} 
		}

		return null;
	}
	
	void validateClassScope(SymbolTable scope, ClassType object, String name, int line) {
		if (scope == null) {
			if (object == null || object.name.equals("Library"))
				throw new TypeException(String.format("Method %s doesn't exist", name), line);
			else 
				throw new TypeException("Unable to find class scope", line);
				
		}
	}

	@Override
	public Object visit(VariableLocation location) {
		/*if (location.isExternal()) 
		{
			System.out.println("111\n");
			return location.getLocation().accept(this);
		}
		System.out.println("222\n");
		return true;*/
		SymbolEntry sym = location.getSymbolsTable().getEntry(location.getName());
		if (sym.getKind() ==IDSymbolsKinds.FIELD) {
			if (location.getSymbolsTable().getType() == IDSymbolsKinds.STATIC_METHOD)
				throw new TypeException("Use of field inside static method is not allowed", location.getLine());
		}
		return sym.getType();

	}

	@Override
	public Object visit(ArrayLocation location) {
		IC.AST.Type typeIndex = (IC.AST.Type)location.getIndex().accept(this);
		IC.AST.Type typeArray = (IC.AST.Type)location.getArray().accept(this);
		if (typeIndex == null || typeIndex.equals("int") == false)
			throw new TypeException("Array index must be an integer", location.getLine());
		if (typeArray == null)
			throw new TypeException("Array type must be of non-void type", location.getLine());
		IC.AST.Type typeReturned = typeArray.clone();
		typeReturned.decrementDimension();
		return typeReturned;
	}

	@Override
	public Object visit(StaticCall call) {
		
		SymbolTable scope = call.getSymbolsTable();
		while (scope.getParentSymbolTable() != null)
			scope = scope.getParentSymbolTable();
		scope = scope.getClassScope(call.getClassName());
		validateClassScope(scope, null, call.getName(), call.getLine());

		
		// figure out if the name even exists
		SymbolEntry symFromST = scope.getEntry(call.getName());
		if (symFromST == null) {
			throw new TypeException(String.format("Method %s doesn't exist", 
					call.getName()), call.getLine());
		}
		
		return symFromST.getType().equals("void") ? null : symFromST.getType();
	}

	@Override
	public Object visit(VirtualCall call) {
		
		SymbolTable scope = call.getSymbolsTable();
		Object typeObject = null;
		String prefixClass = "";
		if (call.getLocation() != null) {
			typeObject = call.getLocation().accept(this);
			if (typeObject instanceof ClassType == false)
				throw new TypeException("Object is not of class type", call.getLine());
			prefixClass = ((ClassType)typeObject).name + ".";
			while (scope.getParentSymbolTable() != null)
				scope = scope.getParentSymbolTable();
			scope = scope.getClassScope(((ClassType)typeObject).name);
			validateClassScope(scope, (ClassType)typeObject, call.getName(), call.getLine());
		}
		else {
			if (scope.getType() == IDSymbolsKinds.STATIC_METHOD) {
				SymbolEntry symFromSTTemp = scope.getEntry(call.getName());
				if (symFromSTTemp == null || symFromSTTemp.getKind() == IDSymbolsKinds.VIRTUAL_METHOD)
					throw new TypeException(
							"Calling a local virtual method from inside a static method is not allowed", call.getLine());	
				
			}
		}
		
		// figure out if the name even exists
		SymbolEntry symFromST = scope.getEntry(call.getName());
		if (symFromST == null) {
			if (call.getLocation() == null) {
				throw new TypeException(String.format(
						"%s not found in symbol table", call.getName()), call.getLine());
			}
			throw new TypeException(String.format("Method %s.%s not found in type table", 
					((ClassType)typeObject).name, call.getName()), call.getLine());

		}
		
		
		return symFromST.getType().equals("void") ? null : symFromST.getType();
	}

	@Override
	public Object visit(This thisExpression) {
		
		SymbolTable scope = thisExpression.getSymbolsTable();
		if (scope.getType() == IDSymbolsKinds.STATIC_METHOD)
			throw new TypeException("Use of 'this' expression inside static method is not allowed", thisExpression.getLine());
		while (scope.getType() != IDSymbolsKinds.CLASS) {
			scope = scope.getParentSymbolTable();
			if (scope == null)
				throw new TypeException("this keyword out of class context", thisExpression.getLine());
		}
		return new UserType(thisExpression.getLine(), scope.getId());
	}

	@Override
	public Object visit(NewClass newClass) {
		
		SymbolTable scope = newClass.getSymbolsTable();
		while (scope.getParentSymbolTable() != null)
			scope = scope.getParentSymbolTable();
		scope = scope.getClassScope(newClass.getName());
		if (scope == null)
			throw new TypeException("Unknown class name to be created", newClass.getLine());
		return new UserType(newClass.getLine(), newClass.getName());
	}

	@Override
	public Object visit(NewArray newArray) {
	
		IC.AST.Type typeSize = (IC.AST.Type)newArray.getSize().accept(this);
		IC.AST.Type typeArray = (IC.AST.Type)newArray.getType().accept(this);
		if (typeSize == null || typeSize.getName().equals("int") == false)
			throw new TypeException("Array size must be an integer", newArray.getLine());
		IC.AST.Type typeReturned = typeArray.clone();
		typeReturned.incrementDimension();
		return typeReturned;
	}

	@Override
	public Object visit(Length length) {
	
		return new PrimitiveType(length.getLine(), DataTypes.INT);
	}

	@Override
	public Object visit(Literal literal) {
		
		if(literal.getType().toString().equals("INTEGER"))
			return new IntType();//(literal.getLine(), DataTypes.INT);	
		if(literal.getType().toString().equals("STRING"))
			return new PrimitiveType(literal.getLine(), DataTypes.STRING);	
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
	
		IC.AST.Type type = (IC.AST.Type)unaryOp.getOperand().accept(this);
		if (type == null)
			throw new TypeException("Unary operator operand must be of non-void type", unaryOp.getLine());
		switch(unaryOp.getOperator()) {
		case UMINUS:
			if (type.getName().equals("int"))
				return new PrimitiveType(0, DataTypes.INT);
			break;
		}
		throw new TypeException("Operand of unary operator has an invalid type", unaryOp.getLine());
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		
		Type typeFirst = (Type)binaryOp.getFirstOperand().accept(this);
		Type typeSecond = (Type)binaryOp.getSecondOperand().accept(this);
				if (typeFirst == null || typeSecond == null)
			throw new TypeException("Binary operator operands must be of non-void type", binaryOp.getLine());
		
		
		String onWhat = "";
		String opType = "";
		switch(binaryOp.getOperator()) {
		case LAND:
		case LOR:
			if (typeFirst.getName().equals("boolean") && typeSecond.getName().equals("boolean")) 
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			onWhat = "non-boolean";
			opType = "logical";
			break;
		case LT:
		case LTE:
		case GT:
		case GTE:
			if (typeFirst.getName().equals("int") && typeSecond.getName().equals("int")) 
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			onWhat = "non-integer";
			opType = "logical";
			break;
		case EQUAL:
		case NEQUAL:
			if (typeFirst.equals(typeSecond))
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			if (typeFirst.nullComparable() && typeSecond.getName().equals("void"))
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			if (typeFirst.getName().equals("void") && typeSecond.nullComparable()) 
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			onWhat = "not-fitting";
			opType = "logical";
			break;
			}
		
		throw new TypeException(String.format("Invalid %s binary op (%s) on %s expression",
				opType, binaryOp.getOperator().toString(),
				onWhat), binaryOp.getLine());
	
	}


	@Override
	public Object visit(MathUnaryOp unaryOp) {
	
		IC.AST.Type type = (IC.AST.Type)unaryOp.getOperand().accept(this);
		if (type == null)
			throw new TypeException("Unary operator operand must be of non-void type", unaryOp.getLine());
		switch(unaryOp.getOperator()) {
		case LNEG:
			if (type.getName().equals("boolean"))
				return new PrimitiveType(0, DataTypes.BOOLEAN);
			break;
		}
		throw new TypeException("Operand of unary operator has an invalid type", unaryOp.getLine());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Object visit(MathBinaryOp binaryOp) {
		Type typeFirst = (Type)binaryOp.getFirstOperand().accept(this);
		Type typeSecond = (Type)binaryOp.getSecondOperand().accept(this);
		
		if (typeFirst == null || typeSecond == null)
			throw new TypeException("Binary operator operands must be of non-void type", binaryOp.getLine());
		String onWhat = "";
		String opType = "";
		
		switch(binaryOp.getOperator()) {
	case PLUS:
		if (typeFirst.getName().equals("IntType") && typeSecond.getName().equals("IntType")) 
			return new IntType();
		if (typeFirst.getName().equals("StringType") && typeSecond.getName().equals("StringType")) 
			return new StringType();
		onWhat = "non-integer or non-string";
		opType = "arithmetic";
		break;
	case MINUS:
	case MULTIPLY:
	case DIVIDE:
	case MOD:
		if (typeFirst.getName().equals("IntType") && typeSecond.getName().equals("IntType")) 
			return new IntType();
		onWhat = "non-integer";
		opType = "arithmetic";
		break;
		}
		throw new TypeException(String.format("Invalid %s binary op (%s) on %s expression",
				opType, binaryOp.getOperator().toString(),
				onWhat), binaryOp.getLine());
		
	}
}
