package IC.Types;

public class TypeException extends RuntimeException{
	private int line;
	public TypeException(String message, int line)
	{
		super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
