import nodes.Expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonas on 12/9/16.
 */
public class Scope {
    public String selector = null;
    public String propertyName = null;
    private Map<String, Expression> vars = new HashMap<>();

    public Scope() {}

    public Scope(Scope scope) {
        if (scope != null) {
            selector = scope.selector;
            propertyName = scope.propertyName;
            vars = new HashMap<>(scope.vars);
        }
    }

    public Scope(Scope scope, String selector) {
        this(scope);
        this.selector = selector;
    }

    // Returns variable from Scope, or null if it is not visible (undeclared)
    public Expression getVar(String var) {
        return vars.getOrDefault(var, null);
    }

    // Sets variable in Scope, overwrites any variable with the same name
    public void setVar(String var, Expression e) {
        vars.put(var, e);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("{");
        String sep = "";
        for (Map.Entry<String, Expression> entry : vars.entrySet()) {
            out.append(sep);
            out.append(entry.getKey() + "=" + entry.getValue());
            sep = ", ";
        }
        out.append("}");
        return out.toString();
    }
}
