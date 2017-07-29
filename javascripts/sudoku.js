var sudoku = (function() {

	// SUDOKU

	// 2014 - Einar Egilsson

	// This sudoku library is based HEAVILY on Peter Norvig's excellent Sudoku solver,
	// available at http://norvig.com/sudoku.html.

	// This library contains a solver, generator, serialization of puzzles
	// and methods to get conflicts and hints for puzzles that are in progress.
	// For a completely straight port of Norvig's solver, look at
	// https://github.com/einaregilsson/sudoku.js/sudoku-norvig.js

	// To see a better explanation of this library, look at the blog post
	// at http://einaregilsson.com/sudoku and to see it in action try
	// my Sudoku game at http://www.sudoku-webgame.com


	// Start by setting up some basic datastructures and connections
	// between them. Each row is numbered from 1-9, each column
	// from A-I. Each square has an id like E4.

	// Throughout this libarary we will often use the following variable names:

	//    d - digit
	//    r - row
	//    c - column
	//    s - square, e.g. E5
	//    u - unit, e.g. one whole row, column or box of squares.

	var ROWS 		= ['1','2','3','4','5','6','7','8','9']
	  , COLS 		= ['A','B','C','D','E','F','G','H','I']
	  , DIGITS		= '123456789'
	  , SQUARES 	= cross(COLS, ROWS) //Simple list of all squares, [A1, A2, ..., I9]
	  , UNITLIST 	= []  //List of all units. Each unit contains 9 squares. [ [A1,A2,...A9], [B1,B2,...,B9]...]
	  , UNITS		= {}  //Units organized by square. UNITS['A1'] = [ ['A1'...'A9'], ['A1'...'I1'], ['A1'...'C3']]
	  , PEERS		= {}; //For each square, the list of other square that share a unit with it. PEERS['A1'] = ['A1', 'A2' ... 'H1','I1']



	var undefined;

	for (var i=0; i<ROWS.length; i++) {
		UNITLIST.push(cross(COLS, [ROWS[i]])); 
	}

	for (var i=0; i<COLS.length; i++) {
		UNITLIST.push(cross([COLS[i]],ROWS)); 
	}

	var groupCols = ['ABC', 'DEF', 'GHI'];
	var groupRows = ['123', '456', '789'];
	for (var c=0;c<groupCols.length;c++) {
		for (var r=0;r<groupRows.length;r++) {
			UNITLIST.push(cross(chars(groupCols[c]), chars(groupRows[r])));
		}
	}

	for (var i=0; i<SQUARES.length;i++) {
		var square 		= SQUARES[i]
		  , squarePeers = []
		  , squareUnits = [];
		
		for (var j=0; j<UNITLIST.length; j++) {
			var unit = UNITLIST[j];
			if (contains(unit, square)) {
				squareUnits.push(unit);
				for (var k=0; k<unit.length;k++) {
					if (!contains(squarePeers, unit[k]) && unit[k] !== square) {
						squarePeers.push(unit[k]);
					}
				}
			}
		}
		UNITS[square] = squareUnits;
		PEERS[square] = squarePeers;
	}

	///// Utility methods. /////

	//Array.indexOf is not supported in old IEs
	function vals(obj) {
		var result = [];
		for (var key in obj) {
			result.push(obj[key]);
		}
		return result;
	}

	function keys(obj) {
		var result = [];
		for (var key in obj) {
			result.push(key);
		}
		return result;
	}

	function each(list, func) {
		filter(list, func);
	}

	function dict(keys, values) {
		var result = {};
		each(keys, function(i, key) {
			result[key] = values[i];
		});
		return result;
	}

	function print(s) {
		console.log(s + '\r\n');
	}

	function all(list, func) {
		for (var i=0; i < list.length; i++) {
			if (!func(list[i])) {
				return false;
			}
		}
		return true;
	}

	function any(list, func) {
		for (var i=0; i < list.length; i++) {
			var result = func(list[i]);
			if (result) {
				return result;
			}
		}
		return false;
	}

	function filter(list, func) {
		var result = [];
		for (var i=0; i < list.length; i++) {
			if (func.length > 1 ) {
				if (func(i, list[i])) {
					result.push(list[i]);
				}
			} else if (func(list[i])) {
				result.push(list[i]);
			}
		}
		return result;
	}

	function sum(list) {
		var result = 0;
		each(list, function(l) {
			if (typeof l == 'number') {
				result += l;
			} else if (typeof l == 'boolean') {
				result += l ? 1 : 0;
			} else {
				throw 'Only numbers and booleans supported';
			}
		});
		return result;
	}

	function some(seq, func) {
		//Return some element ofseq that is true.
		for (var i=0; i < seq.length; i++) {
			var result = func(seq[i]);
			if (result) {
				return result;
			}
		}
		return false;
	}

	function first(list, func) {
		var result = [];
		for (var i=0; i < list.length; i++) {
			if (func.length > 1 ) {
				if (func(i, list[i])) {
					return list[i];
				}
			} else if (func(list[i])) {
				return list[i];
			}
		}
		return null;
	}

	function map(list, expr) {
		var result = [];
		each(list, function(value) {
			if (typeof expr === 'function') {
				result.push(expr(value));
			} else if (typeof expr === 'string') {
				result.push(value[expr]);
			}
		});
		return result;
	}

	function max(list, expr) {
		var maxValue;
		
		each(list, function(value) {
			var candidate;
			if (typeof expr === 'undefined') {
				candidate = value;
			} else if (typeof expr === 'string') {
				candidate = value[expr];
			} else if (typeof expr === 'function') {
				candidate = expr(value);
			}
			
			if (typeof maxValue === 'undefined' || candidate > maxValue) {
				maxValue = candidate;
			}
		});
		return maxValue;
	}

	function min(list, expr) {
		var minValue;
		
		each(list, function(value) {
			var candidate;
			if (typeof expr === 'undefined') {
				candidate = value;
			} else if (typeof expr === 'string') {
				candidate = value[expr];
			} else if (typeof expr === 'function') {
				candidate = expr(value);
			}
			
			if (typeof minValue === 'undefined' || candidate < minValue) {
				minValue = candidate;
			}
		});
		return minValue;
	}

	function randomElement(list) {
		return list[Math.floor(Math.random() * list.length)];
	}


	//Array.indexOf is not supported in old IEs
	function contains(list, val) {
		return any(list, function(x) { return x === val; });
	}

	function set(list) {
		var result = [];
		each(list, function(val) {
			if (!contains(result, val)) {
				result.push(val);
			}
		});
		return result;
	}

	function concat() {
		return Array.prototype.concat.apply([], arguments);
	}

	function repeat(str, times) {
		return Array(times+1).join(str);
	}

	function center(str, width) {
		var pad = width - str.length;
		if (pad <= 0) {
			return str;
		}
		return repeat(' ', Math.floor(pad / 2)) 
			+ str 
			+ repeat(' ', Math.ceil(pad / 2));
	}

	function copy(board) {
		return dict(keys(board), vals(board));
	}

	function randomInt (min, max) {
	    return Math.floor(Math.random() * (max - min + 1)) + min;
	}

	function shuffle(seq) {
	    //Return a randomly shuffled copy of the input sequence.
	    seq = map(seq, function(x) { return x;});
		//Fisher yates shuffle
		var i = seq.length;
		while (--i) {
			var j = Math.floor(Math.random() * (i + 1));
			var ival = seq[i];
			var jval = seq[j];
			seq[i] = jval;
			seq[j] = ival;
		}
		
	    return seq
	}

	function range(count) {
		var result = [];
		for (var i=0; i < count; i++) {
			result.push(i);
		}
		return result;
	}

	function chars(s) {
		var result = [];
		for (var i = 0; i < s.length; i++) {
			result.push(s.charAt(i));
		}
		return result;
	}

	function cross(a, b) {
		var result = [];
		for (var i=0; i<a.length;i++) {
			for (var j=0; j<b.length;j++) {
				result.push(a[i] + b[j]);
			}
		}
		return result;
	}

	function getHint(puzzle, values) {
		if (!values) {
			throw { message : 'Values must be sent in'};
		}
		var solved = solve(puzzle);

		var errorSquares = [];
		// 1. Check if there are any wrong fields, Hint about those first
		for (var s in values) {
			var guess = values[s];
			if (guess && guess !== solved[s]) {
				errorSquares.push(s);
			}
		}

		if (errorSquares.length > 0) {
			return {
				type : 'error',
				square : randomElement(errorSquares)
			};
		}

		// 2. Find a field that has only one possibility and give a hint about that.
		var elimValues = {};
		for (var s in solved) {
			elimValues[s] = DIGITS;
		}

		// One round of elimination only
		for (var s in values) {
			elimValues[s] = values[s];
			var digit = values[s];
			var units = UNITS[s];
			for (var i = 0; i < PEERS[s].length; i++) {
				var elimSquare = PEERS[s][i];
				elimValues[elimSquare] = elimValues[elimSquare].replace(digit, '');
			}
		}

		var hintSquares = [];
		for (var s in elimValues) {
			if (elimValues[s].length == 1 && !values[s]) {
				hintSquares.push(s);
			} 
		}
		if (hintSquares.length > 0) {
			return {
				type : 'squarehint',
				square : randomElement(hintSquares)
			}
		}


		var unitHints = [];
		// 3. Is there a unit where one digit is only a possibility in one square?
		for (var s in elimValues) {
			var value = elimValues[s];
			if (value.length == 1) {
				continue;
			}
			var units = UNITS[s];
			for (var i = 0; i < value.length; i++) {
				var d = value.charAt(i);
				for (var u =0; u < units.length; u++) {
					var unit = units[u];
					if (all(unit, function(s2) {
						return s2 == s || elimValues[s2].indexOf(d) == -1;
					})) {
						var unitType = 'box';
						if (unit[0].charAt(0) == unit[8].charAt(0)) {
							unitType = 'row';
						} else if (unit[0].charAt(1) == unit[8].charAt(1)) {
							unitType = 'column';
						}
						unitHints.push({
							type : 'unithint',
							unitType : unitType,
							unit : unit,
							digit : d
						});
					}
				}
			}
		}

		if (unitHints.length > 0) {
			return randomElement(unitHints);
		}
		
		return {
			type : 'dontknow',
			squares : elimValues
		};
	}
	
	function getConflicts(values) {
		
		var errors = [];	
		for (var key in values) {
			var value = values[key] + '';
			if (!value || value.length > 1) {
				continue;
			}

			var units = UNITS[key];
			for (var i = 0; i < UNITS[key].length; i++) {
				var unit = UNITS[key][i];
				for (var j=0; j< unit.length; j++) {
					var otherKey = unit[j];
					var otherValue = values[otherKey] + '';
					
					if (otherKey != key && value == otherValue){
						errors.push({
							unit : unit,
							errorFields : [key, otherKey]
						});
					}
				}
			}
		}
		return errors;
	}

	function solve(grid, options) {
		return search(parseGrid(grid), options);
	}

	function search(values, options) {
		options = options || {};
		options.chooseDigit = options.chooseDigit || 'random';
		options.chooseSquare = options.chooseSquare || 'minDigits';

		//Using depth-first search and propagation, try all possible values."
		if (values === false) {
			return false; //Failed earlier
		}

		if (all(SQUARES, function(s) { return values[s].length == 1; })) {
			return values; // Solved!
		}
		
		//Chose the unfilled square s with the fewest possibilities
		var candidates = filter(SQUARES, function(s) { return values[s].length > 1; });
		candidates.sort(function(s1,s2) { 
			if (values[s1].length != values[s2].length) {
				return values[s1].length - values[s2].length; 
			}
			if (s1 < s2) {
				return -1;
			} else {
				return 1;
			}

		});

		var s;
		if (options.chooseSquare == 'minDigits') {
			s = candidates[0];
		} else if (options.chooseSquare == 'maxDigits') {
			s = candidates[candidates.length-1];
		} else if (options.chooseSquare == 'random') {
			s = candidates[Math.floor(Math.random()*candidates.length)];
		}

		var digitsLeft = chars(values[s]);
		if (options.chooseDigit == 'max') {
			digitsLeft.reverse();
		} else if (options.chooseDigit == 'random') {
			digitsLeft = shuffle(digitsLeft);
		}

		return some(digitsLeft, function(d) { return search(assign(copy(values), s, d), options) });
	}

	function isUnique(grid) {
		var input = typeof grid === 'string' ? gridValues(grid) : grid;

		var solved1 = solve(input, { chooseDigit : 'min'});
		var solved2 = solve(input, { chooseDigit : 'max'});
		if (!solved1 || !solved2) {
			throw 'Failed to solve';
		}

		for (var s in solved1) {
			if (solved2[s] != solved1[s]) {
				return false;
			}
		}
		return true;

	}

	function serialize(values) {
		var serialized = '';
		for (var i=0; i< SQUARES.length; i++) {
			serialized += values[SQUARES[i]] || 'x';
		}
		serialized = serialized.replace(/xxxxxx/g, 'f')
								.replace(/xxxxx/g, 'e')
								.replace(/xxxx/g, 'd')
								.replace(/xxx/g, 'c')
								.replace(/xx/g, 'b')
								.replace(/x/g, 'a');
		return serialized;
	}

	function deserialize(serialized) {
		var values = {};
		serialized = serialized.replace(/f/g, 'xxxxxx')
								.replace(/e/g, 'xxxxx')
								.replace(/d/g, 'xxxx')
								.replace(/c/g, 'xxx')
								.replace(/b/g, 'xx')
								.replace(/a/g, 'x');
		
		for (var i=0; i< SQUARES.length; i++) {
			if (serialized.charAt(i) != 'x') {
				values[SQUARES[i]] = serialized.charAt(i);
			}
		}
		return values;
	}

	function isSolvableWithElimination(grid) {
		return isSolved(parseGrid(grid));
	}

	function isSolved(values) {
		for (var s in values) {
			if (values[s].length > 1) {
				return false;
			}
		}
		return true;
	}

	function squareCount(difficulty) {
		if (difficulty == 'easy') {
			return 35;
		} else if (difficulty == 'medium') {
			return 28;
		}
		return 20;
	}

	function generate(difficulty) {
		var start = new Date().getTime();
		var minSquares = squareCount(difficulty || 'easy');
		
		var fullGrid = solve({});
		var generatedGrid = copy(fullGrid);
		var shuffledSquares = shuffle(SQUARES);
		var filledSquares = shuffledSquares.length;

		for (var i = 0; i < shuffledSquares.length; i++) {
			var s = shuffledSquares[i];

			delete generatedGrid[s];
			filledSquares--;
			if (!isSolvableWithElimination(generatedGrid) || !isUnique(generatedGrid)) {
				generatedGrid[s] = fullGrid[s];
				filledSquares++;
			}

			if (filledSquares === minSquares) {
				break;
			}

		}
		var time = new Date().getTime() - start;
		debug('Generated puzzle with ' + keys(generatedGrid).length + ' squares in ' + time + 'ms');
		return generatedGrid;
	}
 
	function parseGrid(grid) {
		//Convert grid to a dict of possible values, {square: digits}, or
		//return false if a contradiction is detected

		// To start, every square can be any digit; then assign values from the grid.
		var values = {}; 
		each(SQUARES, function(s) { values[s] = DIGITS; });
		

		var input = typeof grid === 'string' ? gridValues(grid) : grid;
		for (var s in input) {
			var d = input[s];
			if (!assign(values, s, d)) {
				return false; // (Fail if we can't assign d to square s.)
			}
		}
		return values;	
	}

	function gridValues(grid) {
	    //Convert grid into a dict of {square: char} with '0' or '.' for empties.
		grid = grid.replace(/[^0-9\.]/g, '');
		var input = {};
		for (var i = 0; i < SQUARES.length; i++){
			var val = grid[i];
			if (DIGITS.indexOf(val) != -1) {
				input[SQUARES[i]] = val;
			}
		}
	    return input;
	}

	//################ Constraint Propagation ################

	function assign(values, s, d) {
		//Eliminate all the other values (except d) from values[s] and propagate.
		//Return values, except return false if a contradiction is detected.
		var otherValues = values[s].replace(d, '');
		if (all(chars(otherValues), function(d2) { return eliminate(values, s, d2); })) {
			return values;
		} else {
			return false;
		}
	}

	function eliminate(values, s, d) {
		//Eliminate d from values[s]; propagate when values or places <= 2.
		//return values, except return false if a contradiction is detected.
		
		if (values[s].indexOf(d) == -1) {
			return values; //Already eliminated
		}
		
		values[s] = values[s].replace(d, '');
		// (1) If a square s is reduced to one value d2, then eliminate d2 from the peers.
		if (values[s].length == 0) {
			return false; //Contradiction: removed last value
		} else if (values[s].length == 1) {
			var d2 = values[s];
			if (!all(PEERS[s], function(s2) { return eliminate(values, s2, d2); })) {
				return false;
			}
		}
		// (2) If a unit u is reduced to only one place for a value d, then put it there.
		for (var i=0; i < UNITS[s].length; i++) {
			var u = UNITS[s][i];
			var dplaces = filter(u, function(s2) { return values[s2].indexOf(d) != -1; });
			if (dplaces.length == 0) {
				return false; //Contradiction: no place for this value
			} else if (dplaces.length == 1) {
				// d can only be in one place in unit; assign it there
				if (!assign(values, dplaces[0], d)) {
					return false;
				}
			}
		}

		return values;
	}

	var module = {
		solve : solve,
		getConflicts : getConflicts,
		getHint : getHint,
		isUnique : isUnique,
		generate : generate,
		serialize : serialize,
		deserialize : deserialize,
		debug : false,
		test : parseGrid,
		unitList : UNITLIST
	};

	function debug(msg) {
		if (module.debug) {
			print(msg);
		}
	}

	return module;
})();

if (typeof exports !== 'undefined') {
	for (var i in sudoku) {
		exports[i] = sudoku[i];
	}
}
