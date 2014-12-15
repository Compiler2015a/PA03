package IC.AST;

import IC.DataTypes;

/**
 * Primitive data type AST node.
 * 
 * @author Tovi Almozlino
 */
public class PrimitiveType extends Type {

	private DataTypes type;

	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	/**
	 * Constructs a new primitive data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param type
	 *            Specific primitive data type.
	 */
	public PrimitiveType(int line, DataTypes type) {
		super(line);
		this.type = type;
	}

	public String getName() {
		return type.getDescription();
	}
	
	@Override
	public boolean nullAssignable() {
		return type == DataTypes.STRING || getDimension() > 0;
	}
	
	@Override
	public boolean nullComparable() {
		return type == DataTypes.STRING || type == DataTypes.VOID || getDimension() > 0;
	}
	
	@Override
	public Type clone() {
		Type other = new PrimitiveType(getLine(), type);
		other.setDimension(getDimension());
		return other;
	}

}