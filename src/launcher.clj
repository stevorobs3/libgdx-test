(ns launcher
  (:require
    [core]
    [game-screen]
    [menu-screen])
  (:import
    [com.badlogic.gdx.backends.lwjgl LwjglApplication]
    (com.badlogic.gdx Game))
  (:gen-class))

(declare create-menu-screen)

(defn create-game-screen [game]
  (game-screen/create game create-menu-screen))

(defn create-menu-screen [game]
  (menu-screen/create game create-game-screen))

(defn -main []
  (let [game ^Game (core/create-game create-menu-screen)]
    (LwjglApplication. game "demo" 800 600)))

(comment
  (do (require '[launcher]
               '[core])
      (launcher/-main)))