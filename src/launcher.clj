(ns launcher
  (:require
    [core]
    [game-screen]
    [menu-screen]
    [end-game-screen])
  (:import
    [com.badlogic.gdx.backends.lwjgl LwjglApplication LwjglApplicationConfiguration]
    (com.badlogic.gdx Game))
  (:gen-class))

(declare create-menu-screen
         create-end-game-screen)

(defn create-game-screen [context]
  (game-screen/create context create-menu-screen create-end-game-screen))

(defn create-menu-screen [context]
  (menu-screen/create context create-game-screen))

(defn create-end-game-screen [context score]
  (end-game-screen/create context score create-game-screen))

(defn -main []
  (let [game   ^Game (core/create-game create-menu-screen)
        config (LwjglApplicationConfiguration.)]
    (set! (.title config) "test")
    (set! (.width config) 1024)
    (set! (.height config) 768)
    (LwjglApplication. game config)))

(comment
  (do (require '[launcher]
               '[core])
      (launcher/-main)))