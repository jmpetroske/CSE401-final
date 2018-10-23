package nodes;

/**
 * ComplexExpression represents an expression that includes multiple
 * different units, these kind of expressions should be evaluated during
 * run time with JS.
 */
public class ComplexExpression implements Expression {
    String op;
    Expression e1;
    Expression e2;

    public ComplexExpression(String op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * getFallbackFactor returns the factor that should be used as a
     * fallback value in the CSS
     */
    public Factor getFallbackFactor() {
        Factor f1 = null;
        Factor f2 = null;

        if (e1 instanceof Factor) f1 = (Factor) e1;
        else f1 = ((ComplexExpression)e1).getFallbackFactor();

        if (e2 instanceof Factor) f2 = (Factor) e2;
        else f2 = ((ComplexExpression)e2).getFallbackFactor();

        // Current heuristic: Choose the left value
        return f1;
    }

    public String toString() {
        Factor fallback = getFallbackFactor();
        return fallback == null ? "undefined" : fallback.toString();
    }

    public String toJsonString() {
        String left = "";
        String right = "";
        if (e1 instanceof Factor) left = "{ \"num\": " + ((Factor) e1).value + ", \"unit\": \"" + ((Factor) e1).unit + "\" }";
        else left = ((ComplexExpression) e1).toJsonString();

        if (e2 instanceof Factor) right = "{ \"num\": " + ((Factor) e2).value + ", \"unit\": \"" + ((Factor) e2).unit + "\" }";
        else right = ((ComplexExpression) e2).toJsonString();

        return "{ \"op\": \"" + this.op + "\", \"left\": " + left + ", \"right\": " + right + " }";
    }
}
