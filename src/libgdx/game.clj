(ns libgdx.game
  (:import (com.badlogic.gdx Game Gdx)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer)
           (com.badlogic.gdx.graphics.g2d SpriteBatch BitmapFont)
           (com.badlogic.gdx.graphics OrthographicCamera)
           (com.badlogic.gdx.utils.viewport FitViewport)))


(defn create [create-initial-screen-fn world-width world-height]
  (let [[font sprite-batch shape-renderer
         :as disposables] (repeatedly 3 (fn [] (atom nil)))
        view-port    (atom nil)]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn
                           (do
                             (reset! font (BitmapFont.))
                             (reset! sprite-batch (SpriteBatch.))
                             (reset! shape-renderer (ShapeRenderer.))
                             (reset! view-port (let [camera (doto (OrthographicCamera.)
                                                              (.update))
                                                     vp     (FitViewport. world-width world-height camera)]
                                                 (.update vp (.getWidth Gdx/graphics) (.getHeight Gdx/graphics) true)
                                                 vp))
                             {:game           this
                              :font           @font
                              :sprite-batch   @sprite-batch
                              :world-width    world-width
                              :world-height   world-height
                              :shape-renderer @shape-renderer
                              :view-port      @view-port})))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))))))