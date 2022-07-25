(ns game-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.utils.viewport Viewport)
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
  (.setColor shape-renderer fill-color)
  (.rect shape-renderer x y width height)
  (.setColor shape-renderer outline-color)
  (doseq [[start end] [[(Vector2. 0 0) (Vector2. 0 height)]
                       [(Vector2. 0 height) (Vector2. width height)]
                       [(Vector2. width height) (Vector2. width 0)]
                       [(Vector2. width 0) (Vector2. 0 0)]]]
    (.rectLine shape-renderer (.add start (Vector2. x y)) (.add end (Vector2. x y)) line-thickness)))


(defn- draw-grid [shape-renderer rect-size line-thickness x-offset
                  num-rows num-cols]
  (let [fill-color    Color/BLACK
        outline-color Color/DARK_GRAY]
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (doseq [i (range num-cols)
            j (range num-rows)]
      (draw-square shape-renderer
                   (+ (* rect-size i) x-offset) (* rect-size j)
                   rect-size rect-size
                   line-thickness
                   fill-color
                   outline-color))
    (.end shape-renderer)))

(defn- draw-tetromino [shape-renderer color rect-size line-thickness x-offset vertices]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [[x y] vertices]

    (draw-square shape-renderer
                 (+ x-offset (* x rect-size)) (* y rect-size)
                 rect-size rect-size
                 line-thickness
                 color
                 Color/WHITE))
  (.end shape-renderer))


(def ^:private fps-timer (atom nil))
(def ^:private fps (atom nil))
(defn- debug-fps [^SpriteBatch sprite-batch
                  ^BitmapFont font
                  ^OrthographicCamera camera
                  delta-time]
  (.setProjectionMatrix sprite-batch (.combined camera))
  (when (or (nil? @fps-timer) (>= @fps-timer 1))
    (reset! fps-timer 0)
    (reset! fps (format "%.1f" (/ 1 delta-time))))
  (swap! fps-timer (fn [fps-timer]
                     (+ (or fps-timer 0)
                        delta-time)
                     ))
  (.begin sprite-batch)
  (.setColor font Color/RED)
  (.draw font sprite-batch (str "FPS=" @fps) (float 20) (float 20))
  (.end sprite-batch))

(defn- render [{:keys [delta-time
                       world-width
                       world-height
                       ^ShapeRenderer shape-renderer
                       ^SpriteBatch sprite-batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               {:keys [tetrominos
                       timer] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        num-rows                   20
        num-cols                   10
        rect-size                  (/ world-height num-rows)
        grid-line-thickness        4
        tetromino-line-thickness   2
        x-offset                   (- (/ world-width 2) (/ (* num-cols rect-size) 2))
        move-time                  1
        new-timer                  (+ timer delta-time)
        [new-tetrominos new-timer] (if (>= new-timer move-time)
                                     [(->> tetrominos
                                           (map (fn [tetromino]
                                                  (update tetromino :vertices
                                                          (fn [vertices]
                                                            (map
                                                              (fn [[x y]] [x (dec y)])
                                                              vertices))))))
                                      (- new-timer move-time)]
                                     [tetrominos new-timer])
        ]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw-grid shape-renderer rect-size grid-line-thickness x-offset
               num-rows num-cols)

    (doseq [{:keys [color vertices] :as _tetromino} new-tetrominos]
      (draw-tetromino shape-renderer
                      color rect-size tetromino-line-thickness x-offset
                      vertices))

    #_(debug-fps sprite-batch font (.getCamera view-port) delta-time)
    (assoc state :tetrominos new-tetrominos
                 :timer new-timer)))

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
  (let [state (atom {:view-port  (first view-ports)
                     :tetrominos [{:vertices [[4 19]
                                              [5 19]
                                              [4 18]
                                              [5 18]]
                                   :color    Color/SALMON}
                                  {:vertices [[6 19]
                                              [7 19]
                                              [8 19]
                                              [9 19]]
                                   :color    Color/OLIVE}
                                  #_{:vertices [[2 2]
                                                [2 3]
                                                [3 2]
                                                [3 3]]
                                     :color    Color/TEAL}]
                     :timer      0})]
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