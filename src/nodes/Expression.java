package nodes;

// This interface represents an evaluated expression
// An evaluated expression is either a factor (single value) or
// a complex expression (e.g. 50%-2px, which cannot be evaluated
// until run in the browser to get the actual value for '50%')
public interface Expression {
    public String toString();
}
