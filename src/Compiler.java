import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.PrintWriter;
import java.util.*;

public class Compiler {

    private CharStream stream;
    private StringBuilder out;
    private StringBuilder js;

    public Compiler(StringBuilder out, StringBuilder js, CharStream stream) {
        this.stream = stream;
        this.out = out;
        this.js = js;
    }

    public List<CompileError> run() {
        List<CompileError> errors = new ArrayList<>();
        ParserLexer lexer = new ParserLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ParserParser parser = new ParserParser(tokens);

        System.out.println("Compiling...");
        (new Visitor(out, js, errors)).visit(parser.stylesheet());
        return errors;

    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: [input filename] [CSS Output filename] [JS output filename]");

            // default values for debugging
            args = new String[] {"data/test2.css", "output.css", "output.js"};
        }
        try {
            CharStream stream = new ANTLRFileStream(args[0]);
            StringBuilder out = new StringBuilder();
            StringBuilder js = new StringBuilder();

            Compiler compiler = new Compiler(out, js, stream);
            List<CompileError> errors = compiler.run();

            if (errors.size() > 0) {
                for (CompileError error : errors) {
                    System.out.println(error.toString());
                }
            } else {
                System.out.println("\nCSS:\n---------------------------------------");
                System.out.println(out.toString());
                System.out.println("\nJS:\n---------------------------------------");
                System.out.println(js.toString());

                try {
                    PrintWriter cssout = new PrintWriter(args[1], "UTF-8");
                    cssout.print(out.toString());
                    cssout.close();
                } catch (Exception e) {
                    System.out.println("Couldn't write CSS to file " + args[1]);
                    System.out.println(e.getMessage());
                }

                try {
                    PrintWriter jsout = new PrintWriter(args[2], "UTF-8");
                    jsout.print(js.toString());
                    jsout.close();
                } catch (Exception e) {
                    System.out.println("Couldn't write JS to file " + args[2]);
                    System.out.println(e.getMessage());
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
