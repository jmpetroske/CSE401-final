(function() {

window.addEventListener("resize", recompute);
window.onload = recompute;

//var sorted = false;
function recompute() {
/*
	if (!sorted) {
		// Sort the operations by a heuristic of how specific the selector is
		// This should make styling for elements higher in the DOM hierarchy be calculated before elements lower in the DOM hierarchy
		calcOperations.sort(function(a, b) {

		});
	}
*/
	var elements = new Array();
    for (var i = 0; i < calcOperations.length; i++) {
        $(calcOperations[i].selector).each(function(j, element) {
			var jqueryelement = $(element);
			elements.push(
				{	
					element: jqueryelement,
					depth: jqueryelement.parents().length,
					calcOperation: calcOperations[i]
				}
			);
        });
    }
	// Make sure we update elements high in the DOM hierarchy first
	// Also apply selectors that are less specific first, so that more specific selectors will take precedence
	elements.sort(function(a, b) {
		var result = a.depth - b.depth;
		if (result == 0) {
			result = a.calcOperation["selector"] - b.calcOperation["selector"].length; 
		}
		return result;
	});
	for (var i = 0; i < elements.length; i++) {
    	var value = calculate(elements[i].calcOperation["expr"], elements[i].element);
    	elements[i].element.css(elements[i].calcOperation["property"], value.toString() + "px");
	}
}

// Returns a value in px
function calculate(expr, element) {
    if (expr.hasOwnProperty("op")) {
        switch(expr["op"]) {
            case "ADD":
                return (calculate(expr["left"], element) + calculate(expr["right"], element));
            case "SUB":
                return (calculate(expr["left"], element) - calculate(expr["right"], element));
            case "MLT":
                return (calculate(expr["left"], element) * calculate(expr["right"], element));
            case "DIV":
                return (calculate(expr["left"], element) / calculate(expr["right"], element));
            default:
                console.log("unsupported op: " + expr["op"] + " \n");
                return 0;
        }
    } else {
        var num = expr["num"];
        // https://www.w3.org/TR/css3-values/#absolute-lengths
        switch(expr["unit"]) {
            case "mm":
                num = num / 10; /* FALLTHROUGH: converted to cm */
            case "cm":
                num = num / 2.54; /* FALLTHROUGH: converted to in */
            case "in":
                return 96 * num;
            case "pt":
                return (num * 96 / 72)
            case "px":
                return num;
            case "%":
                return element.parent().width() * num / 100;
            case "em":
                return num * parseFloat(element.css('font-size'));
            case "rem":
                return num * parseFloat($(":root").css('font-size'));
            default:
                console.log("unsupported unit: " + expr["num"] + " \n");
                return 0;
                break;
        }
    }
}

}());
