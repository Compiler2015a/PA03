package IC.SemanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import IC.SymbolsTable.*;
import IC.AST.*;

public class SymbolsInstanceAnalyzer implements Visitor{
	
	private NotInctanciatedError semanticErrorMsg;
	
	public SymbolsInstanceAnalyzer() {
	}
	
	public void getSemanticError() throws SemanticError {
		throw new SemanticError(semanticErrorMsg.getLine(), semanticErrorMsg.getMassage());
	}
	
	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses())
			if (!(Boolean)icClass.accept(this))
				return false;
			
		return true;
	}
	@Override
	public Object visit(ICClass icClass) {
		for (Method method : icClass.getMethods())
			if (!(Boolean)method.accept(this))
				return false;
		
		return true;
	}
	
	@Override
	public Object visit(Field field) {
		return true;
	}
	
	@Override
	public Object visit(VirtualMethod method) {
		return methodVisit(method);
	}
	
	@Override
	public Object visit(StaticMethod method) {
		return methodVisit(method);
	}
	
	@Override
	public Object visit(LibraryMethod method) {
		return methodVisit(method);
	}
	
	@Override
	public Object visit(Formal formal) {
		return true;
	}
	
	@Override
	public Object visit(PrimitiveType type) {
		return true;
	}
	
	@Override
	public Object visit(UserType type) {
		return true;
	}
	
	@Override
	public Object visit(Assignment assignment) {
		if (!(Boolean)assignment.getVariable().accept(this))
			return false;
		
		if (!(Boolean)assignment.getAssignment().accept(this))
			return false;
		
		return true;
	}
	
	@Override
	public Object visit(CallStatement callStatement) {	
		return (Boolean)callStatement.getCall().accept(this);
	}
	
	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue())
			return (Boolean)returnStatement.getValue().accept(this);
		
		return true;
	}
	
	@Override
	public Object visit(If ifStatement) {
		if (!(Boolean)ifStatement.getCondition().accept(this))
			return false;
		
		if (!(Boolean)ifStatement.getOperation().accept(this))
			return false;
		
		if (ifStatement.hasElse())
			if (!(Boolean)ifStatement.getElseOperation().accept(this))
				return false;
		
		return true;
	}
	
	@Override
	public Object visit(While whileStatement) {
		if (!(Boolean)whileStatement.getCondition().accept(this))
			return false;
		
		if (!(Boolean)whileStatement.getOperation().accept(this))
			return false;
		
		return true;
	}
	
	@Override
	public Object visit(Break breakStatement) {
		return true;
	}
	
	@Override
	public Object visit(Continue continueStatement) {
		return true;
	}
	
	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for (Statement stmnt : statementsBlock.getStatements())
		{
			if (!(Boolean)stmnt.accept(this))
				return false;
		}
		
		return true;
	}
	
	@Override
	public Object visit(LocalVariable localVariable) {
		return true;
	}
	
	@Override
	public Object visit(VariableLocation location) {
		if(location.isExternal())
			if (!(Boolean)location.getLocation().accept(this))
				return false;
		
		if (!tableLookUp(location.getSymbolsTable(), location.getName(), IDSymbolsKinds.VARIABLE)) {
			this.semanticErrorMsg = new NotInctanciatedError
					(location.getLine(), location.getName(), IDSymbolsKinds.VARIABLE);
				return false;
		}
		
		return true;
	}
	
	@Override
	public Object visit(ArrayLocation location) {
		return (Boolean)location.getArray().accept(this);
	}
	
	@Override
	public Object visit(StaticCall call) {
		if (!tableLookUp(call.getSymbolsTable(), call.getClassName(), IDSymbolsKinds.CLASS)) {
			this.semanticErrorMsg = new NotInctanciatedError
					(call.getLine(), call.getClassName(), IDSymbolsKinds.CLASS);
			return false;
		}
		if (!tableLookUp(call.getSymbolsTable(), call.getName(), IDSymbolsKinds.STATIC_METHOD)) {
			this.semanticErrorMsg = new NotInctanciatedError
					(call.getLine(), call.getClassName(), IDSymbolsKinds.STATIC_METHOD);
			return false;
		}
		
		for (Expression arg : call.getArguments()) {
			if (!(Boolean)arg.accept(this))
				return false;
		}
		
		return true;
	}
	
	@Override
	public Object visit(VirtualCall call) {
		if (call.isExternal())
			if (!(Boolean)call.getLocation().accept(this))
				return false;
		
		if (!tableLookUp(call.getSymbolsTable(), call.getName(), IDSymbolsKinds.VIRTUAL_METHOD)) {
			this.semanticErrorMsg = new NotInctanciatedError
					(call.getLine(), call.getName(), IDSymbolsKinds.VIRTUAL_METHOD);
			return false;
		}
		
		for (Expression arg : call.getArguments()) {
			if (!(Boolean)arg.accept(this))
				return false;
		}
		return true;
	}
	
	@Override
	public Object visit(This thisExpression) {
		return true;
	}
	
	@Override
	public Object visit(NewClass newClass) {
		return true;
	}
	
	@Override
	public Object visit(NewArray newArray) {
		return true;
	}
	
	@Override
	public Object visit(Length length) {
		return true;
	}
	
	@Override
	public Object visit(MathBinaryOp binaryOp) {
		return true;
	}
	
	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		return true;
	}
	
	@Override
	public Object visit(MathUnaryOp unaryOp) {
		return true;
	}
	
	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		return true;
	}
	
	@Override
	public Object visit(Literal literal) {
		return true;
	}
	
	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return (Boolean)expressionBlock.getExpression().accept(this);
	}
	
	private Object methodVisit(Method method) {
		for (Statement stmnt : method.getStatements()) {
			if (!(Boolean)stmnt.accept(this))
				return false;
		}
		
		return true;
	}
	
	private Boolean tableLookUp(SymbolTable symbolsTable, String varId, IDSymbolsKinds kind) {
		if (symbolsTable == null)
			return false;
		
		if (symbolsTable.entries.containsKey(varId))
			if (symbolsTable.entries.get(varId).getKind() == kind)
				return true;
		
		return tableLookUp(symbolsTable.parentSymbolTable, varId, kind);
	}
	
	private class NotInctanciatedError {
		private int line;
		private String name;
		private IDSymbolsKinds kind;
		
		public NotInctanciatedError(int line, String name, IDSymbolsKinds kind) {
			this.line = line;
			this.name = name;
			this.kind = kind;
		}
		
		public int getLine() {
			return line;
		}
		
		public String getMassage() {
			String msg = this.kind + " " + this.name + " " + "was not found";
			return msg;
		}
	}
}
