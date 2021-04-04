# Open Sudoku

[Open Sudoku](https://opensudoku.moire.org/) is a simple open-source Sudoku game.
It's designed to be controlled both by finger and keyboard.
It's preloaded with 90 puzzles in 3 difficulty levels.
More puzzles can be downloaded from the web, and it also allows you to enter your own puzzles.

## Download

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/packages/org.moire.opensudoku/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=org.moire.opensudoku)


## Authors and Contributors

Current version of Open Sudoku is authored by [Óscar García Amor](https://ogarcia.me/).

The first version of Open Sudoku was developed by [Roman Mašek](https://github.com/romario333) and
was contributed to by Vit Hnilica, Martin Sobola, Martin Helff, and Diego Pierotto.

### Contributors

* [Sergey Pimanov](https://github.com/spimanov):
  * Added functionality to highlight cells with similar values

* [TacoTheDank](https://github.com/TacoTheDank):
  * Compile with and updated support up to Android Pie (except for the SDK 23+ permission check for external storage access)
  * Added Java 1.8 compatibility (lambdas, methods, etc)
  * Reformatted code
  * Slightly improved app performance within "res" files
  * Fixed the grammar of some strings
  * Migrated App to AppCompat
  * Migrated to AndroidX

* [steve3424](https://github.com/steve3424):
  * Fixed some bugs
  * Added permission checks for reading/writing external storage
  * Added puzzle solver

* [Justin Nordin](https://github.com/jlnordin):
  * Added awesome theme preview
  * Added new title screen with resume feature
  * Added spectacular full App UI themes
  * New action buttons for undo and settings in game screen
  * New option for highlight notes that match a particular number when a cell is selected
  * New menu option to undo to before mistake
  * New config option to remove notes on number entry
  * New config option to skip titlescreen
  * Fix folder view to show sudoku previews with custom theme colors
  * Fix the crash when no cell was selected and you used the _hint_ command
  * Fix crash when using the new feature to automatically remove cell notes

* [Chris Lane](https://github.com/ChrisLane):
  * Keep same values highlighted after empty cell selection

* [Jesus Fuentes](https://github.com/fuentesj11):
  * New menu option to sort puzzles by last played and play time

* [Daniel](https://github.com/demield):
  * Added copy and paste ability in sudoku editor

* [Роман](https://github.com/D0ct0rZl0):
  * Added _reset all_ option in sudoku list menu

## Support or Contact

Having trouble with Open Sudoku? Create an issue in https://github.com/ogarcia/opensudoku/issues or contact me via opensudoku@moire.org and I'll help you sort it out.
