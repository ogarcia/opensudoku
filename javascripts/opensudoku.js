/*
 * opensudoku.js
 * Copyright (C) 2017 Óscar García Amor <ogarcia@connectical.com>
 *
 * Distributed under terms of the GNU GPLv3 license.
 */

var sudokuWorker
var counter
var puzzles
var selectedLevel

function savePuzzles (level="easy") {
// generate and save .opensudoku file
	
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
  
  var blob =  new Blob([header + puzzles + footer], {type: "text/xml"});
  
  let filePrefix = level === 'veryhard'? 'very_hard' : level;
  
  saveAs(blob, filePrefix +"_generated.opensudoku");
  
}



window.onload = function() {

  if (window.Worker) {
	  
	var progressBar = document.getElementById('progress');
	  
	// setup web worker
    sudokuWorker = new Worker('javascripts/worker.js');
    
    sudokuWorker.onmessage = function(msg) {
	
	  progressBar.value++
		
      if (++counter === 20) {
    	  savePuzzles(selectedLevel);
    	  document.getElementById('progressDisplay').style.display = 'none';
    	  progressBar.value = 0
      }
      else {
		// more puzzles need to be generated. Append last generated puzzle to the puzzles variable and trigger next generation
  	  
        var osformat = msg.data
                    .replace(/a/g, '0')
                    .replace(/b/g, '00')
                    .replace(/c/g, '000')
                    .replace(/d/g, '0000')
                    .replace(/e/g, '00000')
                    .replace(/f/g, '000000');
        puzzles += '  <game data="' + osformat + '" />\n';
  	  
        // generate next puzzle
        sudokuWorker.postMessage({level: selectedLevel});
      }
    }

    // listen to click on generation links
    var generateLinks = document.getElementById('generateLinks');
    
    generateLinks.onclick = function(event) {
  	  
  	  var level = event.target.id;
  	
      if(level) {
		  
		document.getElementById('progressDisplay').style.display = 'block';
  		
  	    event.preventDefault();
  	    
  	    // reset generated puzzles counter
  	    counter = 0;
  	    puzzles = '';
  	    selectedLevel = level;
  	    
  	    // generate first puzzle
  	    sudokuWorker.postMessage({level: selectedLevel});
  	    
  	  }
  	
    }
  }
  else {
    // visitor's browser does not support web workers
    document.getElementById('puzzle-generation').innerHTML = '<p>If you want more puzzles, this page offers a puzzle generation feature. However, this feature is not compatible with your browser and therefore disabled. Please try again from a browser supporting "web workers".</p>';
  }
  
}
