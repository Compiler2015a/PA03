package IC.SymbolsTable;

import java.util.LinkedList;
import java.util.Queue;

import IC.SemanticAnalysis.SemanticError;
import IC.SemanticAnalysis.SemanticErrorThrower;
import IC.Types.*;
import IC.AST.*;

public class SymbolsTableBuilder implements Visitor {
	
	private Queue<ASTNode> nodeHandlingQueue;
	private SymbolTable rootSymbolTable;

	private SymbolTable currentClassSymbolTablePoint;
	
	private TypeTable typeTable;
	
	private SemanticErrorThrower semanticErrorThrower;
	
	int blockCounter;
	
	public SymbolsTableBuilder(TypeTable typeTable, String tableId) {
		this.nodeHandlingQueue = new LinkedList<ASTNode>();
		this.rootSymbolTable = new SymbolTable(tableId, SymbolTableTypes.GLOBAL);
		this.currentClassSymbolTablePoint = null;
		
		this.typeTable = typeTable;
		
		this.semanticErrorThrower = null;
	}

	public SymbolTable getSymbolTable() {
		return rootSymbolTable;
	}
	
	public void buildSymbolTables(Program root) throws SemanticError {
		nodeHandlingQueue.add(root);
		ASTNode currentNode;
		this.blockCounter = 0;
		while (!nodeHandlingQueue.isEmpty()) {
			currentNode = nodeHandlingQueue.poll();
			if (!(Boolean)currentNode.accept(this)) 
				semanticErrorThrower.execute();
		}
	}
	
	@Override
	public Object visit(Program program) {
		SymbolTable programSymbolTable = this.rootSymbolTable;
		for (ICClass iccls : program.getClasses()) {
			nodeHandlingQueue.add(iccls);
			if (!addEntryAndCheckDuplication(programSymbolTable, 
					new SymbolEntry(iccls.getName(), typeTable.uniqueClassTypes.get(iccls.getName()), 
							IDSymbolsKinds.CLASS))) {
				this.semanticErrorThrower = new SemanticErrorThrower(
						iccls.getLine(), "class " + iccls.getName() + " is declared more than once");
				return false;
			}
			SymbolTable icclsParentSymbolTable;
			if (iccls.hasSuperClass()) 
				icclsParentSymbolTable = programSymbolTable.findChildSymbolTable(iccls.getSuperClassName());
			else
				icclsParentSymbolTable = programSymbolTable;
			iccls.setSymbolsTable(icclsParentSymbolTable);
			
			SymbolTable currentClassSymbolTable = new SymbolTable(iccls.getName(), SymbolTableTypes.CLASS);
			currentClassSymbolTable.setParentSymbolTable(icclsParentSymbolTable);
			icclsParentSymbolTable.addTableChild(iccls.getName(), currentClassSymbolTable);
		}
		return true;
	}

	@Override
	public Object visit(ICClass icClass) {
		SymbolTable currentClassSymbolTable = this.rootSymbolTable.findChildSymbolTable(icClass.getName());
		for (Field field : icClass.getFields()) {
			nodeHandlingQueue.add(field);
			if (!addEntryAndCheckDuplication(currentClassSymbolTable, 
					new SymbolEntry(field.getName(), 
							typeTable.getTypeFromASTTypeNode(field.getType()), IDSymbolsKinds.FIELD))) {
				this.semanticErrorThrower = new SemanticErrorThrower(
						field.getLine(), "field " + field.getName() + " is declared more than once");
				return false;
			}
			field.setSymbolsTable(currentClassSymbolTable);
		}
		
		for (Method method : icClass.getMethods()) {
			nodeHandlingQueue.add(method);
			if (!addEntryAndCheckDuplication(currentClassSymbolTable, 
					new SymbolEntry(method.getName(), 
							typeTable.getMethodType(method), getMethodKind(method)))) {
				this.semanticErrorThrower = new SemanticErrorThrower(
						method.getLine(), "method " + method.getName() + " is declared more than once");
				return false;
			}
			method.setSymbolsTable(currentClassSymbolTable);
			SymbolTable currentMethodSymbolTable = new SymbolTable(method.getName(), SymbolTableTypes.METHOD);
			currentMethodSymbolTable.setParentSymbolTable(currentClassSymbolTable);
			currentClassSymbolTable.addTableChild(method.getName(), currentMethodSymbolTable);
		}
		
		return true;
	}

	@Override
	public Object visit(Field field) {
		return true;
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
		if (!addEntryAndCheckDuplication(formal.getSymbolsTable(), 
				new SymbolEntry(formal.getName(), typeTable.getTypeFromASTTypeNode(formal.getType()), IDSymbolsKinds.FORMAL))) {
			this.semanticErrorThrower = new SemanticErrorThrower(
					formal.getLine(), "formal " + formal.getName() + " is declared more than once");
			return false;
		}
		
		return true;
	}

	@Override
	public Object visit(PrimitiveType type) {
		// not called
		return null;
	}

	@Override
	public Object visit(UserType type) {
		// not called
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		assignment.getVariable().setSymbolsTable(assignment.getSymbolsTable());
		if (!(Boolean)assignment.getVariable().accept(this))
			return false;
		assignment.getAssignment().setSymbolsTable(assignment.getSymbolsTable());
		if (!(Boolean)assignment.getAssignment().accept(this))
			return false;
		
		return true;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		callStatement.getCall().setSymbolsTable(callStatement.getSymbolsTable());
		if (!(Boolean)callStatement.getCall().accept(this))
			return false;
		
		return true;
	}

	@Override
	public Object visit(Return returnStatement) {
		if (returnStatement.hasValue()) {
			returnStatement.getValue().setSymbolsTable(returnStatement.getSymbolsTable());
			if (!(Boolean)returnStatement.getValue().accept(this))
				return false;
		}
		
		return true;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getCondition().setSymbolsTable(ifStatement.getSymbolsTable());
		if (!(Boolean)ifStatement.getCondition().accept(this))
			return false;
		ifStatement.getOperation().setSymbolsTable(ifStatement.getSymbolsTable());
		if (!(Boolean)ifStatement.getOperation().accept(this))
			return false;
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().setSymbolsTable(ifStatement.getSymbolsTable());
			if (!(Boolean)ifStatement.getElseOperation().accept(this))
				return false;
		}
		return true;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getCondition().setSymbolsTable(whileStatement.getSymbolsTable());
		if (!(Boolean)whileStatement.getCondition().accept(this))
			return false;
		whileStatement.getOperation().setSymbolsTable(whileStatement.getSymbolsTable());
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
		this.blockCounter++;
		SymbolTable blockStmntSymbolTable = new SymbolTable("block#" + blockCounter, SymbolTableTypes.STATEMENT_BLOCK);
		statementsBlock.getSymbolsTable().addTableChild(
				blockStmntSymbolTable.getId(), blockStmntSymbolTable);
		blockStmntSymbolTable.setParentSymbolTable(statementsBlock.getSymbolsTable());
		for (Statement stmnt : statementsBlock.getStatements()) {
			stmnt.setSymbolsTable(blockStmntSymbolTable);
			if (!(Boolean)stmnt.accept(this))
				return false;
		}
		
		return true;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		if (!addEntryAndCheckDuplication(localVariable.getSymbolsTable(), 
				new SymbolEntry(localVariable.getName(), typeTable.getTypeFromASTTypeNode(localVariable.getType()), IDSymbolsKinds.VARIABLE))) {
			this.semanticErrorThrower = new SemanticErrorThrower(localVariable.getLine(),
					"variable " + localVariable.getName() + " is initialized more than once");
			return false;
		}
		
		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().setSymbolsTable(localVariable.getSymbolsTable());
			if (!(Boolean)localVariable.getInitValue().accept(this))
				return false;
		}
		
		return true;
	}

	@Override
	public Object visit(VariableLocation location) {
		if (location.isExternal()) {
			location.getLocation().setSymbolsTable(location.getSymbolsTable());
			if (!(Boolean)location.getLocation().accept(this))
				return false;
			if (getVariableSymbolEntry(location.getName(), this.currentClassSymbolTablePoint) == null) {
				this.semanticErrorThrower = new SemanticErrorThrower(location.getLine(),
						"variable " + location.getName() + " is not initialized");
				return false;
			}
			
		}
		else {
			SymbolEntry varEntry = getVariableSymbolEntry(location.getName(),  location.getSymbolsTable());
			if (varEntry == null) {
				this.semanticErrorThrower = new SemanticErrorThrower(location.getLine(),
						"variable " + location.getName() + " is not initialized");
				return false;
			}
			if (varEntry.getType().isClassType()) 
				this.currentClassSymbolTablePoint = this.rootSymbolTable.findChildSymbolTable(varEntry.getType().toString());
			
				
		}
		return true;
	}

	@Override
	public Object visit(ArrayLocation location) {
		location.getArray().setSymbolsTable(location.getSymbolsTable());
		if (!(Boolean)location.getArray().accept(this))
			return false;
		location.getIndex().setSymbolsTable(location.getSymbolsTable());
		if (!(Boolean)location.getIndex().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(StaticCall call) {
		SymbolTable clsSymbolTable = this.rootSymbolTable.findChildSymbolTable(call.getClassName());
		if (clsSymbolTable == null) {
			this.semanticErrorThrower = new SemanticErrorThrower(call.getLine(),
					"the class " + call.getClassName() + " dosen't exist");
			return false;
		}
		if(getMethodSymbolEntry(call.getName(), IDSymbolsKinds.STATIC_METHOD, clsSymbolTable) == null) {
			this.semanticErrorThrower = new SemanticErrorThrower(call.getLine(),
					"the method " + call.getName() + " dosen't exist");
			return false;
		}
		
		for (Expression arg : call.getArguments()) {
			arg.setSymbolsTable(call.getSymbolsTable());
			if (!(Boolean)arg.accept(this))
				return false;
		}
		
		return true;
	}

	@Override
	public Object visit(VirtualCall call) {
		if (call.isExternal()) {
			call.getLocation().setSymbolsTable(call.getSymbolsTable());
			if (!(Boolean)call.getLocation().accept(this))
				return false;
			if(getMethodSymbolEntry(call.getName(), IDSymbolsKinds.VIRTUAL_METHOD, this.currentClassSymbolTablePoint) == null) {
				this.semanticErrorThrower = new SemanticErrorThrower(call.getLine(),
						"the method " + call.getName() + " dosen't exist");
				return false;
			}
		}
		else if(getMethodSymbolEntry(call.getName(), IDSymbolsKinds.VIRTUAL_METHOD, call.getSymbolsTable()) == null) {
			this.semanticErrorThrower = new SemanticErrorThrower(call.getLine(),
					"the method " + call.getName() + " dosen't exist");
			return false;
		}
		
		for (Expression arg : call.getArguments()) {
			arg.setSymbolsTable(call.getSymbolsTable());
			if (!(Boolean)arg.accept(this))
				return false;
		}
		
		return true;
	}

	@Override
	public Object visit(This thisExpression) {
		SymbolTable bottomSymbolTable = thisExpression.getSymbolsTable();
		while (bottomSymbolTable.getId().contains("block#")) 
			bottomSymbolTable = bottomSymbolTable.getParentSymbolTable();
		
		bottomSymbolTable = bottomSymbolTable.getParentSymbolTable();
		this.currentClassSymbolTablePoint = bottomSymbolTable;
				
		return true;
	}

	@Override
	public Object visit(NewClass newClass) {
		if (this.rootSymbolTable.findChildSymbolTable(newClass.getName()) == null) {
			this.semanticErrorThrower = new SemanticErrorThrower(newClass.getLine(),
					"the class " + newClass.getName() + " dosen't exist");
			return false;
		}
		
		return true;
	}

	@Override
	public Object visit(NewArray newArray) {
		newArray.getSize().setSymbolsTable(newArray.getSymbolsTable());
		if (!(Boolean)newArray.getSize().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(Length length) {
		return true;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		binaryOp.getFirstOperand().setSymbolsTable(binaryOp.getSymbolsTable());
		if(!(Boolean)binaryOp.getFirstOperand().accept(this))
			return false;
		binaryOp.getSecondOperand().setSymbolsTable(binaryOp.getSymbolsTable());
		if(!(Boolean)binaryOp.getSecondOperand().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		binaryOp.getFirstOperand().setSymbolsTable(binaryOp.getSymbolsTable());
		if(!(Boolean)binaryOp.getFirstOperand().accept(this))
			return false;
		binaryOp.getSecondOperand().setSymbolsTable(binaryOp.getSymbolsTable());
		if(!(Boolean)binaryOp.getSecondOperand().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		unaryOp.getOperand().setSymbolsTable(unaryOp.getSymbolsTable());
		if(!(Boolean)unaryOp.getOperand().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		unaryOp.getOperand().setSymbolsTable(unaryOp.getSymbolsTable());
		if(!(Boolean)unaryOp.getOperand().accept(this))
			return false;

		return true;
	}

	@Override
	public Object visit(Literal literal) {
		return true;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		expressionBlock.getExpression().setSymbolsTable(expressionBlock.getSymbolsTable());
		return expressionBlock.getExpression().accept(this);
	}
	
	private Object visitMethod(Method method) {
		SymbolTable currentMethodSymbolTable = method.getSymbolsTable().findChildSymbolTable(
				method.getName());
		for (Formal formal : method.getFormals()) {
			formal.setSymbolsTable(currentMethodSymbolTable);
			if (!(Boolean)formal.accept(this))
				return false;
		}
		
		for (Statement stmnt : method.getStatements()) {
			stmnt.setSymbolsTable(currentMethodSymbolTable);
			if(!(Boolean)stmnt.accept(this))
				return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @return true if and only if there is no variable duplication 
	 * and the SymbolEntry was added successfully.
	 */
	private Boolean addEntryAndCheckDuplication(SymbolTable table, SymbolEntry entry) {
		if (table.hasEntry(entry.getId()))
			return false;
		
		table.addEntry(entry.getId(), entry);
		
		return true;
	}
	
	private IDSymbolsKinds getMethodKind(Method method) {
		if (method instanceof VirtualMethod)
			return IDSymbolsKinds.VIRTUAL_METHOD;
		
		return IDSymbolsKinds.STATIC_METHOD;
	}
	
	private SymbolEntry getVariableSymbolEntry(String name, SymbolTable bottomSymbolTable) {
		while (bottomSymbolTable.getId().contains("block#")) {
			if (bottomSymbolTable.hasEntry(name))
				return bottomSymbolTable.getEntry(name);
			bottomSymbolTable = bottomSymbolTable.getParentSymbolTable();
		}
		
		// Checking method table:
		if (bottomSymbolTable.hasEntry(name))
			return bottomSymbolTable.getEntry(name);
		
		String containigMethodName = bottomSymbolTable.getId();
		bottomSymbolTable = bottomSymbolTable.getParentSymbolTable();
		
		if (bottomSymbolTable.getEntry(containigMethodName).getKind() == 
				IDSymbolsKinds.STATIC_METHOD)
			return null;
		
		// Checking class tables:
		while (!bottomSymbolTable.getId().equals("globals#")) {
			SymbolTable clsTable = bottomSymbolTable;
			if (clsTable.hasEntry(name))
				if (clsTable.getEntry(name).getKind() == IDSymbolsKinds.FIELD)
					return clsTable.getEntry(name);
			bottomSymbolTable = bottomSymbolTable.getParentSymbolTable();
		}

		return null;
	}
	
	private SymbolEntry getMethodSymbolEntry(
			String name, IDSymbolsKinds methodKind, SymbolTable bottomClassSymbolTable) {
		while (bottomClassSymbolTable != null) {
			if (bottomClassSymbolTable.hasEntry(name))
				if (bottomClassSymbolTable.getEntry(name).getKind() == methodKind)
					return bottomClassSymbolTable.getEntry(name);
			bottomClassSymbolTable = bottomClassSymbolTable.getParentSymbolTable();
		}
		return null;
	}
}
