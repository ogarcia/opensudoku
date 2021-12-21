/*
 * opensudoku.js
 * Copyright (C) 2017 Óscar García Amor <ogarcia@connectical.com>
 *
 * Distributed under terms of the GNU GPLv3 license.
 */

function generateSudoku (level="easy", number=10) {
	
  // set .opensudoku file header
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
  
  // generate puzzles
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
  
  // generate and save .opensudoku file
  var blob =  new Blob([header + puzzles + footer], {type: "text/xml"});
  
  let filePrefix = level === 'veryhard'? 'very_hard' : level;
  
  saveAs(blob, filePrefix +"_generated.opensudoku");
  
}

window.onload = function() {

  var generateLinks = document.getElementById('generateLinks');
  
  generateLinks.onclick = function(event) {
	  
	var level = event.target.id;
	
    if(level) {
		
	  event.preventDefault();
	  
	  generateSudoku(level, 20);
	}
	
  }
  
}
