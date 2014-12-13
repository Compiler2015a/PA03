package IC.Types;

import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;

public class TypeTableBuilder implements Visitor {

	private TypeTable builtTypeTable;
	
	public TypeTableBuilder() {
		this.builtTypeTable = new TypeTable();
		builtTypeTable.addPrimitiveTypes();
	}
	
	public TypeTable getBuiltTypeTable() {
		return this.builtTypeTable;
	}
	
	public void findMainMethod(Program program) {
		for (ICClass icClass : program.getClasses()) {
			for (Method method : icClass.getMethods()) {
				if (method.getName().equals("main")) {
					method.accept(this);
					return;
				}
			}
		}
	}
	
	@Override
	public Object visit(Program program) {
		for (ICClass icClass : program.getClasses())
			icClass.accept(this);
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		builtTypeTable.addClassType(icClass);
		for (Field field : icClass.getFields())
			field.accept(this);
		for (Method method : icClass.getMethods())
			method.accept(this);
		
		return null;
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
