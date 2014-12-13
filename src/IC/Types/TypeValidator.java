package IC.Types;

import IC.DataTypes;
import IC.AST.*;
import IC.SymbolsTable.IDSymbolsKinds;
import IC.SymbolsTable.SymbolEntry;
import IC.SymbolsTable.SymbolTable;

public class TypeValidator implements Visitor
{
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

	private boolean isTypeAssignmentValid(IC.AST.Type typeTo, IC.AST.Type  typeFrom, SymbolTable table) {
		// check if type can be assigned null
		if (typeTo.nullAssignable() && typeFrom.equals("void")) 
			return true;
		
		// check if the types are equal
		if (typeTo.equals(typeFrom)) 
			return true;

		
		// check hierarchy (don't allow object array subtyping)
		if (table.isTypeOf(typeTo.getName(), typeFrom.getName()) &&
				typeFrom.getDimension() == 0 &&
				typeTo.getDimension() == 0) 
			return true;

		return false;
	}
	
	@Override
	public Object visit(Assignment assignment) {
		IC.AST.Type typeTo = (IC.AST.Type)assignment.getVariable().accept(this);
		IC.AST.Type typeFrom = (IC.AST.Type)assignment.getAssignment().accept(this);
		if (typeTo == null || typeFrom == null)
			throw new TypeException("Assignment variable and value must be of non-void type", assignment.getLine());
		if (isTypeAssignmentValid(typeTo, typeFrom, assignment.getSymbolsTable()) == false) {
			throw new TypeException(String.format("Invalid assignment of type %s to variable of type %s",
					typeFrom, typeTo), assignment.getLine());
		}
		return typeTo;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return callStatement.getCall().accept(this);
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
				return null;
	}

	@Override
	public Object visit(If ifStatement) {
		Type typeCondition = (Type)ifStatement.getCondition().accept(this);
		if (typeCondition == null || typeCondition.equals("boolean") == false)
			throw new TypeException("Non boolean condition for if statement", ifStatement.getLine());
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
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
		// TODO Auto-generated method stub
				return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
				return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}
	
}