package IC.Types;


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
		for (Method method : icClass.getMethods()) 
			method.accept(this);
		
		return null;
	}

	@Override
	public Object visit(Field field) {
		return null;
	}
	
	public Object visitMethod(Method method) {
		for (Formal formal : method.getFormals()) 
			formal.accept(this);
				
		for (Statement statement : method.getStatements()) 
			statement.accept(this);

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
		return null;
	}

	@Override
	public Object visit(UserType type) {
		return null;
	}
	
	private boolean isTypeAssignmentValid(Type typeTo, Type typeFrom, SymbolTable scope) {
		// check if type can be assigned null
		if (typeTo.isNullAssignable() && typeFrom.isNullType()) 
			return true;
		// check if the types are equal
		if (typeTo.equals(typeFrom)) 
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
		if (typeTo != typeFrom)
			throw new TypeException("Value assigned to local variable type mismatch", assignment.getLine());
		return typeTo;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		Type typeInFact = new VoidType();
		if (returnStatement.getValue() != null) {
			typeInFact = (Type)returnStatement.getValue().accept(this);
			if (typeInFact == null)
				throw new TypeException("Return value must be of non-void type", returnStatement.getLine());
		}
		SymbolTable scope = returnStatement.getSymbolsTable();
		//System.out.println("*1"+scope.getType()+"\n");
		while (scope.getId().contains("block#")) 
			scope = scope.getParentSymbolTable();
		
		//get the return type of the match method=scope
		String typeExpected="void";
		for(SymbolEntry x: scope.getParentSymbolTable().getEntries().values())
		{
			if(x.getId().equals(scope.getId()))
			{
				typeExpected=x.getType().toString();
				typeExpected=typeExpected.substring(x.getType().toString().indexOf("->")+3);
			}
		}
		if (!typeInFact.toString().equals(typeExpected))
			throw new TypeException(String.format(
					"Return statement is not of type %s", typeExpected), returnStatement.getLine());
			
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		Type typeCondition = (Type)ifStatement.getCondition().accept(this);
		if (typeCondition == null || !typeCondition.isBoolType())
			throw new TypeException("Non boolean condition for if statement", ifStatement.getLine());
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
		if (typeCondition == null || !typeCondition.isBoolType())
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
		if (localVariable.getInitValue() != null) 
		{
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
		return location.getEntryType();
	}

	@Override
	public Object visit(ArrayLocation location) {
		Type typeIndex = (Type)location.getIndex().accept(this);
		Type typeArray = location.getEntryType();
		if (typeIndex == null || !typeIndex.isIntType())
			throw new TypeException("Array index must be an integer", location.getLine());
		if (typeArray == null)
			throw new TypeException("Array type must be of non-void type", location.getLine());
		//Type typeReturned = typeArray.clone();
		//typeReturned.decrementDimension();
		return typeArray;
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
		
		return symFromST.getType().equals("void") ? null : symFromST.getType(); // TODO ???
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
		
		
		return symFromST.getType().equals("void") ? null : symFromST.getType(); // TODO ???
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
		return newClass.getEntryType();
	}

	@Override
	public Object visit(NewArray newArray) {
		Type typeSize = (Type)newArray.getSize().accept(this);
		
		if (typeSize == null || !typeSize.isIntType())
			throw new TypeException("Array size must be an integer", newArray.getLine());

		return newArray.getEntryType();
	}

	@Override
	public Object visit(Length length) {
		Type type = (Type)length.getArray().accept(this);
		if (!type.isArrayType())
			throw new TypeException("Length expression must have an array type", length.getLine());
		return length.getEntryType();
	}

	@Override
	public Object visit(Literal literal) {
		return literal.getEntryType();
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
	
		Type type = (Type)unaryOp.getOperand().accept(this);
		if (type == null)
			throw new TypeException("Unary operator operand must be of non-void type", unaryOp.getLine());
		switch(unaryOp.getOperator()) {
			case LNEG:
				if (type.isBoolType())
					return type;
				break;
			default:
				break;
		}
		throw new TypeException("Operand of unary operator has an invalid type", unaryOp.getLine());
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		Type typeFirst = (Type)binaryOp.getFirstOperand().accept(this);
		Type typeSecond = (Type)binaryOp.getSecondOperand().accept(this);

		String onWhat = "";
		String opType = "";
		switch(binaryOp.getOperator()) {
			case LAND:
			case LOR:
				if (typeFirst.isBoolType() && typeSecond.isBoolType()) 
					return binaryOp.getEntryType();
				onWhat = "non-boolean";
				opType = "logical";
				break;
			case LT:
			case LTE:
			case GT:
			case GTE:
				if (typeFirst.isIntType() && typeSecond.isIntType()) 
					return binaryOp.getEntryType();
				onWhat = "non-integer";
				opType = "logical";
				break;
			case EQUAL:
			case NEQUAL:
				if (typeFirst.equals(typeSecond))
					return binaryOp.getEntryType();
				if ((typeFirst.isNullAssignable()) && (typeSecond.isNullType()))
					return binaryOp.getEntryType();
				if ((typeFirst.isNullType()) && (typeSecond.isNullAssignable()))
					return binaryOp.getEntryType();
				if ((typeFirst.subTypeOf(typeSecond)))
					return binaryOp.getEntryType();
				if ((typeSecond.subTypeOf(typeFirst)))
					return binaryOp.getEntryType();
				onWhat = "not-fitting";
				opType = "logical";
				break;
			default:
				break;
		}
		
		throw new TypeException(String.format("Invalid %s binary op (%s) on %s expression",
				opType, binaryOp.getOperator().toString(),
				onWhat), binaryOp.getLine());
	
	}


	@Override
	public Object visit(MathUnaryOp unaryOp) {
	
		Type type = (Type)unaryOp.getOperand().accept(this);
		if (type == null)
			throw new TypeException("Unary operator operand must be of non-void type", unaryOp.getLine());
			switch(unaryOp.getOperator()) {
			case UMINUS:
				if (type.isIntType())
					return type;
				break;
			default:
				break;
		}
		throw new TypeException("Operand of unary operator has an invalid type", unaryOp.getLine());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return (Type)expressionBlock.getExpression().accept(this);
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
				if ((typeFirst.isIntType() && typeSecond.isIntType()) 
						|| (typeFirst.isStringType() && typeSecond.isStringType())) {
						binaryOp.setEntryType(typeFirst);
						return typeFirst;
				}
				onWhat = "non-integer or non-string";
				opType = "arithmetic";
				break;
			case MINUS:
			case MULTIPLY:
			case DIVIDE:
			case MOD:
				if (typeFirst.isIntType() && typeSecond.isIntType()) 
					return typeFirst;
				onWhat = "non-integer";
				opType = "arithmetic";
				break;
				default:
					break;
		}
		throw new TypeException(String.format("Invalid %s binary op (%s) on %s expression",
				opType, binaryOp.getOperator().toString(),
				onWhat), binaryOp.getLine());
		
	}
}
