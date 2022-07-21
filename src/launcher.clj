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

(defn create-game-screen [context]
  (game-screen/create context create-menu-screen))

(defn create-menu-screen [context]
  (menu-screen/create context create-game-screen))

(defn -main []
  (let [game ^Game (core/create-game create-menu-screen)]
    (LwjglApplication. game "demo" 800 600)))

(comment
  (do (require '[launcher]
               '[core])
      (launcher/-main)))