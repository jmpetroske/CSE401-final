import java.util.HashMap;
import java.util.Map;

/**
 * PropertyMappings contain all CSS properties where it is valid
 * to use complex expressions.
 *
 * The reason for this is simple, if a complex expression contains
 * 50% (for example), this mapping will tell which attribute in the
 * parent element to look up the actual value.
 *
 * Example:
 *  .some-class { min-height: 50% - 12px; }
 *
 *  To calculate the actual value of 50%-12px we need to know what
 *  value we should take 50% of (which is the height of the parent
 *  element).
 */
public class PropertyMappings {
    private static final Map<String, String> mappings = new HashMap<>();
    static {
        mappings.put("height",        "height");
        mappings.put("min-height",    "height");
        mappings.put("max-height",    "height");
        mappings.put("width",         "width");
        mappings.put("min-width",     "width");
        mappings.put("max-width",     "width");
    }

    public static final String lookup(String property) {
        return mappings.getOrDefault(property.toLowerCase(), null);
    }
}
