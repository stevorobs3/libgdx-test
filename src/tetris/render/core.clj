(ns tetris.render.core
  (:require
    [tetris.core :as tetris])
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch BitmapFont TextureRegion GlyphLayout)
           (com.badlogic.gdx.graphics OrthographicCamera Color GL20)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer$ShapeType ShapeRenderer)
           (com.badlogic.gdx.math Vector2)
           (com.badlogic.gdx.utils Align)
           (com.badlogic.gdx Gdx)
           (com.badlogic.gdx.utils.viewport Viewport)))

(def ^:private fps-timer (atom nil))
(def ^:private fps (atom nil))
(defn render-debug-fps [^SpriteBatch sprite-batch
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

(defn render-score [^SpriteBatch sprite-batch
                    ^BitmapFont font
                    ^OrthographicCamera camera
                    {:keys [points level lines-cleared] :as _score}
                    world-width
                    world-height
                    rect-size]
  ;todo: use scene 2d ui? + make it pretty.
  (let [points-text                       (str " Score: " points)
        level-text                        (str " Level: " level)
        lines-cleared-text                (str " Lines: " lines-cleared)
        grid-x                            (float (+ (* rect-size 5) (/ world-width 2)))
        text-width                        (float (- world-width (* 10 rect-size)))
        ^GlyphLayout points-layout        (GlyphLayout. font points-text Color/BLUE text-width Align/left true)
        ^GlyphLayout level-layout         (GlyphLayout. font level-text Color/BLUE text-width Align/left true)
        ^GlyphLayout lines-cleared-layout (GlyphLayout. font lines-cleared-text Color/BLUE text-width Align/left true)]
    (.setProjectionMatrix sprite-batch (.combined camera))
    (.begin sprite-batch)
    (.setScale (.getData font) 2)
    (.draw font sprite-batch points-layout grid-x
           (float (- world-height
                     (.height points-layout))))
    (.draw font sprite-batch level-layout grid-x
           (float (- world-height
                     (* 3 (.height points-layout)))))
    (.draw font sprite-batch lines-cleared-layout grid-x
           (float (- world-height
                     (* 5 (.height points-layout)))))

    (.end sprite-batch)))

(defn- cell
  [shape-renderer
   x y
   width height
   line-thickness
   vertex-pairs
   fill-color
   outline-color]
  (.setColor shape-renderer fill-color)
  (.rect shape-renderer x y width height)
  (.setColor shape-renderer outline-color)
  (let [start-origin (Vector2. x y)
        end-origin   (Vector2. x y)
        reset-vec    (fn [v]
                       (set! (.x v) x)
                       (set! (.y v) y)
                       v)]
    (doseq [[start end] vertex-pairs]
      (.rectLine ^ShapeRenderer shape-renderer
                 (.add (reset-vec start-origin) start)
                 (.add (reset-vec end-origin) end) line-thickness))))

(defn- hollow-cell
  [shape-renderer
   x y
   vertex-pairs
   line-thickness
   ^Color outline-color]
  (.setColor shape-renderer outline-color)
  (doseq [[start end] vertex-pairs]
    (.rectLine ^ShapeRenderer shape-renderer
               (.add (Vector2. x y) start)
               (.add (Vector2. x y) end)
               line-thickness)))

(defn render-grid
  [shape-renderer
   {:keys [line-thickness
           num-rows
           num-cols
           rect-size
           cell-vertex-pairs
           x-offset
           y-offset
           fill-color
           outline-color]
    :as   _config
    :or   {y-offset 0}}]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [i (range num-cols)
          j (range num-rows)]
    (cell shape-renderer
          (+ (* rect-size i) x-offset)
          (+ (* rect-size j) y-offset)
          rect-size rect-size
          line-thickness
          cell-vertex-pairs
          fill-color
          outline-color))
  (.end shape-renderer))

(defn render-ghost-piece
  [shape-renderer
   vertices
   {:keys [color
           line-thickness
           rect-size
           x-offset
           y-offset
           cell-vertex-pairs]
    :or   {y-offset 40}}]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [[x y] vertices]
    (hollow-cell shape-renderer
                 (+ x-offset (* x rect-size))
                 (+ y-offset (* y rect-size))
                 cell-vertex-pairs
                 line-thickness
                 color))
  (.end shape-renderer))

(defn render-piece
  ([^SpriteBatch sprite-batch
    ^TextureRegion tile
    {:keys [rect-size x-offset y-offset] :as _grid
     :or   {y-offset 0}}
    vertices]
   (doseq [[x y] vertices]
     (.draw sprite-batch tile
            (float (+ x-offset (* x rect-size)))
            (float (+ y-offset (* y rect-size)))
            (float rect-size)
            (float rect-size)))))

(defn render
  [{:keys [delta-time
           ^ShapeRenderer shape-renderer
           ^SpriteBatch sprite-batch
           ^BitmapFont font
           ^Viewport view-port
           world-width
           world-height]}
   {:keys [background-color
           ghost-piece
           grid
           next-piece-grid
           next-piece
           piece
           pieces
           score
           tiles] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        rect-size                  (:rect-size grid)
        cell-vertices              [(Vector2. 0 0)
                                    (Vector2. 0 rect-size)
                                    (Vector2. rect-size rect-size)
                                    (Vector2. rect-size 0)]
        cell-vertex-pairs          (conj (partition 2 1 cell-vertices)
                                         ((juxt last first) cell-vertices))
        next-piece-grid            {:num-rows          12
                                    :num-cols          6
                                    :rect-size         (:rect-size grid)
                                    :x-offset          (+ (:x-offset grid)
                                                          (+ (int (/ (:rect-size grid) 2)))
                                                          (* (:rect-size grid) 10))
                                    :y-offset          (* (:rect-size grid) 3)
                                    :line-thickness    4
                                    :cell-vertex-pairs cell-vertex-pairs
                                    :fill-color        (.cpy Color/BLACK)
                                    :outline-color     (let [color (.cpy Color/DARK_GRAY)]
                                                         (set! (.a color) 0.7)
                                                         color)}]
    (.glClearColor Gdx/gl
                   (.r background-color)
                   (.g background-color)
                   (.b background-color)
                   (.a background-color))
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.glEnable Gdx/gl GL20/GL_BLEND)
    (.glBlendFunc Gdx/gl GL20/GL_SRC_ALPHA GL20/GL_ONE_MINUS_SRC_ALPHA)

    (.setProjectionMatrix shape-renderer (.combined camera))
    (render-grid shape-renderer grid)
    (render-grid shape-renderer next-piece-grid)


    (when-let [{:keys [piece]} (tetris/move-piece-to-bottom state (:num-cols grid))]
      (render-ghost-piece shape-renderer
                          (:vertices (tetris/normalise-vertices piece))
                          ghost-piece))
    (.begin sprite-batch)
    (doseq [{:keys [index vertices] :as _piece} (conj pieces (tetris/normalise-vertices piece))]
      (render-piece sprite-batch
                    (nth tiles index)
                    grid
                    vertices))
    (when (:next-piece state)
      (let [{:keys [index vertices]} (tetris/normalise-vertices
                                       (assoc next-piece
                                         :position [2 1]))]
        ;todo: render in a different grid
        (render-piece sprite-batch
                      (nth tiles index)
                      next-piece-grid
                      vertices)))
    (.end sprite-batch)

    (render-score sprite-batch font camera score
                  world-width world-height (:rect-size grid))
    (render-debug-fps sprite-batch font camera delta-time)))
