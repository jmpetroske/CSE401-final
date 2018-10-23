package nodes;

import java.text.DecimalFormat;
import java.util.regex.*;

/**
 * Created by jonas on 12/7/16.
 */
public class Factor extends Value implements Expression {
    public enum Type {
        String, Number, Complex, Variable, Ident, Color
    }

    public Type type;
    public String value;
    public String unit;
    public double num;

    public Factor(Type type, String value) {
        this(type, value, "");
    }

    public Factor(Type type, String value, String unit) {
        this.type = type;
        this.value = value;
        this.unit = unit == null ? "" : unit;
        if (type == Type.Color) { // Assert that value is a valid hex color
            String pattern = "#([0-9a-f]{3}){1,2}";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(value);
            if (!m.find()) { // value doesn't represent a valid color
                System.out.println(value + " does not represent a valid CSS color");
            }
        }
    }

    public String toString() {
        switch (type) {
            case Number:
                Double val = Double.parseDouble(value);
                return (new DecimalFormat("0.#")).format(val) + unit;
        }

        return value;
    }

    public Double getNumber() {
        return Double.parseDouble(this.value);
    }

    private static String getValidUnit(Factor a, Factor b) {
        if (a.type != Type.Number || b.type != Type.Number) {
            // System.out.println(" ERROR: Trying to do arithmetic operations on a non-numeric type!");
            return null;
        }

        if (a.type != b.type) return null;

        if (a.unit.equals(b.unit)) return a.unit;
        if (a.type == Type.Number && !a.unit.isEmpty() && b.unit.isEmpty()) return a.unit;
        if (a.type == Type.Number && a.unit.isEmpty() && !b.unit.isEmpty()) return b.unit;

        return null;
    }

    public Expression add(Factor b) {
        String unit = getValidUnit(this, b);
        if (unit != null) {
            Double newValue = getNumber() + b.getNumber();
            return new Factor(this.type, newValue.toString(), unit);
        }

        // TODO: This if statement might be redundant if getValidUnit throws
        if (this.type == Type.Number) {
            return new ComplexExpression("ADD", this, b);
        }

        return null;
    }

    public Expression sub(Factor b) {
        String unit = getValidUnit(this, b);
        if (unit != null) {
            Double newValue = getNumber() - b.getNumber();
            return new Factor(this.type, newValue.toString(), unit);
        }

        // TODO: This if statement might be redundant if getValidUnit throws
        if (this.type == Type.Number) {
            return new ComplexExpression("SUB", this, b);
        }

        return null;
    }

    public Expression mul(Factor b) {
        String unit = getValidUnit(this, b);
        if (unit != null) {
            Double newValue = getNumber() * b.getNumber();
            return new Factor(this.type, newValue.toString(), unit);
        }

        // TODO: This if statement might be redundant if getValidUnit throws
        if (this.type == Type.Number) {
            return new ComplexExpression("MLT", this, b);
        }

        return null;
    }

    public Expression div(Factor b) {
        String unit = getValidUnit(this, b);
        if (unit != null) {
            Double newValue = getNumber() / b.getNumber();
            return new Factor(this.type, newValue.toString(), unit);
        }

        // TODO: This if statement might be redundant if getValidUnit throws
        if (this.type == Type.Number) {
            return new ComplexExpression("DIV", this, b);
        }

        return null;
    }
}

