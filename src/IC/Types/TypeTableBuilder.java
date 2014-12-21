package IC.Types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import IC.AST.*;
import IC.SemanticAnalysis.SemanticError;
import IC.SemanticAnalysis.SemanticErrorThrower;
/**
 * builds a type table corresponding to a given program
 */
public class TypeTableBuilder implements Visitor {
	private final String MAIN_METHOD_CORRECT_SIGNATURE = "string[] -> void";
	
	private TypeTable builtTypeTable;
	private SemanticErrorThrower semanticErrorThrower;
	private String programFilePath;
	/**
	 * main constructor
	 * @param tableId name of the type table to be built
	 */
	public TypeTableBuilder(File file) {
		this.programFilePath = file.getPath();
		this.builtTypeTable = new TypeTable(file.getName());
		builtTypeTable.addPrimitiveTypes();
	}
	
	/**
	 * @return currently held type table
	 */
	public TypeTable getBuiltTypeTable() {
		return this.builtTypeTable;
	}
	
	/**
	// Builds the program's Type Table and also checks the following semantic issues:
	// 1) There is only one Main method with the correct signature (using findAndCheckMainMethod)
	// 2) Classes only extends classes which were declared before them.
	//	  (this also prevents any inheritance cycle).
	 * 
	 * @param program AST node root of the program used to construct the type table
	 * @throws SemanticError
	 */
	public void buildTypeTable(Program program) throws SemanticError {
		if (!findAndCheckMainMethod(program))
			semanticErrorThrower.execute();
		if (!(Boolean)visit(program))
			semanticErrorThrower.execute();
	}
	
	
	private Boolean findAndCheckMainMethod(Program program) {
		int mainMethodCounter = 0;
		Method lastMainMethod = null;
		for (ICClass icClass : program.getClasses()) {
			for (Method method : icClass.getMethods()) {
				if (method.getName().equals("main")) {
					lastMainMethod = method;
					mainMethodCounter++;
				}
			}
		}
		// Main method count checking:
		if (mainMethodCounter == 0) {
			try {
				LineNumberReader lnr = new LineNumberReader(new FileReader(new File(this.programFilePath)));
				lnr.skip(Long.MAX_VALUE);
				semanticErrorThrower = new SemanticErrorThrower(lnr.getLineNumber() + 1, "Main Method is missing");
				lnr.close();
				return false;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (mainMethodCounter > 1) {
			semanticErrorThrower = new SemanticErrorThrower(lastMainMethod.getLine(), "Main Method is declared more than once");
			return false;
		}
		
		// Signature checking and type registering of the main method:
		if (!(lastMainMethod instanceof StaticMethod)) {
			semanticErrorThrower = new SemanticErrorThrower(lastMainMethod.getLine(), "Main Method must be static");
			return false;
		}
			
		for (Formal formal : lastMainMethod.getFormals())
			formal.accept(this);
		lastMainMethod.getType().accept(this);
		
		builtTypeTable.addMethodType(lastMainMethod);
		MethodType methodType = builtTypeTable.getMethodType(lastMainMethod);
		if (!methodType.toString().equals(MAIN_METHOD_CORRECT_SIGNATURE)) {
			semanticErrorThrower = new SemanticErrorThrower(lastMainMethod.getLine(), "Main Method has a wrong signature");
			return false;
		}
		
		return true;
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
		// Checks if the class extends a class which was not 
		// declared before (including class extending itself situation).
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
		ifStatement.getOperation().accept(this);
		if (ifStatement.hasElse())
			ifStatement.getElseOperation().accept(this);
		
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getOperation().accept(this);
		
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

	/**
	 * visit for general method
	 * @param method method to visit
	 * @return null
	 */
	private Object visitMethod(Method method) {
		for (Formal formal : method.getFormals())
			formal.accept(this);
		method.getType().accept(this);
		// method type registration
		builtTypeTable.addMethodType(method);
		for (Statement stmnt : method.getStatements())
			stmnt.accept(this);
		
		return null;
	}
	
	/**
	 * visit for general IC.AST.Type type
	 * @param type
	 * @return null
	 */
	private Object visitType(IC.AST.Type type) {
		// array type registration.
		if (isArrayType(type))
			builtTypeTable.addArrayType(type);
		
		return null;
	}
	
	/**
	 * 
	 * @param typeNode
	 * @return true iff the type is an array (dimension > 0)
	 */
	private Boolean isArrayType(IC.AST.Type typeNode) {
		return (typeNode.getDimension() > 0);
	}
}
