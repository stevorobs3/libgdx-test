(ns core
  (:import
    [com.badlogic.gdx Game Gdx Input$Keys Input$Buttons]
    [com.badlogic.gdx.graphics GL20]
    (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)))

(defn- render [shape-renderer delta-time {:keys [position radius]}]
  (let [circle-x (cond-> (:x position)
                         (.isButtonPressed Gdx/input Input$Buttons/LEFT) ((fn [x] (.getX Gdx/input)))
                         (.isKeyPressed Gdx/input Input$Keys/A) dec
                         (.isKeyPressed Gdx/input Input$Keys/D) inc)
        circle-y (cond-> (:y position)
                         (.isButtonPressed Gdx/input Input$Buttons/LEFT) ((fn [y] (- (.getHeight Gdx/graphics) (.getY Gdx/input))))
                         (.isKeyPressed Gdx/input Input$Keys/W) inc
                         (.isKeyPressed Gdx/input Input$Keys/S) dec)
        radius   50]



    (.glClearColor Gdx/gl (float 0.25) 0.25 (float 0.25) (float 1))
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (.setColor shape-renderer 0 0 1 1)

    (.circle shape-renderer circle-x circle-y radius)
    (.end shape-renderer)
    {:position {:x circle-x
                :y circle-y}
     :radius   radius}))

(defn create-game []
  (let [shape-renderer (atom nil)
        state          (atom {:position {:x 400
                                         :y 300}
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