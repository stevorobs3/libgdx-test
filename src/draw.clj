(ns draw
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch BitmapFont TextureRegion)
           (com.badlogic.gdx.graphics OrthographicCamera Color)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer$ShapeType ShapeRenderer)
           (com.badlogic.gdx.math Vector2)))

(def ^:private fps-timer (atom nil))
(def ^:private fps (atom nil))
(defn debug-fps [^SpriteBatch sprite-batch
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
    (.rectLine ^ShapeRenderer shape-renderer (.add (Vector2. x y) start) (.add (Vector2. x y) end) line-thickness)))

(defn grid
  [shape-renderer
   {:keys [line-thickness
           num-rows
           num-cols
           rect-size
           cell-vertex-pairs
           x-offset
           fill-color
           outline-color] :as _config}]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [i (range num-cols)
          j (range num-rows)]
    (cell shape-renderer
          (+ (* rect-size i) x-offset) (* rect-size j)
          rect-size rect-size
          line-thickness
          cell-vertex-pairs
          fill-color
          outline-color))
  (.end shape-renderer))

(defn ghost-piece
  [shape-renderer
   vertices
   {:keys [color
           line-thickness
           rect-size
           x-offset
           cell-vertex-pairs]}]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [[x y] vertices]

    (hollow-cell shape-renderer
                 (+ x-offset (* x rect-size))
                 (* y rect-size)
                 cell-vertex-pairs
                 line-thickness
                 color))
  (.end shape-renderer))

(defn piece
  ([^SpriteBatch sprite-batch
    ^TextureRegion tile
    rect-size
    x-offset
    vertices]
   (doseq [[x y] vertices]
     (.draw sprite-batch tile
            (float (+ x-offset (* x rect-size)))
            (float (* y rect-size))
            (float rect-size)
            (float rect-size)))))