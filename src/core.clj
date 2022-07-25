(ns core
  (:import
    [com.badlogic.gdx Game Gdx]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
    (com.badlogic.gdx.graphics OrthographicCamera)
    (com.badlogic.gdx.graphics.glutils ShapeRenderer)
    (com.badlogic.gdx.utils.viewport FitViewport)))

(defn create-game [create-initial-screen-fn]
  (let [[font batch shape-renderer
         :as disposables] (repeatedly 3 (fn [] (atom nil)))
        view-port    (atom nil)
        world-width  800
        world-height 800]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn
                           (do
                             (reset! font (BitmapFont.))
                             (reset! batch (SpriteBatch.))
                             (reset! shape-renderer (ShapeRenderer.))
                             (reset! view-port (let [camera (doto (OrthographicCamera.)
                                                              (.update))
                                                     vp     (FitViewport. world-width world-height camera)]
                                                 #_(.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
                                                 (.update vp (.getWidth Gdx/graphics) (.getHeight Gdx/graphics) true)
                                                 #_(.update vp world-width world-height true)
                                                 vp))
                             {:game           this
                              :font           @font
                              :batch          @batch
                              :world-width    world-width
                              :world-height   world-height
                              :shape-renderer @shape-renderer
                              :view-port      @view-port})))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))
        ))))