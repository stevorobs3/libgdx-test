(ns tetris.input.core
  (:require [tetris.core :as tetris])
  (:import (com.badlogic.gdx InputAdapter Input$Keys)))

(defn key-down
  [key-code
   {:keys [game] :as context}
   state
   create-menu-screen]
  (if (= (:game-state state) ::tetris/playing)
    (cond
      (= key-code Input$Keys/ESCAPE) (do (.setScreen game (create-menu-screen context))
                                         state)
      (= key-code Input$Keys/LEFT) (-> state
                                       (tetris/piece-movement :left)
                                       (tetris/piece-movement :start-left-auto-move))
      (= key-code Input$Keys/RIGHT) (-> state
                                        (tetris/piece-movement :right)
                                        (tetris/piece-movement :start-right-auto-move))
      (= key-code Input$Keys/UP) (tetris/piece-movement state :up)
      (= key-code Input$Keys/DOWN) (-> state
                                       (tetris/piece-movement :down)
                                       (tetris/piece-movement :down-speed-up))
      (= key-code Input$Keys/SPACE) (tetris/piece-movement state :full-down)
      :else state)
    state))

(defn key-up
  [key-code
   _context
   {:keys [move-direction] :as state}]
  (if (= (:game-state state) ::tetris/playing)
    (cond
      (= key-code Input$Keys/DOWN) (tetris/piece-movement state :down-slow-down)
      (= key-code Input$Keys/LEFT) (if (= move-direction :left)
                                     (tetris/piece-movement state :stop-left-auto-move)
                                     state)
      (= key-code Input$Keys/RIGHT) (if (= move-direction :right)
                                      (tetris/piece-movement state :stop-right-auto-move)
                                      state)
      :else state)
    state))

(defn input-adapter [state context create-menu-screen]
  (proxy [InputAdapter] []
    (keyDown [char]
      (swap! state (fn [s] (key-down char context s create-menu-screen)))
      true)
    (keyUp [char]
      (swap! state (fn [s] (key-up char context s)))
      true)))