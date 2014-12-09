package IC.SymbolsTable;

import IC.AST.*;
import IC.SemanticAnalysis.SemanticError;

public class TableScanner implements Visitor {
	
	private int blocksCounter;
	private SymbolTable currentSymbolTable;
	
	private InitializedMoreThanOnceError moreThanOnceError;
	private ExtnendingNonExistingClassError exdnsNonExistingClassEroor;
	
	public TableScanner() {
		
	}
	
	public void getSemanticError() throws SemanticError {
		if (exdnsNonExistingClassEroor != null)
			throw new SemanticError(exdnsNonExistingClassEroor.getLine(), exdnsNonExistingClassEroor.getMassage());
		if (moreThanOnceError != null)
			throw new SemanticError(moreThanOnceError.getLine(), moreThanOnceError.getMassage());
	}
	
	public void Init() {
		this.blocksCounter = 0;
		this.moreThanOnceError = null;
		this.exdnsNonExistingClassEroor = null;
	}
	
	SymbolTable getSymbolTableTree() {
		
		
		return null;
	}

	@Override
	public Object visit(Program program) {
		SymbolTable root = new SymbolTable("globals");
		this.currentSymbolTable = root;
		for (ICClass icClass : program.getClasses()) {
			if (!(Boolean)icClass.accept(this))
				return false;
		}
		return true;
	}

	@Override
	public Object visit(ICClass icClass) {
		SymbolTable virtualClsTable = new SymbolTable(icClass.getName() + "_virtual");
		SymbolTable staticClsTable = new SymbolTable(icClass.getName() + "_static");
		
		virtualClsTable.parentSymbolTable = this.currentSymbolTable;
		virtualClsTable.parentSymbolTable.children.put(icClass.getName() + "_virtual", virtualClsTable);
		virtualClsTable.parentSymbolTable.entries.put(icClass.getName(),
				new SymbolEntry(icClass.getName(), null, IDSymbolsKinds.Class));
		staticClsTable.parentSymbolTable = this.currentSymbolTable;
		staticClsTable.parentSymbolTable.children.put(icClass.getName() + "_static", staticClsTable);
	
		for (Method method : icClass.getMethods()) {
			if (method instanceof VirtualMethod) 
				this.currentSymbolTable = virtualClsTable;
			if (method instanceof StaticMethod)
				this.currentSymbolTable = staticClsTable;
			if (!(Boolean)method.accept(this))
				return false;
		}
		
		this.currentSymbolTable = virtualClsTable;
		for (Field field : icClass.getFields())  {
			if (!(Boolean)field.accept(this))
				return false;
		}
		
		this.currentSymbolTable = virtualClsTable.parentSymbolTable;
		
		if (icClass.hasSuperClass()) {
			if ((!this.currentSymbolTable.children.containsKey(icClass.getSuperClassName() + "_virtual"))  ||
					(!this.currentSymbolTable.children.containsKey(icClass.getSuperClassName() + "_static"))) {
				this.exdnsNonExistingClassEroor = new ExtnendingNonExistingClassError(
						icClass.getLine(), icClass.getSuperClassName());
				return false;
			}
			SymbolTable parentVirtualSymbolTable = this.currentSymbolTable.children.get(
					icClass.getSuperClassName() + "_virtual");
			SymbolTable parentStaticSymbolTable = this.currentSymbolTable.children.get(
					icClass.getSuperClassName() + "_static");
			for (String key : parentVirtualSymbolTable.entries.keySet()) {
				if (!virtualClsTable.entries.containsKey(key))
					virtualClsTable.entries.put(key, parentVirtualSymbolTable.entries.get(key));
			}
			for (String key : parentStaticSymbolTable.entries.keySet()) {
				if (!staticClsTable.entries.containsKey(key))
					staticClsTable.entries.put(key, parentVirtualSymbolTable.entries.get(key));
			}
		}
		
		return true;
	}

	@Override
	public Object visit(Field field) {
		if (this.currentSymbolTable.entries.containsKey(field.getName())) {
			this.moreThanOnceError = new InitializedMoreThanOnceError(
					field.getLine(), field.getName(), IDSymbolsKinds.Variable);
			return false;
		}
		this.currentSymbolTable.entries.put(field.getName(),
				new SymbolEntry(field.getName(), field.getType(), IDSymbolsKinds.Variable));
		
		field.setSymbolsTable(this.currentSymbolTable);
		
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
		if (this.currentSymbolTable.entries.containsKey(formal.getName())) {
			this.moreThanOnceError = new InitializedMoreThanOnceError(
					formal.getLine(), formal.getName(), IDSymbolsKinds.Variable);
			return false;
		}
		
		this.currentSymbolTable.entries.put(formal.getName(),
				new SymbolEntry(formal.getName(), formal.getType(), IDSymbolsKinds.Variable));
		formal.setSymbolsTable(this.currentSymbolTable);
		
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return null;
	}

	@Override
	public Object visit(UserType type) {
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.getVariable().accept(this);
		assignment.getAssignment().accept(this);
		assignment.setSymbolsTable(this.currentSymbolTable);
		return true;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().accept(this);
		callStatement.setSymbolsTable(this.currentSymbolTable);
		return true;
	}

	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue())
			returnStatement.getValue().accept(this);

		returnStatement.setSymbolsTable(this.currentSymbolTable);
		return true;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().accept(this);
		if (!(Boolean)ifStatement.getOperation().accept(this))
			return false;
		
		if (ifStatement.hasElse()) {
			if (!(Boolean)ifStatement.getElseOperation().accept(this))
				return false;
		}
		
		ifStatement.setSymbolsTable(this.currentSymbolTable);
		return true;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getCondition().accept(this);
		if (!(Boolean)whileStatement.getOperation().accept(this))
			return false;
		
		whileStatement.setSymbolsTable(this.currentSymbolTable);
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
		
		this.blocksCounter++;
		String blockId = "block" + blocksCounter;
		SymbolTable stmntBlockTable = new SymbolTable(blockId);
		stmntBlockTable.parentSymbolTable = this.currentSymbolTable;
		stmntBlockTable.parentSymbolTable.children.put(blockId, stmntBlockTable);
		this.currentSymbolTable = stmntBlockTable;
		
		for (Statement stmnt : statementsBlock.getStatements()) {
			if (!(Boolean)stmnt.accept(this))
				return false;
		}
		
		this.currentSymbolTable = stmntBlockTable.parentSymbolTable;
		statementsBlock.setSymbolsTable(this.currentSymbolTable);
		return true;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		if (this.currentSymbolTable.entries.containsKey(localVariable.getName())) {
			this.moreThanOnceError = new InitializedMoreThanOnceError(
					localVariable.getLine(), localVariable.getName(), IDSymbolsKinds.Variable);
			return false;
		}
		
		this.currentSymbolTable.entries.put(localVariable.getName(),
				new SymbolEntry(localVariable.getName(), localVariable.getType(), IDSymbolsKinds.Variable));
		
		if (localVariable.hasInitValue())
			localVariable.getInitValue().accept(this);
		localVariable.setSymbolsTable(this.currentSymbolTable);
		
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {
		if (location.isExternal()) {
			location.getLocation().accept(this);
		}
		location.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getArray().accept(this);
		location.getIndex().accept(this);
		location.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		for (Expression arg : call.getArguments())
			arg.accept(this);

		call.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		for (Expression arg : call.getArguments())
			arg.accept(this);
		
		call.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.getSize().accept(this);
		newArray.setSymbolsTable(this.currentSymbolTable);
		
		return null;
	}

	@Override
	public Object visit(Length length) {
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		binaryOp.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().accept(this);
		binaryOp.getSecondOperand().accept(this);
		binaryOp.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		unaryOp.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.getOperand().accept(this);
		unaryOp.setSymbolsTable(this.currentSymbolTable);
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return (Boolean)expressionBlock.accept(this);
	}
	
	private Object methodVisit(Method method) {
		SymbolTable methodTable = new SymbolTable(method.getName());
		methodTable.parentSymbolTable = this.currentSymbolTable;
		methodTable.parentSymbolTable.children.put(method.getName(), methodTable);
		methodTable.parentSymbolTable.entries.put(method.getName(),
				new SymbolEntry(method.getName(), method.getType(), IDSymbolsKinds.Method));
		this.currentSymbolTable = methodTable;
		
		for (Formal formal : method.getFormals()) { 
			if (!(Boolean)formal.accept(this))
				return false;
		}
		
		for (Statement stmnt : method.getStatements()) {
			if (!(Boolean)stmnt.accept(this))
				return false;
		}
		
		this.currentSymbolTable = methodTable.parentSymbolTable;
		method.setSymbolsTable(this.currentSymbolTable);
		
		return true;
	}
	
	private class ExtnendingNonExistingClassError {
		private int line;
		private String name;
		
		public ExtnendingNonExistingClassError(int line, String name) {
			this.line = line;
			this.name = name;
		}
		
		public int getLine() {
			return line;
		}
		
		public String getMassage() {
			return this.name + " class must be initialized before called to be extended";
		}
		
	}
	
	private class InitializedMoreThanOnceError {
		private int line;
		private String name;
		private IDSymbolsKinds kind;
		
		public InitializedMoreThanOnceError(int line, String name, IDSymbolsKinds kind) {
			this.line = line;
			this.name = name;
			this.kind = kind;
		}
		
		public int getLine() {
			return line;
		}
		
		public String getMassage() {
			String msg = this.kind + " " + this.name + " " + "was initialized more than once";
			return msg;
		}
		
	}
}
