(ns tetris.screens.game
  (:require
    [tetris.render.core :as render]
    [tetris.input.core :as input]
    [tetris.scoring.core :as scoring]
    [libgdx.screen :as gdx-screen]
    [tetris.core :as tetris])
  (:import (com.badlogic.gdx.graphics Color Texture)
           (com.badlogic.gdx.graphics.g2d TextureRegion)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.math Vector2)))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn create
  [{:keys [world-height world-width] :as context}
   create-menu-screen
   create-end-game-screen]
  (let [piece-spawn-point [5 20]
        num-rows          20
        num-cols          10
        rect-size         (/ world-width (+ num-cols 14))
        x-offset          (- (/ world-width 2) (/ (* num-cols rect-size) 2))
        y-offset          rect-size
        tile-texture      (Texture. "tetris-tiles.png")
        tiles             (map (fn [number]
                                 (TextureRegion. tile-texture (int (+ (* 13 20) 8)) (int (+ (* number 20) 8)) (int 16) (int 16)))
                               [5 9 19 27 29 35 49])
        cell-vertices     [(Vector2. 0 0)
                           (Vector2. 0 rect-size)
                           (Vector2. rect-size rect-size)
                           (Vector2. rect-size 0)]
        cell-vertex-pairs (conj (partition 2 1 cell-vertices)
                                ((juxt last first) cell-vertices))
        grid              {:line-thickness    4
                           :cell-vertex-pairs cell-vertex-pairs
                           :num-rows          num-rows
                           :num-cols          num-cols
                           :rect-size         rect-size
                           ;todo: better name for this too!
                           :x-offset          x-offset
                           :y-offset          y-offset
                           :fill-color        (.cpy Color/BLACK)
                           :outline-color     (let [color (.cpy Color/DARK_GRAY)]
                                                (set! (.a color) 0.7)
                                                color)}
        hold-piece-grid   {:num-rows          5
                           :num-cols          6
                           :rect-size         (:rect-size grid)
                           :x-offset          (+ (:x-offset grid)
                                                 (- (* 6.5 (:rect-size grid))))
                           :y-offset          (* (:rect-size grid) 15)
                           :line-thickness    4
                           :cell-vertex-pairs cell-vertex-pairs
                           :fill-color        (.cpy Color/BLACK)
                           :outline-color     (let [color (.cpy Color/DARK_GRAY)]
                                                (set! (.a color) 0.7)
                                                color)}
        next-piece-grid   {:num-rows          10
                           :num-cols          6
                           :rect-size         (:rect-size grid)
                           :x-offset          (+ (:x-offset grid)
                                                 (+ (int (/ (:rect-size grid) 2)))
                                                 (* (:rect-size grid) 10))
                           :y-offset          (* (:rect-size grid) 7)
                           :line-thickness    4
                           :cell-vertex-pairs cell-vertex-pairs
                           :fill-color        (.cpy Color/BLACK)
                           :outline-color     (let [color (.cpy Color/DARK_GRAY)]
                                                (set! (.a color) 0.7)
                                                color)}
        ghost-piece       {:color             (let [color (.cpy Color/WHITE)]
                                                (set! (.a color) 0.4)
                                                color)
                           :line-thickness    2
                           :rect-size         rect-size
                           :x-offset          x-offset
                           :y-offset          y-offset
                           :cell-vertex-pairs cell-vertex-pairs}
        state             (atom {:background-color        (.cpy Color/GRAY)
                                 ;todo: move times need to be made simpler
                                 :move-time               {:down      (scoring/level->down-move-time 0)
                                                           :sideways  1
                                                           ;todo: this isn't a move-time, this should be done async instead, possibly just using futures...
                                                           :full-down 0.2}
                                 :fast-move-time          0.05
                                 :sideways-fast-move-time 0.2
                                 :piece-line-thickness    2
                                 :piece-spawn-point       piece-spawn-point
                                 :next-piece-grid         next-piece-grid
                                 :hold-piece-grid         hold-piece-grid
                                 :tiles                   tiles
                                 :x-offset                x-offset
                                 :grid                    grid
                                 :ghost-piece             ghost-piece
                                 :score                   scoring/initial-score})
        context           (assoc context :create-end-game-screen create-end-game-screen)]
    (gdx-screen/create
      (input/input-adapter state context create-menu-screen)
      {:render  (fn [delta]
                  (swap! state #(tetris/main-loop context % delta))
                  (render/render (assoc context :delta-time delta)
                                 @state))
       :resize  (fn [width height]
                  (swap! state (fn [s] (resize context s width height))))
       :dispose (fn [] (.dispose tile-texture))})))