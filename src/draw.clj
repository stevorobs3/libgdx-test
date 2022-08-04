(ns draw
  (:import (com.badlogic.gdx.graphics.g2d SpriteBatch BitmapFont)
           (com.badlogic.gdx.graphics OrthographicCamera Color)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer$ShapeType)
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



;todo: reuse vector2's
(defn- square
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

(defn grid [shape-renderer rect-size line-thickness x-offset
                  num-rows num-cols]
  (let [fill-color    Color/BLACK
        outline-color Color/DARK_GRAY]
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (doseq [i (range num-cols)
            j (range num-rows)]
      (square shape-renderer
              (+ (* rect-size i) x-offset) (* rect-size j)
              rect-size rect-size
              line-thickness
              fill-color
              outline-color))
    (.end shape-renderer)))

(defn piece
  [shape-renderer
   color
   rect-size
   line-thickness
   x-offset
   vertices]
  (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
  (doseq [[x y] vertices]

    (square shape-renderer
            (+ x-offset (* x rect-size)) (* y rect-size)
            rect-size rect-size
            line-thickness
            color
            Color/WHITE))
  (.end shape-renderer))