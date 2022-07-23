(ns core
  (:import
    [com.badlogic.gdx Game Gdx]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
    (com.badlogic.gdx.graphics OrthographicCamera)
    (com.badlogic.gdx.graphics.glutils ShapeRenderer)))

(defn create-game [create-initial-screen-fn]
  (let [[font batch shape-renderer
         :as disposables] (repeatedly 3 (fn [] (atom nil)))
        camera (atom nil)]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn {:game           this
                                                    :font           (reset! font (BitmapFont.))
                                                    :batch          (reset! batch (SpriteBatch.))
                                                    :camera         (reset! camera (doto (OrthographicCamera.)
                                                                                     (.setToOrtho true)
                                                                                     (.update)))
                                                    :shape-renderer (reset! shape-renderer (ShapeRenderer.))}))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))
        ))))