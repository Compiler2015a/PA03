package IC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;



import java_cup.runtime.Symbol;
import IC.AST.*;
import IC.Parser.*;
import IC.SemanticAnalysis.SemanticError;
import IC.SymbolsTable.*;
import IC.Types.*;

public class Compiler {

	public static void main(String[] args) {

		if (args.length == 0 || args.length > 2) {
			System.err.println("Error: invalid arguments");
			System.exit(-1);
		}
		
		ICClass libRoot = null;
		try {
			if(args.length == 2 && args[1].substring(0, 2).equals("-L")) { //if library required
				//parse library file
				File libFile = new File(args[1].substring(2));
				FileReader libFileReader = new  FileReader(libFile);
				LibLexer libScanner = new LibLexer(libFileReader);
				LibParser libParser = new LibParser(libScanner);

				Symbol libParseSymbol = libParser.parse();
				libRoot = (ICClass) libParseSymbol.value;
				System.out.println("Parsed " + libFile.getName() +" successfully!");
				
			// Pretty-print the program to System.out
			//	PrettyPrinter printer = new PrettyPrinter(args[1].substring(2));
			//	System.out.println(printer.visit(libRoot));
			}

			//parse IC file
			File icFile = new File(args[0]);
			FileReader icFileReader = new FileReader(icFile);

			Lexer scanner = new Lexer(icFileReader);
			Parser parser = new Parser(scanner);

			Symbol parseSymbol = parser.parse();
			Program ICRoot = (Program) parseSymbol.value;
			if (libRoot != null)
				ICRoot.getClasses().add(0, libRoot);
			
			System.out.println("Parsed " + icFile.getName() +" successfully!");
			System.out.println();

			TypeTableBuilder typeTableBuilder = new TypeTableBuilder(icFile.getName());
			typeTableBuilder.buildTypeTable(ICRoot);
			SymbolsTableBuilder s = new SymbolsTableBuilder(typeTableBuilder.getBuiltTypeTable(), icFile.getName());
			s.buildSymbolTables(ICRoot);
			s.getSymbolTable().printTable();
			typeTableBuilder.getBuiltTypeTable().printTable();
			
			//Pretty-print the program to System.out
			PrettyPrinter printer = new PrettyPrinter(args[0]);
			//System.out.println(printer.visit(ICRoot));
			//validates that all the return values are correct
			
			//validates that all the types are correct
			TypeValidator tv = new TypeValidator();
			ICRoot.accept(tv);
			

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (LexicalError e) {
			System.out.println(e.getMessage());
		} catch (SemanticError e) {
			System.out.println(e.getMessage());
		} catch (TypeException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
