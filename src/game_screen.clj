(ns game-screen
  (:require
    [draw]
    [tetris])
  (:import (com.badlogic.gdx Screen Gdx InputAdapter)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color Texture)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch TextureRegion)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)))

(defn- render [{:keys [delta-time
                       world-width
                       ^ShapeRenderer shape-renderer
                       ^SpriteBatch sprite-batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               {:keys [num-cols
                       num-rows
                       grid-line-thickness
                       piece-line-thickness
                       tiles
                       ^Texture tile-texture
                       ^float rect-size] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        x-offset                   (- (/ world-width 2) (/ (* num-cols rect-size) 2))
        {:keys [pieces piece] :as new-state} (tetris/main-loop state delta-time)
        tiles                      (map (fn [number]
                                          (TextureRegion. tile-texture (int (+ (* 13 20) 8)) (int (+ (* number 20) 8)) (int 16) (int 16)))
                                        [5 9 19 27 29 35 49])]
    (let [color Color/GRAY]
      (.glClearColor Gdx/gl (.r color) (.g color) (.b color) (.a color)))
    (.glClear Gdx/gl (bit-or GL20/GL_COLOR_BUFFER_BIT GL20/GL_DEPTH_BUFFER_BIT))
    (.glEnable Gdx/gl GL20/GL_BLEND)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw/grid shape-renderer rect-size grid-line-thickness x-offset
               num-rows num-cols)

    (.begin sprite-batch)
    (doseq [{:keys [index vertices] :as _piece} (conj pieces (tetris/normalise-vertices piece))]
      (draw/piece sprite-batch
                  (nth tiles index)
                  rect-size
                  x-offset
                  vertices))
    (.end sprite-batch)
    ;todo: change this so that pieces comes first / pass in state???
    (when (:piece new-state)
      (let [ghost-piece (:piece (tetris/move-piece-to-bottom new-state num-cols))
            color       (.cpy Color/WHITE)]
        (set! (.a color) 0.25)

        (draw/piece-old shape-renderer
                        color
                        rect-size piece-line-thickness x-offset
                        (:vertices (tetris/normalise-vertices ghost-piece)))))

    (draw/debug-fps sprite-batch font camera delta-time)
    new-state))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn create [{:keys [world-height] :as context} create-game-screen]
  (let [piece-spawn-point [5 19]
        num-rows          20
        num-cols          10
        tile-texture      (Texture. "tetris-tiles.png")
        tiles             (map (fn [number]
                                 (TextureRegion. tile-texture (int (+ (* 13 20) 8)) (int (+ (* number 20) 8)) (int 16) (int 16)))
                               [5 9 19 27 29 35 49])
        state             (atom {:num-rows                num-rows
                                 :num-cols                num-cols
                                 :move-time               {:down     1
                                                           :sideways 1}
                                 :fast-move-time          0.05
                                 :sideways-fast-move-time 0.1
                                 :grid-line-thickness     4
                                 :piece-line-thickness    2
                                 :rect-size               (/ world-height num-rows)
                                 :piece-spawn-point       piece-spawn-point
                                 :tiles                   tiles
                                 :tile-texture            tile-texture})]
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