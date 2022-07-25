(ns game-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.utils Align)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)
           (com.badlogic.gdx.math Vector2)))


;todo: probably pull .begin / .end outside of this method to a higher level?
;todo: reuse vector2's
(defn- draw-square
  [shape-renderer
   x y
   width height
   line-thickness
   ^Color fill-color
   ^Color outline-color]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (.setColor shape-renderer fill-color)
  (.rect shape-renderer x y width height)

  (.setColor shape-renderer outline-color)
  (doseq [[start end] [[(Vector2. 0 0) (Vector2. 0 height)]
                       [(Vector2. 0 height) (Vector2. width height)]
                       [(Vector2. width height) (Vector2. width 0)]
                       [(Vector2. width 0) (Vector2. 0 0)]]]
    (.rectLine shape-renderer (.add start (Vector2. x y)) (.add end (Vector2. x y)) line-thickness))
  (.end shape-renderer))


(defn- draw-grid [shape-renderer world-width world-height]
  (let [num-rows       20
        num-cols       10
        rect-size     (/ world-height num-rows)
        line-thickness (float 4)
        fill-color     Color/BLACK
        outline-color  Color/DARK_GRAY
        start-x (- (/ world-width 2) (/ (* num-cols rect-size) 2))]
    (doseq [i (range num-cols)
            j (range num-rows)]
      (draw-square shape-renderer
                   (+ (* rect-size i) start-x) (* rect-size j)
                   rect-size rect-size
                   line-thickness
                   fill-color
                   outline-color))))

(defn- render [{:keys [world-width
                       world-height
                       ^ShapeRenderer shape-renderer

                       ^Viewport view-port] :as _context}
               state]
  (let [^OrthographicCamera camera (.getCamera view-port)]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw-grid shape-renderer world-width world-height)




    state))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (println "typed in game screen" key-code Input$Keys/SPACE)
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  state)

(defn create [{:keys [view-ports] :as context} create-game-screen]
  (let [state (atom {:view-port (first view-ports)})]
    (proxy [Screen] []
      (render [delta]
        (swap! state #(render (assoc context :delta-time delta) %)))
      (show []
        (println "showing game screen")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyDown [char]
                                (swap! state (fn [s] (key-down char context s create-game-screen)))
                                true))))
      (hide []
        (println "hiding game screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (swap! state (fn [s] (resize context s width height))))
      (pause [])
      (resume [])
      (dispose [])
      )))