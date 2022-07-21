(ns game-screen
  (:import (com.badlogic.gdx Game Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20)))

(defn- render [{:keys [r g b]}]
  (.glClearColor Gdx/gl r g b 1)
  (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT))

(defn- key-down [key-code {:keys [game] :as context} state new-state create-game-screen]
  (prn "typed in game screen" key-code Input$Keys/SPACE)
  (reset! state (new-state))
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  true)

(defn- show [context state new-state create-menu-screen]
  (prn "showing game screen")
  (.setInputProcessor Gdx/input
                      (proxy [InputAdapter] []
                        (keyDown [keycode]
                          (key-down keycode context state new-state create-menu-screen)))))
(defn create [context create-menu-screen]
  (let [rand-float #(float (/ (rand-int Integer/MAX_VALUE) Integer/MAX_VALUE))
        new-state  #(hash-map :r (rand-float)
                              :g (rand-float)
                              :b (rand-float))
        state      (atom (new-state))]
    (proxy [Screen] []
      (render [delta]
        (render @state))
      (show []
        (show context state new-state create-menu-screen)
        )
      (hide []
        (prn "hiding game screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (prn "resizing" width height))
      (pause [])
      (resume [])
      (dispose []))))