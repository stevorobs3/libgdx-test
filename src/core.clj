(ns core
  (:import
    [com.badlogic.gdx Game Gdx]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
    (com.badlogic.gdx.graphics OrthographicCamera)
    (com.badlogic.gdx.graphics.glutils ShapeRenderer)))

(defn create-game [create-initial-screen-fn]
  (let [[font batch shape-renderer
         :as disposables] (repeatedly 3 (fn [] (atom nil)))
        camera       (atom nil)
        world-width  100
        world-height 100]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn {:game           this
                                                    :font           (reset! font (BitmapFont.))
                                                    :batch          (reset! batch (SpriteBatch.))
                                                    :world-width    world-width
                                                    :world-height   world-height
                                                    :camera         (reset! camera (let [c (doto (OrthographicCamera. world-width world-height)
                                                                                             (.update))]
                                                                                     (.set (.position c) (/ world-width 2) (/ world-height 2) 0)
                                                                                     c))
                                                    :shape-renderer (reset! shape-renderer (ShapeRenderer.))}))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))
        ))))