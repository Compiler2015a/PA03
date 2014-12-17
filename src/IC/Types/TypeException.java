package IC.Types;

public class TypeException extends RuntimeException{
	private int line;
	public TypeException(String message, int line)
	{
		super("semantic error at line " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
