

# build application

clj -M -m core.Game

# run application

clj -M -m launcher

# Controls
## Menu screen controls:
* Start game: Space

##Game screen controls:
* Hard drop: Space
* Soft drop: down arrow
* Rotate: Up arrow
* Horizontal movement :Left and right Arrows
* back to main menu: Esc


# todos

* Add animations around the piece landing
* Add animation for row clearing
* Add queue of next pieces
* merge context and state into single object
* Add a schema that is validated only in dev mode
* Refactor the data structure to be cleaner
* Add a scoring
* Display score
* Add leveling system (speeds up)
* Add end game condition
* Add end game screen
* Split up the tetris namespace further into different components
* Add command to build a mac/windows/linux executable
* Port it to android
* Use images for the pieces and grid such that its pretty