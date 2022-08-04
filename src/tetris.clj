(ns tetris
  (:require [clojure.set :as set]
            [medley.core :as medley])
  (:import (com.badlogic.gdx Input$Keys)
           (com.badlogic.gdx.graphics Color)))

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

(defn normalise-vertices
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

(defn key-down
  [key-code
   {:keys [game] :as context}
   {:keys [pieces piece num-cols piece-spawn-point] :as state}
   create-game-screen]
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

(defn find-complete-rows
  [pieces num-cols]
  (->> pieces
       (mapcat :vertices)
       (group-by second)
       (medley/map-vals count)
       (filter #(= (second %) num-cols))
       keys))

(defn main-loop [{:keys [num-cols
                         move-time
                         piece
                         pieces
                         piece-spawn-point
                         timer] :as state} delta-time]
  (let [new-timer (+ timer delta-time)
        [new-pieces new-piece new-timer] (if (>= new-timer move-time)
                                           (let [next-piece (tetris/move-piece pieces piece [0 -1] num-cols piece-spawn-point)]
                                             (prn "complete rows are" (find-complete-rows (first next-piece) num-cols))

                                             (conj
                                               next-piece
                                               (- new-timer move-time)))
                                           [pieces piece new-timer])]
    (assoc state :pieces new-pieces
                 :piece new-piece
                 :timer new-timer)))