(ns core
  (:import
    [com.badlogic.gdx Game]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)
    (com.badlogic.gdx.graphics OrthographicCamera Texture)
    (com.badlogic.gdx.graphics.glutils ShapeRenderer)
    (com.badlogic.gdx.utils.viewport FitViewport FillViewport StretchViewport ExtendViewport)))

(defn create-game [create-initial-screen-fn]
  (let [[font batch shape-renderer justina-texture
         :as disposables] (repeatedly 4 (fn [] (atom nil)))
        camera       (atom nil)
        view-ports   (atom nil)
        world-width  1920
        world-height 1080]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn
                           (do
                             (reset! font (BitmapFont.))
                             (reset! batch (SpriteBatch.))
                             (reset! justina-texture (Texture. "justina.jpg"))
                             (reset! shape-renderer (ShapeRenderer.))
                             (reset! view-ports [(let [camera (doto (OrthographicCamera.)
                                                                (.update))
                                                       vp     (FitViewport. world-width world-height camera)]
                                                   (.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
                                                   (.apply vp)
                                                   (.update vp world-width world-height true)
                                                   vp)
                                                 (let [camera (doto (OrthographicCamera.)
                                                                (.update))
                                                       vp     (FillViewport. world-width world-height camera)]
                                                   (.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
                                                   (.apply vp)
                                                   (.update vp world-width world-height true)
                                                   vp)
                                                 (let [camera (doto (OrthographicCamera.)
                                                                (.update))
                                                       vp     (StretchViewport. world-width world-height camera)]
                                                   (.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
                                                   (.apply vp)
                                                   (.update vp world-width world-height true)
                                                   vp)
                                                 (let [camera (doto (OrthographicCamera.)
                                                                (.update))
                                                       vp     (ExtendViewport. world-width world-height camera)]
                                                   (.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
                                                   (.apply vp)
                                                   (.update vp world-width world-height true)
                                                   vp)])
                             {:game            this
                              :font            @font
                              :batch           @batch
                              :world-width     world-width
                              :world-height    world-height
                              :justina-texture @justina-texture
                              :shape-renderer  @shape-renderer
                              :view-port       (first @view-ports)
                              :view-ports      @view-ports})))
        (println "creating game!"))
      (dispose []
        (println "disposing" (count disposables) "things")
        (doseq [d disposables]
          (println "disposing " (type @d))
          (.dispose @d))
        ))))