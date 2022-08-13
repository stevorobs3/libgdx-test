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

(defn find-complete-rows
  [pieces num-cols]
  (->> pieces
       (mapcat :vertices)
       (group-by second)
       (medley/map-vals count)
       (filter #(= (second %) num-cols))
       keys))

;todo: tidy up this function so that it just returns the position of the new piece, and separate out
; whether it was collided
(defn- move-piece [pieces piece [direction-x direction-y :as direction] num-cols piece-spawn-point]
  (let [new-piece       (update piece :position (fn [[x y]]
                                                  [(+ x direction-x)
                                                   (+ y direction-y)]))
        collided?       (collision? pieces new-piece num-cols)
        going-down?     (= direction [0 -1])
        new-state       (cond
                          (and collided? going-down?) {:pieces (conj pieces (normalise-vertices piece))
                                                       :piece  (random-piece piece-spawn-point)}
                          collided? {:pieces pieces
                                     :piece  piece}
                          :else {:pieces pieces
                                 :piece  new-piece})
        complete-rows   (find-complete-rows (:pieces new-state) num-cols)
        next-game-state (if (seq complete-rows)
                          ::clearing
                          ::playing)]

    (cond-> new-state
            (seq complete-rows) (assoc :complete-rows complete-rows)
            :always (assoc :game-state next-game-state))))

(defn- move-piece-simple [pieces piece [direction-x direction-y] num-cols]
  (let [new-piece (update piece :position (fn [[x y]]
                                            [(+ x direction-x)
                                             (+ y direction-y)]))]
    (if (collision? pieces new-piece num-cols) piece new-piece)))

(defn move-piece-to-bottom-simple [piece pieces num-cols]
  (loop [piece piece]
    (let [new-piece (move-piece-simple pieces piece [0 -1] num-cols)]
      (if (= new-piece piece)
        new-piece
        (recur new-piece)))))


(defn- move-piece-to-bottom [pieces piece num-cols piece-spawn-point]
  (loop [{:keys [pieces piece]} {:pieces pieces
                                 :piece  piece}]
    (let [new-state (move-piece pieces piece [0 -1] num-cols piece-spawn-point)]
      (if (> (count (:pieces new-state)) (count pieces))
        new-state
        (recur new-state)))))

(defn- rotate-piece [pieces piece num-cols]
  (let [new-piece (update piece :vertices rotate-90)
        collided? (collision? pieces new-piece num-cols)]
    (if collided? piece new-piece)))


(defn piece-movement
  [{:keys [fast-move-time
           old-move-time
           move-time
           num-cols
           piece
           pieces
           piece-spawn-point] :as state}
   direction]
  (case direction
    :left (merge state (move-piece pieces piece [-1 0] num-cols piece-spawn-point))
    :right (merge state (move-piece pieces piece [1 0] num-cols piece-spawn-point))
    :up (assoc state :piece (rotate-piece pieces piece num-cols))
    :down-speed-up (-> state
                       (assoc-in [:move-time :down] fast-move-time)
                       (assoc-in [:timer :down] 0.0)
                       (assoc :old-move-time (:down move-time)))
    :down-slow-down (-> state
                        (assoc-in [:move-time :down] old-move-time)
                        (dissoc :old-move-time))
    :down (merge state (move-piece pieces piece [0 -1] num-cols piece-spawn-point))
    :full-down (let [new-state (move-piece-to-bottom pieces piece num-cols piece-spawn-point)]
                 (merge state new-state))))

(defn key-down
  [key-code
   {:keys [game] :as context}
   state
   create-game-screen]
  (cond
    (= key-code Input$Keys/ESCAPE) (do (.setScreen game (create-game-screen context))
                                       state)
    (= key-code Input$Keys/LEFT) (piece-movement state :left)
    (= key-code Input$Keys/RIGHT) (piece-movement state :right)
    (= key-code Input$Keys/UP) (piece-movement state :up)
    (= key-code Input$Keys/DOWN) (piece-movement state :down-speed-up)
    (= key-code Input$Keys/SPACE) (piece-movement state :full-down)
    :else state))

(defn key-up
  [key-code
   _context
   state]
  (cond
    (= key-code Input$Keys/DOWN) (piece-movement state :down-slow-down)
    :else state))

(defn do-playing-state [{:keys [move-time timer] :as state}
                        delta-time]

  (let [new-timer (update timer :down + delta-time)]
    (if (>= (:down new-timer) (:down move-time))
      (let [new-state (piece-movement state :down)]
        (merge
          state
          new-state
          {:timer (update new-timer :down - (:down move-time))}))
      (merge state
             {:timer new-timer}))))

(defn do-clearing-state [{:keys [complete-rows pieces] :as state}]
  (let [row-complete? (set complete-rows)
        ;;todo: do this in a nice animated way!
        new-pieces    (->> pieces
                           (map #(update % :vertices (fn [vertices]
                                                       (->> vertices
                                                            (remove (fn [[_ y]] (row-complete? y)))
                                                            (map (fn [[x y]]
                                                                   (let [num-rows-to-drop (count (filter (fn [r] (> y r)) complete-rows))]
                                                                     [x (- y num-rows-to-drop)])))))))
                           (filter #(seq (:vertices %))))]

    (assoc state :pieces new-pieces
                 :game-state ::playing)))

(defn main-loop [{:keys [game-state
                         piece
                         pieces
                         piece-spawn-point
                         timer]
                  :as   state} delta-time]
  (let [
        ;; defaults for start of game
        piece (or piece
                  (random-piece piece-spawn-point))
        {:keys [game-state]
         :as   state-with-defaults} (assoc state :pieces (or pieces [])
                                                 :piece piece
                                                 :timer (or timer {:down 0.0})
                                                 :game-state (or game-state ::playing))]
    (case game-state
      ::playing (do-playing-state state-with-defaults delta-time)
      ::clearing (do-clearing-state state-with-defaults))))