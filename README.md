

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

* Implement holding down key to move things horizontally
* Add queue of next pieces
* merge context and state into single object
* Add a schema that is validated only in dev mode
* Refactor the data structure to be cleaner