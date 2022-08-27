(ns libgdx.screen
  (:import (com.badlogic.gdx Screen Gdx)))


(defn create
  [input-adapter
   {:keys [render
           resize
           dispose]
    :as   _fns}]
  (proxy [Screen] []
    (render [delta]
      (render delta))
    (show []
      (println "showing screen")
      (.setInputProcessor Gdx/input input-adapter))
    (hide []
      (println "hiding screen")
      (.setInputProcessor Gdx/input nil))
    (resize [width height]
      (resize width height))
    (pause [])
    (resume [])
    (dispose []
      (dispose))))