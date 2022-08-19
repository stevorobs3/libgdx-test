(ns scoring)

(defn level->down-move-time [level]
  (float (/ (case level
              0 48
              1 43
              2 38
              3 33
              4 28
              5 23
              6 18
              7 13
              8 8
              9 6
              (cond
                (<= 10 level 12) 5
                (<= 13 level 15) 4
                (<= 16 level 18) 3
                (<= 19 level 28) 2
                (>= level 29) 1))
            60)))

(defn level->lines [level]
  (case level
    0 10
    1 20
    2 30
    3 40
    4 50
    5 60
    6 70
    7 80
    8 90
    16 110
    17 120
    18 130
    19 140
    20 150
    21 160
    22 170
    23 180
    24 190

    (cond
      (<= 9 level 15) 100
      (<= 25 level 28) 200
      ; this is the max level
      (>= 29) :infinity)))

(def initial-score
  {:points                     0
   :level                      0
   :lines-cleared              0
   :lines-remaining-this-level (scoring/level->lines 0)})

(defn points-for-clearing-rows [row-count]
  (case row-count
    1 10
    2 40
    3 90
    4 160
    0))

(defn clear-lines
  [{:keys [level points lines-cleared lines-remaining-this-level]}
   n-lines-cleared]
  (let [level-complete? (>= n-lines-cleared lines-remaining-this-level)
        new-level       (if level-complete?
                          (inc level)
                          level)]
    {:points                     (+ points (points-for-clearing-rows n-lines-cleared))
     :level                      new-level
     :lines-remaining-this-level (if level-complete?
                                   (- (+ (level->lines new-level)
                                         lines-remaining-this-level)
                                      n-lines-cleared)
                                   (- lines-remaining-this-level n-lines-cleared))
     :lines-cleared              (+ lines-cleared n-lines-cleared)}))