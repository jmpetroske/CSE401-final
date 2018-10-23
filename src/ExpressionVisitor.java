import nodes.Expression;
import nodes.Factor;

import java.util.List;

/**
 * Created by jonas on 12/8/16.
 */
public class ExpressionVisitor extends ParserBaseVisitor<Expression> {
    private Scope scope;
    private List<CompileError> errors;

    public ExpressionVisitor(Scope scope, List<CompileError> errors) {
        this.scope = scope;
        this.errors = errors;
    }

    @Override
    public Expression visitExprMulDiv(ParserParser.ExprMulDivContext ctx) {
        Expression e1 = visit(ctx.left);
        Expression e2 = visit(ctx.right);

        if (e1 instanceof Factor && e2 instanceof Factor) {

            if (((Factor)e1).type != Factor.Type.Number || ((Factor)e2).type != Factor.Type.Number) {
                errors.add(new CompileError(ctx, " Cannot do arithmetic operations on a non-numeric type!"));
            }

            if (ctx.op.getText().equals("*")) return ((Factor) e1).mul((Factor) e2);
            if (ctx.op.getText().equals("/")) return ((Factor) e1).div((Factor) e2);
        }

        return null;
    }

    @Override
    public Expression visitExprAddSub(ParserParser.ExprAddSubContext ctx) {
        Expression e1 = visit(ctx.left);
        Expression e2 = visit(ctx.right);

        if (e1 instanceof Factor && e2 instanceof Factor) {

            if (((Factor)e1).type != Factor.Type.Number || ((Factor)e2).type != Factor.Type.Number) {
                errors.add(new CompileError(ctx, " Cannot do arithmetic operations on a non-numeric type!"));
            }

            if (ctx.op.getText().equals("+")) return ((Factor) e1).add((Factor) e2);
            if (ctx.op.getText().equals("-")) return ((Factor) e1).sub((Factor) e2);
        }

        return null;
    }

    @Override
    public Expression visitExprParens(ParserParser.ExprParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitExprFactor(ParserParser.ExprFactorContext ctx) {
        // Resolve variables
        Factor factor = ctx.factor().value;
        if (factor.type == Factor.Type.Variable) {
            return scope.getVar(factor.value);
        }
        return factor;
    }
}