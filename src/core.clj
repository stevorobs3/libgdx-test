(ns core
  (:import
    [com.badlogic.gdx Game Gdx Input$Keys Input$Buttons InputAdapter]
    [com.badlogic.gdx.graphics GL20]
    (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)))

(defn- render [{:keys [r g b] :as state}]
  (.glClearColor Gdx/gl r g b 1)
  (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
  state
  )

(defn create-game []
  (let [rand-float #(float (/ (rand-int Integer/MAX_VALUE) Integer/MAX_VALUE))
        new-state  #(hash-map :r (rand-float)
                              :g (rand-float)
                              :b (rand-float))
        state      (atom (new-state))]
    (proxy [Game] []
      (create []
        (prn "creating game!")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyTyped [char]
                                (prn "typed" char (type char))
                                (swap! state (fn [_] (new-state)))
                                true))))
      (render []
        (swap! state (fn [s]
                       (render s))))
      )))