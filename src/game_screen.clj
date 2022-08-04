(ns game-screen
  (:require
    [draw]
    [tetris])
  (:import (com.badlogic.gdx Screen Gdx InputAdapter)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
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
                       rect-size] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        x-offset                   (- (/ world-width 2) (/ (* num-cols rect-size) 2))
        {:keys [pieces piece] :as new-state} (tetris/main-loop state delta-time)]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw/grid shape-renderer rect-size grid-line-thickness x-offset
               num-rows num-cols)

    (doseq [{:keys [color vertices] :as _piece} (conj pieces (tetris/normalise-vertices piece))]
      (draw/piece shape-renderer
                  color rect-size piece-line-thickness x-offset
                  vertices))

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
        state             (atom {:pieces               []
                                 :piece                (tetris/random-piece piece-spawn-point)
                                 :timer                0
                                 :num-rows             num-rows
                                 :num-cols             num-cols
                                 :move-time            1
                                 :grid-line-thickness  4
                                 :piece-line-thickness 2
                                 :rect-size            (/ world-height num-rows)
                                 :piece-spawn-point    piece-spawn-point})]
    (proxy [Screen] []
      (render [delta]
        (swap! state #(render (assoc context :delta-time delta) %)))
      (show []
        (println "showing game screen")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyDown [char]
                                (swap! state (fn [s] (tetris/key-down char context s create-game-screen)))
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