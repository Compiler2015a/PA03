package IC;

import java.io.FileNotFoundException;
import java.io.FileReader;

import java_cup.runtime.Symbol;
import IC.AST.*;
import IC.Parser.*;
import IC.SemanticAnalysis.SemanticError;
import IC.SemanticAnalysis.SymbolsInstanceAnalyzer;
import IC.SymbolsTable.*;


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
				FileReader libFile = new FileReader(args[1].substring(2));

				LibLexer libScanner = new LibLexer(libFile);
				LibParser libParser = new LibParser(libScanner);

				Symbol libParseSymbol = libParser.parse();
				libRoot = (ICClass) libParseSymbol.value;
				
				
			// Pretty-print the program to System.out
			//	PrettyPrinter printer = new PrettyPrinter(args[1].substring(2));
			//	System.out.println(printer.visit(libRoot));
			}

			//parse IC file
			FileReader icFile = new FileReader(args[0]);

			Lexer scanner = new Lexer(icFile);
			Parser parser = new Parser(scanner);

			Symbol parseSymbol = parser.parse();
			Program ICRoot = (Program) parseSymbol.value;
			if (libRoot != null)
				ICRoot.getClasses().add(libRoot);
			
			System.out.println("Parsed " + args[0] +" successfully!");
			
			//Pretty-print the program to System.out
			PrettyPrinter printer = new PrettyPrinter(args[0]);
			
			SymbolsTableBuilder s = new SymbolsTableBuilder();
			s.buildSymbolTables(ICRoot);
			
			
			
			

		} catch (FileNotFoundException e) {
			System.out.println(e);
		} catch (LexicalError e) {
			System.out.println(e.getMessage());
		} catch (SemanticError e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
