(ns launcher
  (:require [core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main []
  (LwjglApplication. (core/create-game) "demo" 800 600)
  (Keyboard/enableRepeatEvents true))

(comment
  (do (require '[launcher]
               '[core])
      (launcher/-main)))