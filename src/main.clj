(ns main
  (:require
    [core]
    [tetris.screens.high-score :as high-score-screen]
    [tetris.screens.game :as game-screen]
    [tetris.screens.menu :as menu-screen]
    [libgdx.game :as gdx-game])
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
  (high-score-screen/create context score create-game-screen))

(defn -main []
  (let [game   ^Game (gdx-game/create create-menu-screen 800 800)
        config (LwjglApplicationConfiguration.)]
    (set! (.title config) "Tetris")
    (set! (.width config) 1024)
    (set! (.height config) 768)
    (LwjglApplication. game config)))

(comment
  (do (require '[main]
               '[core])
      (main/-main)))