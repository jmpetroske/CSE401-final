import nodes.ComplexExpression;
import nodes.Expression;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

public class Visitor extends ParserBaseVisitor<Void> {
    private StringBuilder out;
    private StringBuilder jsout;
    private List<CompileError> errors;
    private int pos = 0;

    private Map<String, Mixin> mixins = new HashMap<>();

    // the "call stack", since we don't have conditionals we can't allow
    // recursive calls, so basically, for each call we check if it has been
    // callend, if it has throw an error, otherwise mark the function as visited
    // and call it, and then on return we mark it has unvisited again
    private Map<String, Boolean> callStack = new HashMap<>();

    private Stack<Scope> scopes = new Stack<Scope>();

    // array of JSON representations of all the complex expressions we find
    // during parsing, these are then outputted to the js file
    private List<String> complexExpressions = new ArrayList<>();

    public Visitor(StringBuilder out, StringBuilder jsout, List<CompileError> errors) {
        this.out = out;
        this.jsout = jsout;
        this.errors = errors;

        // push empty scope
        scopes.push(new Scope());
    }

    private class Mixin {
        private List<String> args = new ArrayList<String>();
        private ParserParser.BlockContext block;

        public Mixin(ParserParser.BlockContext block) {
            this.block = block;
        }
    }

    @Override
    public Void visitStylesheet(ParserParser.StylesheetContext ctx) {
        // Visit all mixin declarations
        ctx.mixinDeclaration().stream()
                .forEach(mixinctx -> {
                    String mixinName = mixinctx.name.getText();
                    if (mixins.containsKey(mixinName)) {
                        /*
                        throw new Exception(mixinName + " already declared");
                        */
                        errors.add(new CompileError(ctx, mixinName + " already declared, overwriting..."));
                    }
                    Mixin m = new Mixin(mixinctx.block());
                    if (mixinctx.args != null) {
                        mixinctx.args.param().forEach(param -> {
                            m.args.add(param.variable().getText());
                        });
                    }
                    mixins.put(mixinName, m);
                });

        // Visit everything else
        for (ParseTree child : ctx.children) {
            if (child instanceof ParserParser.MixinDeclarationContext) {
                continue;
            }

            visit(child);
        }

        // output the complex expressions to the js string builder
        if (complexExpressions.size() > 0) {
            jsout.append("var calcOperations = [");
            jsout.append(String.join(", ", complexExpressions));
            jsout.append("]");
        }
        return null;
    }

    @Override
    public Void visitVariableDeclaration(ParserParser.VariableDeclarationContext ctx) {
        Expression value = (new ExpressionVisitor(currentScope(), errors).visit(ctx.expression()));
        currentScope().setVar(ctx.variable().getText(), value);
        return null;
    }

    @Override
    public Void visitRuleset(ParserParser.RulesetContext ctx) {
        String selector = getOriginal(ctx.selectors());

        scopes.push(new Scope(currentScope(), selector));

        out.append(selector);
        out.append(" {\n");
        visitChildren(ctx);
        out.append("}\n");

        scopes.pop();
        return null;
    }

    @Override
    public Void visitProperty(ParserParser.PropertyContext ctx) {
        String propertyName = ctx.name.getText();
        currentScope().propertyName = propertyName;

        out.append("\t");
        out.append(propertyName + ": ");
        visitChildren(ctx);
        out.append(";\n");
        return null;
    }

    @Override
    public Void visitValues(ParserParser.ValuesContext ctx) {
        // Values are just a bunch of expressions

        // TODO: If there are more than one expression and any of these
        // expressions evaluates to a complex expression, throw an unsupported error
        String sep = "";
        for (ParserParser.ExpressionContext exprCtx : ctx.expression()) {
            out.append(sep);
            ExpressionVisitor visitor = new ExpressionVisitor(currentScope(), errors);
            Expression evalExpr = visitor.visit(exprCtx);
            if (evalExpr != null) {
                out.append(evalExpr.toString());

                if (evalExpr instanceof ComplexExpression) {
                    String propertyMapping = PropertyMappings.lookup(currentScope().propertyName);
                    if (propertyMapping == null) {
                        errors.add(new CompileError(exprCtx, " Complex expressions are not allowed for property " + currentScope().propertyName));
                        continue;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("{ \"selector\": \"" + currentScope().selector + "\", ");
                    builder.append("\"parentProperty\": \"" + propertyMapping + "\", \"property\": \"");
                    builder.append(currentScope().propertyName + "\", \"expr\": ");
                    builder.append(((ComplexExpression) evalExpr).toJsonString());
                    builder.append("}");
                    complexExpressions.add(builder.toString());
                }
            } else {
                errors.add(new CompileError(exprCtx, " Failed to evaluate expression: " + getOriginal(ctx)));
                out.append("/* undefined: " + getOriginal(exprCtx) + " */");
            }

            sep = " ";
        }
        return null;
    }

    @Override
    public Void visitUseDeclaration(ParserParser.UseDeclarationContext ctx) {
        String name = ctx.name.getText();
        Mixin mixin = mixins.getOrDefault(name, null);
        if (mixin == null) {
            errors.add(new CompileError(ctx, " Cannot to use undeclared mixin " + name));
            return null;
        }

        int num = ctx.args != null ? ctx.args.expression().size() : 0;
        if (num != mixin.args.size()) {
            errors.add(new CompileError(ctx, " Wrong number of arguments when calling mixin " + name));
            return null;
        }

        if (callStack.getOrDefault(name, false)) {
            errors.add(new CompileError(ctx," Recursive call to " + name + " in mixin"));
            return null;
        }

        ExpressionVisitor visitor = new ExpressionVisitor(currentScope(), errors); // We want to evaluate using old scope
        scopes.push(new Scope(currentScope()));

        // Add arguments to scope
        if (ctx.args != null) {
            for (int i = 0; i < mixin.args.size(); i++) {
                currentScope().setVar(mixin.args.get(i), visitor.visit(ctx.args.expression().get(i)));
            }
        }

        callStack.put(name, true);

        // add arguments
        visit(mixin.block);

        callStack.put(name, false);
        scopes.pop();

        return null;
    }

    // "Helper" to get current scope
    private Scope currentScope() {
        return scopes.peek();
    }

    // Helper functions to grab stuff from the original source code.
    //   These are used for example when we add boilerplate code or
    //   to grab selectors and property names.
    public static String getOriginal(ParserRuleContext ctx) {
        CharStream input = ctx.start.getInputStream();
        Interval interval = new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        return input.getText(interval);
    }

    public static String getOriginal(ParserRuleContext ctx, int a, int b) {
        CharStream input = ctx.start.getInputStream();
        Interval interval = new Interval(a, b);
        return input.getText(interval);
    }

}
