# 401CSS - Final project in CSE 401
Jonas Palm, Joseph Petroske

401CSS is an extension to the CSS language that provides variables, mixins, support for expressions including basic arithmetic operations but also complex expressions that uses multiple different types (units). The compiler creates a CSS file from the source and also (if needed) a JS-file that contains logic to do dynamic resizing from complex expressions (such as height: 50% - 12px).

## The language
### Variables
Variables can be used to store any valid value, for example color (#fff), string, numbers or even expressions. Expressions are evaluated when the variable is declared. The scoping rules are based on Python's use of local variables (variables declared at the root level are considered local for that scope and any sub scopes that is created). This means that any variable in a parent scope can be only be overwritten for the current scope, i.e. the changes will not reflect back to the parent scope.

#### Example
```javascript
$bgcolor = #fff;

.selector {
    color: $bgcolor; /* set text color to $bgcolor (= #fff) (evaluated on declaration)  */
    $bgcolor = #000; /* overwrite $bgcolor for current scope  */
    background-color: $bgcolor; /* = #000;  */
}

.selector2 {
   color: $bgcolor; /* = #fff  */
}

```

Produces the following CSS:
```css
.selector {
	color: #fff;
	background-color: #000;
}
.selector2 {
	color: #fff;
}
```

### Mixins
Mixins can be seen as the functions of our language. They can be used to insert attributes by calling the mixin from a selector. Selectors must be declared at the root level and can contain any number of arguments. As we don't have support for conditionals recursive calls are not allowed (will output compiler error).

#### Example 
```javascript
$yellow = #ff0000;
$color = brown;

/* declare a new mixin called alert with two arguments */
@mixin alert($color, $multiple) {
    background-color: $color;
    font-size: 10px * $multiple;
}

.selector {
    /* call mixin alert with arguments $yellow (=#ff0000) and 2  */
    @use alert($yellow, 2); 
    /*
     * Following attributes are inserted into the selector
     *   background-color: #ff0000;
     *   font-size: 20px;
     */
}
```

Produces the following CSS:
```css
.selector {
	background-color: #ff0000;
	font-size: 20px;
}
```

### Math expressions
The language has support for multiply, division, addition and subtraction. Note that each operator has been surrounded by space to avoid it getting parsed the wrong way. This is because CSS identifiers can contain certain characters in an order that resembles math expressions. For example `-12px-12px` would be parsed as a NUMBER followed by an IDENTIFIER (`px-12px` is the valid CSS identifier in this case). So in order for the expression to be parsed and evaluated correctly it has to be written like so: `-12px - 12px` (will evaluate to `0px`).

Only numbers with the same type (unit) will be evaluated and outputted in to the final CSS. For other expressions such as `50% - 12px` see `Complex expressions`.

#### Example

```javascript
$somevar = 5 * 10px; /* evaluates to 50px on declaration */

.my-header {
    $pad = 5px;

    font-size: 2 * 12px; /* => 22px  */
    padding: (2 * $pad) 0 0 (2 * $pad); /* => 10px 0 0 10px  */
    height: $somevar / 2px; /* variables can be used in math expressions  */
}
```
Produces the following CSS:
```css
.my-header {
	font-size: 24px;
	padding: 10px 0 0 10px;
	height: 25px;
}
```

### Complex expressions
Complex expressions are arithmetic expressions using different types (unit) of values. An example of a complex expression is `50% - 12px`. This is implemented using a Javascript helper file. Whenever a complex expression is detected by the compiler it outputs a JSON object representing the expressions. This expression will later be evaluated using `src/calc.js` (should be included in the HTML page).

As an example, assume we have the following source file:
```javascript
$padding = 12px;
.box {
    padding: $padding;
    width: 50% - 2 * $padding;  /* complex expression! (50% - 24px)  */
}
```
This will output two files, first the CSS file:
```css
.box {
    padding: 12px;
    width: 50%; /* left most value is used from the complex expression  */
}
```

and then a JS file containing a list of complex expressions represented using JSON that will be included along with the `src/calc.js` file.
```javascript
var calcOperations = [
  {
    "selector": ".box",
    "parentProperty": "width",
    "property": "width",
    "expr": {
      "op": "SUB",
      "left": { "num": 50, "unit": "%" },
      "right": { "num": 24.0, "unit": "px" }
    }
  }
];
```

The `parentProperty` is the property to look at when evaluating the expression. For example: `.box { height: 50% - 12px }` would look at the width of the parent of `.box` and insert that into the actual expression. The `property` tells `calc.js` which property of the selector element to update.

These mappings are controlled by `src/PropertyMappings.java` which makes it easy to add more mappings if needed.

### Comments
Multi-line comments are supported by surrounding the comment with `/*` and `*/`.

```javascript
.select {
  height: 2 * 12px; /* this is a comment */
}
```

## Instructions
Compile: `$ make`
Run: `$ make run file=PATH`
To run tests: `$ make tests`

### To run examples
We have three examples contained in `examples/`.

- `example1.html` shows the result of usage of the basic features of the language (variables, mixins, math).
- `example2.html` shows the use of mixins.
- `example3.html` shows a use case when complex expressions are needed to solve the problem.

To rebuild the examples from the source files (not needed as we have included the latest builds of each example):

```bash
# (Must be in the root directory of the project)
$ ./examples/example1.sh
$ ./examples/example2.sh
$ ./examples/example3.sh
```

## Documents
[Video Demo](https://drive.google.com/file/d/0B2D_mUdKP1wmdV9WUU5MUkF2Rzg/view?usp=sharing)

[Presentation Slides](https://docs.google.com/a/uw.edu/presentation/d/1AIa8PhUGbSpNIqXCbBQo8o-o86yruaBa7AdtTiBw-j8/edit?usp=sharing)

[Report](https://docs.google.com/a/uw.edu/document/d/1ujQ28xi3VR3qWzFamPQAL40Lfr1Z0znUMeuPD04A104/edit?usp=sharing)

[Design](https://docs.google.com/a/uw.edu/document/d/1BS9OFvHgqmI3gKYXSxo255iVJJp217bBE-Ud50eSKO4/edit?usp=sharing)

[Proposal](https://docs.google.com/a/uw.edu/document/d/19Et8xDW2c0MQlonI4nIXO3kN0cIXvrdqmpMjNN3nMog/edit?usp=sharing)

[Pre proposal](https://docs.google.com/a/uw.edu/document/d/1SAuLcXPYjrPasJI_sDel2sUDzKMJ2TEP3cGQ1ONJQvI/edit?usp=sharing)