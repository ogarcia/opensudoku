/*
 * opensudoku.js
 * Copyright (C) 2017 Óscar García Amor <ogarcia@connectical.com>
 *
 * Distributed under terms of the GNU GPLv3 license.
 */

function generateSudoku (level="easy", number=10) {
  var date = new Date().toJSON().slice(0,10);
  var header = '<?xml version="1.0" encoding="UTF-8"?>\n<opensudoku>\n';
  header += '  <name>Sudoku ' + level + ' generated at ' + date + '</name>\n';
  header += '  <author>ogarcia</author>\n';
  header += '  <description>OpenSudoku levels automagically generated</description>\n';
  header += '  <comment>Generated levels</comment>\n';
  header += '  <created>' + date + '</created>\n';
  header += '  <source>opensudoku-web-generator</source>\n';
  header += '  <level>' + level + '</level>\n';
  var footer = '</opensudoku>';
  var puzzles = '';
  for (i = 0; i < number; i++) {
    var puzzle = sudoku.generate(level);
    var serialized = sudoku.serialize(puzzle);
    var osformat = serialized
                  .replace(/a/g, '0')
                  .replace(/b/g, '00')
                  .replace(/c/g, '000')
                  .replace(/d/g, '0000')
                  .replace(/e/g, '00000')
                  .replace(/f/g, '000000');
    puzzles += '  <game data="' + osformat + '" />\n';
  }
  return header + puzzles + footer;
}

window.onload = function() {
  var easy = document.getElementById('easy');
  var medium = document.getElementById("medium");
  var hard = document.getElementById("hard");

  var data = [];
  var properties = {type: 'text/xml'};

  easy.onclick = function() {
    var blob = new Blob([generateSudoku("easy", 50)], {type: "text/xml;charset=utf-8"});
    saveAs(blob, "easy-generated.opensudoku");
  }
  medium.onclick = function() {
    var blob = new Blob([generateSudoku("medium", 50)], {type: "text/xml;charset=utf-8"});
    saveAs(blob, "medium-generated.opensudoku");
  }
  hard.onclick = function() {
    var blob = new Blob([generateSudoku("hard", 50)], {type: "text/xml;charset=utf-8"});
    saveAs(blob, "hard-generated.opensudoku");
  }
}
