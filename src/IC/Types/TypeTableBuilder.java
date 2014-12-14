package IC.Types;

import IC.AST.*;
import IC.SemanticAnalysis.SemanticError;
import IC.SemanticAnalysis.SemanticErrorThrower;

public class TypeTableBuilder implements Visitor {

	private TypeTable builtTypeTable;
	private SemanticErrorThrower semanticErrorThrower;
	
	public TypeTableBuilder(String tableId) {
		this.builtTypeTable = new TypeTable(tableId);
		builtTypeTable.addPrimitiveTypes();
	}
	
	public TypeTable getBuiltTypeTable() {
		return this.builtTypeTable;
	}
	
	public void buildTypeTable(Program program) throws SemanticError {
		if (!findMainMethod(program))
			semanticErrorThrower.execute();
		if (!(Boolean)visit(program))
			semanticErrorThrower.execute();
	}
	
	private Boolean findMainMethod(Program program) {
		for (ICClass icClass : program.getClasses()) {
			for (Method method : icClass.getMethods()) {
				if (method.getName().equals("main")) {
					for (Formal formal : method.getFormals())
						formal.accept(this);
					method.getType().accept(this);
					
					builtTypeTable.addMethodType(method);
					return true;
				}
			}
		}
		semanticErrorThrower = new SemanticErrorThrower(1, "Main Method is missing");
		// TODO:
		/*
		 * 1) Replace line 1 in the last line (according to the forum)
		 * 2) Check in the specification if we should also throw an error if there are more
		 * 	  than one Main method.
		 */
		return false;
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
		if (!builtTypeTable.addClassType(icClass)) {
			semanticErrorThrower = new SemanticErrorThrower(icClass.getLine(),
					"extended class " + icClass.getSuperClassName() + " was not declared");
			return false;
		}
		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
			method.accept(this);
		
		return true;
	}

	@Override
	public Object visit(Field field) {
		field.getType().accept(this);
		
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
		formal.getType().accept(this);
		
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return visitType(type);
	}

	@Override
	public Object visit(UserType type) {
		return visitType(type);
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
		for (Statement stmnt : statementsBlock.getStatements())
			stmnt.accept(this);
		
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		localVariable.getType().accept(this);
		
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
		newArray.getType().accept(this);
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

	private Object visitMethod(Method method) {
		for (Formal formal : method.getFormals())
			formal.accept(this);
		method.getType().accept(this);
		
		builtTypeTable.addMethodType(method);
		for (Statement stmnt : method.getStatements())
			stmnt.accept(this);
		
		return null;
	}
	
	private Object visitType(IC.AST.Type type) {
		if (isArrayType(type))
			builtTypeTable.addArrayType(type);
		
		return null;
	}
	
	private Boolean isArrayType(IC.AST.Type typeNode) {
		return (typeNode.getDimension() > 0);
	}
}
