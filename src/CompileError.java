import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/**
 * Created by jpetro on 12/12/16.
 */
public class CompileError {
    private int lineAt;
    private int charAt;
    private String descripion;

    public CompileError(ParserRuleContext ctx, String description) {
        Token start = ctx.getStart();
        this.lineAt = start.getLine();
        this.charAt = start.getCharPositionInLine();
        this.descripion = description;
    }

    @Override
    public String toString() {
        return String.format("Error at [%d:%d]: %s", lineAt, charAt, descripion);
    }
}
