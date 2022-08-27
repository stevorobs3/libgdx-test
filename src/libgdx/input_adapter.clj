(ns libgdx.input-adapter
  (:import (com.badlogic.gdx InputAdapter)))

(defn create [{:keys [key-down
                      key-up]
               :or   {key-down (constantly nil)
                      key-up   (constantly nil)}}]
  (proxy [InputAdapter] []
    (keyDown [key-code]
      (key-down key-code)
      true)
    (keyUp [key-code]
      (key-up key-code)
      true)))