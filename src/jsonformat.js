var calcOperations = [
  {
    "selector": "selector",
    "parentProperty": "height", // parent property to look at
    "property": "max-height", // the actual property to be updated
    "expr": {
      "op": "SUB", // SUB, ADD, MLT, DIV
      "left": {
        "op": "ADD",
        "left": {
          "num": 12,
          "unit": "px"
        },
        "right": {
          "num": 10,
          "unit": "%"
        }
      },
      "right": {
        "num": 56,
        "unit": "px"
      }
    }
  }
]
