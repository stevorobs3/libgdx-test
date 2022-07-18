(ns launcher
  (:require [core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard]
           [core Game])
  (:gen-class))

(defn -main []
  (LwjglApplication. (Game.) "demo" 800 600)
  (Keyboard/enableRepeatEvents true))

(comment

  (-main))