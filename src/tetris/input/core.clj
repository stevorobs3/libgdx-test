(ns tetris.input.core
  (:import (com.badlogic.gdx InputAdapter)))


(defn input-adapter [state context create-menu-screen]
  (proxy [InputAdapter] []
    (keyDown [char]
      (swap! state (fn [s] (tetris/key-down char context s create-menu-screen)))
      true)
    (keyUp [char]
      (swap! state (fn [s] (tetris/key-up char context s)))
      true))
  )