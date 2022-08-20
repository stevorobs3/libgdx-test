(ns tetris
  (:require [clojure.set :as set]
            [medley.core :as medley]
            [scoring])
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
    :index    0
    :vertices [[0 0]
               [0 1]
               [-1 0]
               [-1 1]]}
   {:type     :I
    :index    1
    :vertices [[-2 0]
               [-1 0]
               [0 0]
               [1 0]]}
   {:type     :T
    :index    2
    :vertices [[0 1]
               [1 0]
               [0 0]
               [-1 0]]}
   {:type     :S
    :index    3
    :vertices [[0 0]
               [1 0]
               [0 -1]
               [-1 -1]]}
   {:type     :Z
    :index    4
    :vertices [[0 0]
               [-1 0]
               [0 -1]
               [1 -1]]}
   {:type     :L
    :index    5
    :vertices [[0 -1]
               [0 0]
               [0 1]
               [1 -1]]}
   {:type     :J
    :index    6
    :vertices [[0 0]
               [1 0]
               [-1 0]
               [-1 1]]}])

(defn normalise-vertices
  "return a piece with position = 0,0 and vertices offset from there"
  [{:keys [position] :as piece}]
  (-> piece
      (update :vertices (fn [vertices]
                          (map #(vector (+ (first %) (first position))
                                        (+ (second %) (second position)))
                               vertices)))
      (assoc :position [0 0])))

(defn random-piece [piece-spawn-point]
  (-> pieces
      shuffle
      first
      (assoc :position piece-spawn-point)))

(def all-pieces
  (->> (zipmap [0 2 3 6 8 10 12] pieces)
       (map (fn [[index piece]]
              (assoc piece :position [2 index])))
       (map normalise-vertices)))

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

(defn- move-piece [{:keys [pieces piece] :as state} [direction-x direction-y] num-cols]
  (let [new-piece (update piece :position (fn [[x y]]
                                            [(+ x direction-x)
                                             (+ y direction-y)]))
        collided? (collision? pieces new-piece num-cols)]

    (merge state
           {:piece     (if collided? piece new-piece)
            :collided? collided?})))

(defn move-piece-to-bottom [state num-cols]
  (when (:piece state)
    (loop [state state]
      (let [new-state (move-piece state [0 -1] num-cols)]
        (if (:collided? new-state)
          new-state
          (recur new-state))))))

(defn- rotate-piece [pieces piece num-cols]
  (let [new-piece (update piece :vertices rotate-90)
        collided? (collision? pieces new-piece num-cols)]
    {:piece (if collided? piece new-piece)}))

(defn check-for-complete-rows [{:keys [pieces] :as state} num-cols]
  (let [complete-rows   (find-complete-rows pieces num-cols)
        next-game-state (if (seq complete-rows)
                          ::clearing
                          ::playing)]
    (cond-> state
            (seq complete-rows) (assoc :complete-rows complete-rows)
            :always (assoc :game-state next-game-state))))

(defn normalise-collided-piece [{:keys [pieces piece collided?] :as state}]
  (cond-> state
          collided? (-> (assoc :pieces (conj pieces (normalise-vertices piece)))
                        (dissoc :collided? :piece))))

(defn spawn-new-piece [{:keys [complete-rows piece-spawn-point piece] :as state}]
  (cond-> state
          (and (nil? piece)
               (empty? complete-rows)) (assoc :piece (random-piece piece-spawn-point))))

(defn check-for-game-over
  [{:keys [pieces grid] :as state}]
  (let [game-over? (->> pieces
                        (mapcat :vertices)
                        (filter #(>= (second %) (:num-rows grid)))
                        (not= []))]
    (cond-> state
            game-over? (assoc :game-state ::game-over))))

(defn piece-movement
  [{:keys [fast-move-time
           sideways-fast-move-time
           old-move-time
           move-time
           grid
           piece
           pieces] :as state}
   direction]
  (let [{:keys [num-cols]} grid]
    (case direction
      :left (move-piece state [-1 0] num-cols)
      :right (move-piece state [1 0] num-cols)
      :up (merge state (rotate-piece pieces piece num-cols))
      :down-speed-up (-> state
                         (assoc-in [:move-time :down] fast-move-time)
                         (assoc-in [:timer :down] 0.0)
                         (assoc :old-move-time (:down move-time)))
      :down-slow-down (-> state
                          (assoc-in [:move-time :down] old-move-time)
                          (dissoc :old-move-time))
      ;todo: deduplicate
      :start-left-auto-move (-> state
                                (assoc-in [:move-time :sideways] sideways-fast-move-time)
                                (assoc :move-direction :left)
                                (assoc-in [:timer :sideways] 0.0))
      :stop-left-auto-move (-> state
                               (medley/dissoc-in [:move-time :sideways]
                                                 [:timer :sideways]
                                                 [:move-direction]))
      :start-right-auto-move (-> state
                                 (assoc-in [:move-time :sideways] sideways-fast-move-time)
                                 (assoc :move-direction :right)
                                 (assoc-in [:timer :sideways] 0.0))
      :stop-right-auto-move (-> state
                                (medley/dissoc-in [:move-time :sideways]
                                                  [:timer :sideways]
                                                  [:move-direction]))
      :down (-> state
                (move-piece [0 -1] num-cols)
                normalise-collided-piece
                (check-for-complete-rows num-cols)
                spawn-new-piece
                check-for-game-over)
      :full-down (if (get-in state [:timer :full-down])     ;; ignore full down while full down timer is running.
                   state
                   (-> state
                       (move-piece-to-bottom num-cols)
                       normalise-collided-piece
                       (check-for-complete-rows num-cols)
                       spawn-new-piece
                       (assoc-in [:timer :full-down] 0.0)
                       check-for-game-over)))))

(defn key-down
  [key-code
   {:keys [game] :as context}
   state
   create-menu-screen]
  (if (= (:game-state state) ::playing)
    (cond
      (= key-code Input$Keys/ESCAPE) (do (.setScreen game (create-menu-screen context))
                                         state)
      (= key-code Input$Keys/LEFT) (-> state
                                       (piece-movement :left)
                                       (piece-movement :start-left-auto-move))
      (= key-code Input$Keys/RIGHT) (-> state
                                        (piece-movement :right)
                                        (piece-movement :start-right-auto-move))
      (= key-code Input$Keys/UP) (piece-movement state :up)
      (= key-code Input$Keys/DOWN) (-> state
                                       (piece-movement :down)
                                       (piece-movement :down-speed-up))
      (= key-code Input$Keys/SPACE) (piece-movement state :full-down)
      :else state)
    state))

(defn key-up
  [key-code
   _context
   {:keys [move-direction] :as state}]
  (if (= (:game-state state) ::playing)
    (cond
      (= key-code Input$Keys/DOWN) (piece-movement state :down-slow-down)
      (= key-code Input$Keys/LEFT) (if (= move-direction :left)
                                     (piece-movement state :stop-left-auto-move)
                                     state)
      (= key-code Input$Keys/RIGHT) (if (= move-direction :right)
                                      (piece-movement state :stop-right-auto-move)
                                      state)
      :else state)
    state))

(defn- move-downwards [state move-time timer]
  (let [new-state (piece-movement state :down)]
    (merge
      state
      new-state
      {:timer (update timer :down - (:down move-time))})))

(defn- move-sideways [state move-time timer move-direction]
  (let [new-state (piece-movement state move-direction)]
    (merge
      state
      new-state
      {:timer (update timer :sideways - (:sideways move-time))})))

;todo: find a much nicer way to do this async move time  stuff  -  maybe with thread/sleep + futures +
; state swapping?
(defn do-playing-state [{:keys [move-time timer move-direction] :as state}
                        delta-time]

  (let [new-timer                    (cond-> timer
                                             move-direction (update :sideways + delta-time)
                                             (some? (get timer :full-down)) (update :full-down + delta-time)
                                             :always (update :down + delta-time))
        timer-exceeded?              (fn [timer-type]
                                       (and (timer-type new-timer) (>= (timer-type new-timer) (timer-type move-time))))
        down-move-time-exceeded?     (timer-exceeded? :down)
        sideways-move-time-exceeded? (timer-exceeded? :sideways)
        full-down-time-exceeded?     (timer-exceeded? :full-down)]
    (cond-> (merge state {:timer new-timer})
            down-move-time-exceeded? (move-downwards move-time new-timer)
            sideways-move-time-exceeded? (move-sideways move-time new-timer move-direction)
            full-down-time-exceeded? (medley/dissoc-in [:timer :full-down]))))

(defn do-clearing-state [{:keys [complete-rows pieces piece-spawn-point] :as state}]
  (let [row-complete?    (set complete-rows)
        ;;todo: do this in a nice animated way!
        new-pieces       (->> pieces
                              (map #(update % :vertices (fn [vertices]
                                                          (->> vertices
                                                               (remove (fn [[_ y]] (row-complete? y)))
                                                               (map (fn [[x y]]
                                                                      (let [num-rows-to-drop (count (filter (fn [r] (> y r)) complete-rows))]
                                                                        [x (- y num-rows-to-drop)])))))))
                              (filter #(seq (:vertices %))))
        piece            (random-piece piece-spawn-point)
        update-move-time (fn [{{:keys [level]} :score
                               :as             new-state}]
                           (when (not= (get-in state [:score :level]) level)
                             (println "changing  move time" level (scoring/level->down-move-time level)))
                           (assoc-in new-state [:move-time :down] (scoring/level->down-move-time level)))]

    (-> state
        (assoc :pieces new-pieces
               :piece piece
               :game-state ::playing)
        (dissoc :complete-rows)
        (update :score scoring/clear-lines (count complete-rows))
        update-move-time)))

(defn do-game-over-state
  [{:keys [game create-end-game-screen] :as context}
   {:keys [score] :as state}]
  (.setScreen game (create-end-game-screen context score))
  state)

(defn main-loop [context
                 {:keys [game-state
                         pieces
                         timer]
                  :as   state} delta-time]
  (let [{:keys [game-state]
         :as   state-with-defaults} (assoc state :pieces (or pieces [])
                                                 :timer (or timer {:down 0.0})
                                                 :game-state (or game-state ::clearing))]
    (case game-state
      ::playing (do-playing-state state-with-defaults delta-time)
      ::clearing (do-clearing-state state-with-defaults)
      ::game-over (do-game-over-state context state-with-defaults))))