(ns core
  (:import
    [com.badlogic.gdx Game]))

(defn create-game [create-initial-screen-fn]
  (proxy [Game] []
    (create []
      (.setScreen this (create-initial-screen-fn this))
      (prn "creating game!"))
    (dispose []
      (prn "disposing"))
    )
  )