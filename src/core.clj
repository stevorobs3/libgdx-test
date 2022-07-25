(ns core
  (:import
    [com.badlogic.gdx Game]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
    (com.badlogic.gdx.graphics OrthographicCamera Texture)
    (com.badlogic.gdx.graphics.glutils ShapeRenderer)
    (com.badlogic.gdx.utils.viewport FitViewport FillViewport)))

(defn create-game [create-initial-screen-fn]
  (let [[font batch shape-renderer justina-texture
         :as disposables] (repeatedly 4 (fn [] (atom nil)))
        camera       (atom nil)
        view-port    (atom nil)
        world-width  1920
        world-height 1080]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn
                           (do
                             (reset! font (BitmapFont.))
                             (reset! batch (SpriteBatch.))
                             (reset! justina-texture (Texture. "justina.jpg"))
                             (let [camera (let [c (doto (OrthographicCamera.)
                                                    (.update))]
                                            (.set (.position c) (/ world-width 2) (/ world-height 2) 0)
                                            c)]
                               (reset! shape-renderer (ShapeRenderer.))
                               (reset! view-port (let [vp (FitViewport. world-width world-height camera)]
                                                   (.apply vp)
                                                   (.update vp world-width world-height true)
                                                   vp)))
                             {:game            this
                              :font            @font
                              :batch           @batch
                              :world-width     world-width
                              :world-height    world-height
                              :justina-texture @justina-texture
                              :shape-renderer  @shape-renderer
                              :view-port       @view-port})))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))
        ))))