/*
 * opensudoku.js
 * Copyright (C) 2017 Óscar García Amor <ogarcia@connectical.com>
 *
 * Distributed under terms of the GNU GPLv3 license.
 */

function generateSudoku (level="easy", number=10) {
  var date = new Date().toJSON().slice(0,10);
  var header = '<?xml version="1.0" encoding="UTF-8"?>\n<opensudoku>\n';
  header += '  <name>' + level + ' generated at ' + date + '</name>\n';
  header += '  <author>ogarcia</author>\n';
  header += '  <description></description>\n';
  header += '  <comment></comment>\n';
  header += '  <created>' + date + '</created>\n';
  header += '  <source>opensudoku-web-generator</source>\n';
  header += '  <level>' + level + '</level>\n';
  header += '  <sourceURL>http://opensudoku.moire.org/#about-puzzles</sourceURL>\n'
  var footer = '</opensudoku>\n';
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
  var veryhard = document.getElementById("veryhard");

  var data = [];
  var properties = {type: 'text/xml'};

  easy.onclick = function() {
    var blob = new Blob([generateSudoku("easy", 20)], {type: "text/xml"});
    saveAs(blob, "easy_generated.opensudoku");
  }
  medium.onclick = function() {
    var blob = new Blob([generateSudoku("medium", 20)], {type: "text/xml"});
    saveAs(blob, "medium_generated.opensudoku");
  }
  hard.onclick = function() {
    var blob = new Blob([generateSudoku("hard", 20)], {type: "text/xml"});
    saveAs(blob, "hard_generated.opensudoku");
  }
  veryhard.onclick = function() {
    var blob = new Blob([generateSudoku("veryhard", 20)], {type: "text/xml"});
    saveAs(blob, "very_hard_generated.opensudoku");
  }
}
