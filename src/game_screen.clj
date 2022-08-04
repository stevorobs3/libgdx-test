(ns game-screen
  (:require
    [clojure.set :as set]
    [draw])
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)))

(defn rotate-90
  ([verts]
   (map #(rotate-90 (first %) (second %))
        verts))
  ([x y]
   [y (- x)]))

(def pieces
  [{:type     :O
    :color    Color/ROYAL
    :vertices [[0 0]
               [0 1]
               [-1 0]
               [-1 1]]}
   {:type     :I
    :color    Color/FIREBRICK
    :vertices [[-2 0]
               [-1 0]
               [0 0]
               [1 0]]}
   {:type     :T
    :color    Color/GOLDENROD
    :vertices [[0 1]
               [1 0]
               [0 0]
               [-1 0]]}
   {:type     :S
    :color    Color/FOREST
    :vertices [[0 0]
               [1 0]
               [0 -1]
               [-1 -1]]}
   {:type     :Z
    :color    Color/TEAL
    :vertices [[0 0]
               [-1 0]
               [0 -1]
               [1 -1]]}
   {:type     :L
    :color    Color/MAROON
    :vertices [[0 -1]
               [0 0]
               [0 1]
               [1 -1]]}
   {:type     :J
    :color    Color/LIGHT_GRAY
    :vertices [[0 0]
               [1 0]
               [-1 0]
               [-1 1]]}])

(defn- normalise-vertices
  "return a piece with position = 0,0 and vertices offset from there"
  [{:keys [position] :as piece}]
  (-> piece
      (update :vertices (fn [verts]
                          (map #(vector (+ (first %) (first position))
                                        (+ (second %) (second position)))
                               verts)))
      (assoc :position [0 0])))

(defn random-piece [piece-spawn-point]
  (-> pieces
      shuffle
      first
      (assoc :position piece-spawn-point)))

(defn collision?
  "returns true if the piece shares a vertex with any of the pieces or
   any of the left, bottom or right walls"
  [pieces piece num-cols]
  (let [piece-vertices   (-> piece normalise-vertices :vertices set)
        all-vertices     (->> pieces
                              (mapcat :vertices)
                              (into #{}))
        hit-other-piece? (-> (set/intersection all-vertices piece-vertices)
                             empty?
                             not)
        hit-left-wall?   (reduce (fn [acc [x _]]
                                   (or acc (< x 0)))
                                 false
                                 piece-vertices)
        hit-right-wall?  (reduce (fn [acc [x _]]
                                   (or acc (>= x num-cols)))
                                 false
                                 piece-vertices)
        hit-bottom-wall? (reduce (fn [acc [_ y]]
                                   (or acc (< y 0)))
                                 false
                                 piece-vertices)]
    (or hit-other-piece?
        hit-left-wall?
        hit-right-wall?
        hit-bottom-wall?)))

(defn- move-piece [pieces piece [direction-x direction-y :as direction] num-cols piece-spawn-point]
  (let [new-piece   (update piece :position (fn [[x y]]
                                              [(+ x direction-x)
                                               (+ y direction-y)]))
        collided?   (collision? pieces new-piece num-cols)
        going-down? (= direction [0 -1])]
    (cond
      (and collided? going-down?) [(conj pieces (normalise-vertices piece))
                                   (random-piece piece-spawn-point)]
      collided? [pieces piece]
      :else [pieces new-piece])))

(defn- rotate-piece [pieces piece num-cols]
  (let [new-piece (update piece :vertices rotate-90)
        collided? (collision? pieces new-piece num-cols)]
    (if collided? piece new-piece)))

(defn- render [{:keys [delta-time
                       world-width
                       world-height
                       ^ShapeRenderer shape-renderer
                       ^SpriteBatch sprite-batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               {:keys [num-cols
                       num-rows
                       move-time
                       piece
                       pieces
                       piece-spawn-point
                       timer] :as state}]
  (let [^OrthographicCamera camera (.getCamera view-port)
        rect-size                  (/ world-height num-rows)
        grid-line-thickness        4
        piece-line-thickness       2
        x-offset                   (- (/ world-width 2) (/ (* num-cols rect-size) 2))
        new-timer                  (+ timer delta-time)
        [new-pieces new-piece new-timer] (if (>= new-timer move-time)
                                           (conj
                                             (move-piece pieces piece [0 -1] num-cols piece-spawn-point)
                                             (- new-timer move-time))
                                           [pieces piece new-timer])]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (draw/grid shape-renderer rect-size grid-line-thickness x-offset
               num-rows num-cols)

    (doseq [{:keys [color vertices] :as _piece} (conj new-pieces (normalise-vertices new-piece))]
      (draw/piece shape-renderer
                  color rect-size piece-line-thickness x-offset
                  vertices))

    (draw/debug-fps sprite-batch font (.getCamera view-port) delta-time)
    (assoc state :pieces new-pieces
                 :piece new-piece
                 :timer new-timer)))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn- key-down
  [key-code
   {:keys [game] :as context}
   {:keys [pieces piece num-cols piece-spawn-point] :as state}
   create-game-screen]
  (println "typed in game screen" key-code Input$Keys/SPACE
           (= key-code Input$Keys/LEFT))
  (cond
    (= key-code Input$Keys/SPACE) (do (.setScreen game (create-game-screen context))
                                      state)
    (= key-code Input$Keys/LEFT) (let [[new-pieces new-piece] (move-piece pieces piece [-1 0] num-cols piece-spawn-point)]
                                   (assoc state :pieces new-pieces
                                                :piece new-piece))
    (= key-code Input$Keys/RIGHT) (let [[new-pieces new-piece] (move-piece pieces piece [1 0] num-cols piece-spawn-point)]
                                    (assoc state :pieces new-pieces
                                                 :piece new-piece))
    (= key-code Input$Keys/UP) (let [new-piece (rotate-piece pieces piece num-cols)]
                                 (assoc state :piece new-piece))
    (= key-code Input$Keys/DOWN) (let [[new-pieces new-piece] (loop [[pieces piece] [pieces piece]]
                                                                (let [[new-pieces new-piece] (move-piece pieces piece [0 -1] num-cols piece-spawn-point)]
                                                                  (if (> (count new-pieces) (count pieces))
                                                                    [new-pieces new-piece]
                                                                    (recur [new-pieces new-piece])
                                                                    )))]
                                   (assoc state :piece new-piece
                                                :pieces new-pieces))
    :else state))

(defn create [{:keys [view-ports] :as context} create-game-screen]
  (let [piece-spawn-point [5 19]
        state             (atom {:view-port         (first view-ports)
                                 :pieces            []
                                 :piece             (random-piece piece-spawn-point)
                                 :timer             0
                                 :num-rows          20
                                 :num-cols          10
                                 :move-time         1
                                 :piece-spawn-point piece-spawn-point})]
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