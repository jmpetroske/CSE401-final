import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerTest {
    Compiler compiler;
    StringBuilder css;
    StringBuilder js;
    List<CompileError> errs;

    public void source(String in) {
        // Reset everything
        css = new StringBuilder();
        js = new StringBuilder();
        try {
            compiler = new Compiler(css, js, new ANTLRFileStream(in));
        }
        catch (IOException e) {
        }
    }

    public String get(String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        }
        catch (IOException e) {}
        return "";
    }

    @Test
    public void testVariables() {
        source("test/test_variables.in");

        assertEquals((compiler.run()).size(), 0);
        assertEquals(js.toString(), "");
        assertEquals(css.toString().replaceAll("\t", ""), get("test/test_variables.expected"));
    }

    @Test
    public void testMixins() {
        source("test/test_mixins.in");

        assertEquals((compiler.run()).size(), 0);
        assertEquals(js.toString(), "");
        assertEquals(css.toString().replaceAll("\t", ""), get("test/test_mixins.expected"));
    }

    @Test
    public void testExpressions() {
        source("test/test_math.in");

        assertEquals((compiler.run()).size(), 0);
        assertEquals(js.toString(), "");
        assertEquals(css.toString().replaceAll("\t", ""), get("test/test_math.expected"));
    }
}