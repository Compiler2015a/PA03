package IC.SymbolsTable;

import java.util.LinkedList;
import java.util.Queue;

import IC.AST.*;

public class SymbolsTableBuilder implements Visitor {
	
	private Queue<ASTNode> nodeHandlingQueue;
	private SymbolTable rootSymbolTable;
	private SymbolTable currentClassSymbolTablePoint;
	
	int blockCounter;
	
	public SymbolsTableBuilder() {
		this.nodeHandlingQueue = new LinkedList<ASTNode>();
		this.rootSymbolTable = new SymbolTable("globals#");
		this.currentClassSymbolTablePoint = null;
	}

	public SymbolTable buildSymbolTables(Program root) {
		nodeHandlingQueue.add(root);
		ASTNode currentNode;
		this.blockCounter = 0;
		while (!nodeHandlingQueue.isEmpty()) {
			currentNode = nodeHandlingQueue.poll();
			if (!(Boolean)currentNode.accept(this)) {
				System.out.println("error"); // TODO for checks. should be removed
				return null;
			}
		}
		
		return null;
	}
	
	@Override
	public Object visit(Program program) {
		SymbolTable programSymbolTable = this.rootSymbolTable;
		for (ICClass iccls : program.getClasses()) {
			nodeHandlingQueue.add(iccls);
			if (!addEntryAndCheckDuplication(programSymbolTable, 
					new SymbolEntry(iccls.getName(), null, IDSymbolsKinds.CLASS))) {
				// TODO Add error handling of duplicated variable
			}
			SymbolTable icclsParentSymbolTable;
			if (iccls.hasSuperClass()) {
				icclsParentSymbolTable = findSymbolTable(programSymbolTable, iccls.getSuperClassName());
				if (icclsParentSymbolTable == null) { //class name was nor found or not yet initialized
					// TODO Add error handling of extending a non existing class
				}
			}
			else
				icclsParentSymbolTable = programSymbolTable;
			iccls.setSymbolsTable(icclsParentSymbolTable);
			
			SymbolTable currentClassSymbolTable = new SymbolTable(iccls.getName());
			currentClassSymbolTable.parentSymbolTable = icclsParentSymbolTable;
			icclsParentSymbolTable.children.put(iccls.getName(), currentClassSymbolTable);
		}
		return true;
	}

	@Override
	public Object visit(ICClass icClass) {
		SymbolTable currentClassSymbolTable = findSymbolTable(this.rootSymbolTable, icClass.getName());
		for (Field field : icClass.getFields()) {
			nodeHandlingQueue.add(field);
			if (!addEntryAndCheckDuplication(currentClassSymbolTable, 
					new SymbolEntry(field.getName(), null, IDSymbolsKinds.FIELD))) {
				// TODO Add error handling of duplicated variable
			}
			field.setSymbolsTable(currentClassSymbolTable);
		}
		
		for (Method method : icClass.getMethods()) {
			nodeHandlingQueue.add(method);
			if (!addEntryAndCheckDuplication(currentClassSymbolTable, 
					new SymbolEntry(method.getName(), null, getMethodKind(method)))) {
				// TODO Add error handling of duplicated variable
			}
			method.setSymbolsTable(currentClassSymbolTable);
			SymbolTable currentMethodSymbolTable = new SymbolTable(method.getName());
			currentMethodSymbolTable.parentSymbolTable = currentClassSymbolTable;
			currentClassSymbolTable.children.put(method.getName(), currentMethodSymbolTable);
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
		SymbolTable blockStmntSymbolTable = new SymbolTable("block#" + blockCounter);
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
				new SymbolEntry(localVariable.getName(), null, IDSymbolsKinds.VARIABLE))) {
			// TODO Add error handling of duplicated variable
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
			if (!checkVariableInstance(location.getName(), this.currentClassSymbolTablePoint)) {
				// TODO Add error handling for uninitialized variable
			}
			
		}
		else {
			if (!checkVariableInstance(location.getName(),  location.getSymbolsTable())) {
				// TODO Add error handling for uninitialized variable
			}
				
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
		SymbolTable clsSymbolTable = findSymbolTable(this.rootSymbolTable, call.getClassName());
		if (clsSymbolTable == null) {
			// TODO Add error handling for non-existing class
		}
		if(!checkMethodInstance(call.getName(), IDSymbolsKinds.STATIC_METHOD, clsSymbolTable)) {
			// TODO Add error handling for uninitialized variable
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
			call.getLocation().accept(this);
			if(!checkMethodInstance(call.getName(), IDSymbolsKinds.VIRTUAL_METHOD, this.currentClassSymbolTablePoint)) {
				// TODO Add error handling for uninitialized variable
			}
		}

		if(!checkMethodInstance(call.getName(), IDSymbolsKinds.STATIC_METHOD, call.getSymbolsTable())) {
			// TODO Add error handling for uninitialized variable
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
			bottomSymbolTable = bottomSymbolTable.parentSymbolTable;
		
		bottomSymbolTable = bottomSymbolTable.parentSymbolTable;
		this.currentClassSymbolTablePoint = bottomSymbolTable;
				
		return true;
	}

	@Override
	public Object visit(NewClass newClass) {
		if (findSymbolTable(this.rootSymbolTable, newClass.getName()) == null) {
			// TODO Add error handling for non-existing class
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
		SymbolTable currentMethodSymbolTable = findSymbolTable(
				method.getSymbolsTable(), method.getName());
		for (Formal formal : method.getFormals()) {
			nodeHandlingQueue.add(formal);
			if (!addEntryAndCheckDuplication(currentMethodSymbolTable, 
					new SymbolEntry(formal.getName(), null, IDSymbolsKinds.FORMAL))) {
				// TODO Add error handling of duplicated variable
			}
			formal.setSymbolsTable(currentMethodSymbolTable);
		}
		
		for (Statement stmnt : method.getStatements()) {
			stmnt.setSymbolsTable(currentMethodSymbolTable);
			if(!(Boolean)stmnt.accept(this))
				return false;
		}
		
		return true;
	}
	
	private SymbolTable findSymbolTable(SymbolTable root, String id) {
		for (String tableID : root.children.keySet()) {
			if (id.equals(tableID))
				return root.children.get(id);
			else {
				SymbolTable result = findSymbolTable(root.children.get(tableID), id);
				if (result != null)
					return result;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return true if and only if there is no variable duplication 
	 * and the SymbolEntry was added successfully.
	 */
	private Boolean addEntryAndCheckDuplication(SymbolTable table, SymbolEntry entry) {
		if (table.entries.containsKey(entry.getId()))
			return false;
		
		table.entries.put(entry.getId(), entry);
		
		return true;
	}
	
	private IDSymbolsKinds getMethodKind(Method method) {
		if (method instanceof StaticMethod)
			return IDSymbolsKinds.STATIC_METHOD;
		
		return IDSymbolsKinds.VIRTUAL_METHOD;
	}
	
	private Boolean checkVariableInstance(String name, SymbolTable bottomSymbolTable) {
		while (bottomSymbolTable.getId().contains("block#")) {
			if (bottomSymbolTable.entries.containsKey(name))
				return true;
			bottomSymbolTable = bottomSymbolTable.parentSymbolTable;
		}
		
		// Checking method table:
		if (bottomSymbolTable.entries.containsKey(name))
			return true;
		
		String containigMethodName = bottomSymbolTable.getId();
		bottomSymbolTable = bottomSymbolTable.parentSymbolTable;
		
		if (bottomSymbolTable.entries.get(containigMethodName).getKind() == 
				IDSymbolsKinds.STATIC_METHOD)
			return false;
		
		// Checking class tables:
		while (!bottomSymbolTable.getId().equals("globals#")) {
			SymbolTable clsTable = bottomSymbolTable;
			if (clsTable.entries.containsKey(name))
				if (clsTable.entries.get(name).getKind() == IDSymbolsKinds.FIELD)
					return true;
			bottomSymbolTable = bottomSymbolTable.parentSymbolTable;
		}

		return false;
	}
	
	private Boolean checkMethodInstance(
			String name, IDSymbolsKinds methodKind, SymbolTable bottomClassSymbolTable) {
		while (bottomClassSymbolTable != null) {
			if (bottomClassSymbolTable.entries.containsKey(name))
				if (bottomClassSymbolTable.entries.get(name).getKind() == methodKind)
					return true;
			bottomClassSymbolTable = bottomClassSymbolTable.parentSymbolTable;
		}
		return false;
	}

}
