(ns game-screen
  (:require
    [draw]
    [tetris])
  (:import (com.badlogic.gdx Screen Gdx InputAdapter)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color Texture)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch TextureRegion)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)
           (com.badlogic.gdx.math Vector2)))

(defn- render [{:keys [delta-time
                       ^ShapeRenderer shape-renderer
                       ^SpriteBatch sprite-batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               {:keys [background-color
                       num-cols
                       num-rows
                       grid-line-thickness
                       grid-fill-color
                       grid-outline-color
                       piece-line-thickness
                       ghost-piece-color
                       grid-square-vertices
                       start-origin
                       end-origin
                       tiles
                       ^float rect-size
                       x-offset] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        {:keys [pieces piece] :as new-state} (tetris/main-loop state delta-time)]
    (.glClearColor Gdx/gl (.r background-color) (.g background-color) (.b background-color) (.a background-color))
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.glEnable Gdx/gl GL20/GL_BLEND)
    (.glBlendFunc Gdx/gl GL20/GL_SRC_ALPHA GL20/GL_ONE_MINUS_SRC_ALPHA)

    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw/grid shape-renderer rect-size grid-line-thickness x-offset
               num-rows num-cols
               grid-square-vertices
               grid-fill-color
               grid-outline-color)

    (when-let [ghost-piece (:piece (tetris/move-piece-to-bottom new-state num-cols))]

      (draw/ghost-piece shape-renderer
                        ghost-piece-color
                        rect-size piece-line-thickness x-offset
                        (:vertices (tetris/normalise-vertices ghost-piece))))
    (.begin sprite-batch)
    (doseq [{:keys [index vertices] :as _piece} (conj pieces (tetris/normalise-vertices piece))]
      (draw/piece sprite-batch
                  (nth tiles index)
                  rect-size
                  x-offset
                  vertices))
    (.end sprite-batch)

    (draw/debug-fps sprite-batch font camera delta-time)
    new-state))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn create [{:keys [world-height world-width] :as context} create-game-screen]
  (let [piece-spawn-point [5 19]
        num-rows          20
        num-cols          10
        rect-size         (/ world-height num-rows)
        tile-texture      (Texture. "tetris-tiles.png")
        tiles             (map (fn [number]
                                 (TextureRegion. tile-texture (int (+ (* 13 20) 8)) (int (+ (* number 20) 8)) (int 16) (int 16)))
                               [5 9 19 27 29 35 49])
        state             (atom {:background-color        (.cpy Color/GRAY)
                                 :num-rows                num-rows
                                 :num-cols                num-cols
                                 :move-time               {:down      1
                                                           :sideways  1
                                                           ;todo: this isn't a move-time, this should be done async instead, possibly just using futures...
                                                           :full-down 0.2}
                                 :fast-move-time          0.05
                                 :sideways-fast-move-time 0.2
                                 :grid-line-thickness     4
                                 :piece-line-thickness    2
                                 :rect-size               rect-size
                                 :piece-spawn-point       piece-spawn-point
                                 :tiles                   tiles
                                 :tile-texture            tile-texture
                                 :grid-fill-color         (.cpy Color/BLACK)
                                 :grid-outline-color      (let [color (.cpy Color/DARK_GRAY)]
                                                            (set! (.a color) 0.7)
                                                            color)
                                 :ghost-piece-color       (let [color (.cpy Color/WHITE)]
                                                            (set! (.a color) 0.4)
                                                            color)
                                 :x-offset                (- (/ world-width 2) (/ (* num-cols rect-size) 2))
                                 :grid-square-vertices    [[(Vector2. 0 0) (Vector2. 0 rect-size)]
                                                           [(Vector2. 0 rect-size) (Vector2. rect-size rect-size)]
                                                           [(Vector2. rect-size rect-size) (Vector2. rect-size 0)]
                                                           [(Vector2. rect-size 0) (Vector2. 0 0)]]})]
    (proxy [Screen] []
      (render [delta]
        (swap! state #(render (assoc context :delta-time delta) %)))
      (show []
        (println "showing game screen")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyDown [char]
                                (swap! state (fn [s] (tetris/key-down char context s create-game-screen)))
                                true)
                              (keyUp [char]
                                (swap! state (fn [s] (tetris/key-up char context s)))
                                true))))
      (hide []
        (println "hiding game screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (swap! state (fn [s] (resize context s width height))))
      (pause [])
      (resume [])
      (dispose []
        (.dispose tile-texture))
      )))