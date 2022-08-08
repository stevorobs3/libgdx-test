

# build application

clj -M -m core.Game

# run application

clj -M -m launcher

# todos

* Implement holding down key to move things horizontally
* Add queue of next pieces
* merge context and state into single object
* Add a schema that is validated only in dev mode
* Refactor the data structure to be cleaner