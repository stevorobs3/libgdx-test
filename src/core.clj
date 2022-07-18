(ns core
  (:import
    [com.badlogic.gdx Game Gdx]
    [com.badlogic.gdx.graphics GL20]
    (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)))

(defn- render [shape-renderer delta-time {:keys [position speed radius]}]
  (let [circle-x (+ (:x position) (* (:x speed) delta-time))
        circle-y (+ (:y position) (* (:y speed) delta-time))
        speed-x  (if (or (< circle-x radius)
                         (> circle-x (- (.getWidth Gdx/graphics) radius)))
                   (- (:x speed))
                   (:x speed))
        speed-y  (if (or (< circle-y radius)
                         (> circle-y (- (.getHeight Gdx/graphics) radius)))
                   (- (:y speed))
                   (:y speed))
        radius   50]

    (.glClearColor Gdx/gl (float 0.25) 0.25 (float 0.25) (float 1))
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (.setColor shape-renderer 0 0 1 1)

    (.circle shape-renderer circle-x circle-y radius)
    (.end shape-renderer)
    {:position {:x circle-x
                :y circle-y}
     :speed    {:x speed-x
                :y speed-y}
     :radius   radius}))

(defn create-game []
  (let [shape-renderer (atom nil)
        state          (atom {:position {:x 400
                                         :y 300}
                              :speed    {:x 120
                                         :y 60}
                              :radius   50})]
    (proxy [Game] []
      (create []
        (prn "creating game!")
        (swap! shape-renderer (constantly (ShapeRenderer.)))
        )
      (render []
        (swap! state (fn [s]
                       (render @shape-renderer (.getDeltaTime Gdx/graphics) s))))
      (dispose []
        (.dispose @shape-renderer)))))