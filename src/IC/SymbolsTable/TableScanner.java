package IC.SymbolsTable;

import IC.AST.*;

public class TableScanner implements Visitor {
	
	private int blocksCounter;
	
	public TableScanner() {
	}
	
	public void Init() {
		this.blocksCounter = 0;
	}
	
	SymbolTable getSymbolTableTree() {
		
		
		return null;
	}

	@Override
	public Object visit(Program program) {
		SymbolTable root = new SymbolTable("globals");
		SymbolTable currentClassSymbolTable;
		for (ICClass icClass : program.getClasses()) {
			currentClassSymbolTable = (SymbolTable)icClass.accept(this);
			root.entries.put(icClass.getName(),
					new SymbolEntry(icClass.getName(), null, SymbolEntry.Kind.Class));
			currentClassSymbolTable.parentSymbolTable = root;
			root.children.add(currentClassSymbolTable);
		}
		return root;
	}

	@Override
	public Object visit(ICClass icClass) {
		SymbolTable clsTable = new SymbolTable(icClass.getName());
		SymbolTable currentClassSymbolTable;
		for (Method method : icClass.getMethods()) {
			currentClassSymbolTable = (SymbolTable)method.accept(this);
			clsTable.entries.put(method.getName(),
					new SymbolEntry(method.getName(), method.getType(), SymbolEntry.Kind.Method));
			currentClassSymbolTable.parentSymbolTable = clsTable;
			clsTable.children.add(currentClassSymbolTable);
		}
		
		for (Field field : icClass.getFields()) {
			clsTable.entries.put(field.getName(),
					new SymbolEntry(field.getName(), field.getType(), SymbolEntry.Kind.Varable));
		}
		
		return clsTable;
	}

	@Override
	public Object visit(Field field) {
		return null;
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

	@Override
	public Object visit(Assignment assignment) {
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		this.blocksCounter++;
		String blockId = "block" + blocksCounter;
		SymbolTable stmntBlockTable = new SymbolTable(blockId);
		for (Statement stmnt : statementsBlock.getStatements()) {
			if (stmnt instanceof LocalVariable) {
				LocalVariable localVar = (LocalVariable)stmnt;
				stmntBlockTable.entries.put(localVar.getName(),
						new SymbolEntry(localVar.getName(), localVar.getType(), SymbolEntry.Kind.Varable));
			}
		}
		return stmntBlockTable;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
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
		return null;
	}

	@Override
	public Object visit(Length length) {
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return null;
	}
	
	private Object methodVisit(Method method) {
		SymbolTable methodTable = new SymbolTable(method.getName());
		SymbolTable currentClassSymbolTable;
		for (Formal formal : method.getFormals()) {
			methodTable.entries.put(formal.getName(),
					new SymbolEntry(formal.getName(), formal.getType(), SymbolEntry.Kind.Varable));
		}
		
		for (Statement stmnt : method.getStatements()) {
			if (stmnt instanceof LocalVariable) {
				LocalVariable localVar = (LocalVariable)stmnt;
				methodTable.entries.put(localVar.getName(),
						new SymbolEntry(localVar.getName(), localVar.getType(), SymbolEntry.Kind.Varable));
			}
			if (stmnt instanceof StatementsBlock) {
				currentClassSymbolTable = (SymbolTable)stmnt.accept(this);
				
				currentClassSymbolTable.parentSymbolTable = methodTable;
				methodTable.children.add(currentClassSymbolTable);
			}
		}
		
		return methodTable;
	}
}
