// sudoku generation web worker

importScripts('sudoku.js');

onmessage = function(msg) {
  
  var puzzle = sudoku.generate(msg.data.level);
  var serialized = sudoku.serialize(puzzle);
    
  postMessage(serialized);
};
