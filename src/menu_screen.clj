(ns menu-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Camera Texture)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)
           (com.badlogic.gdx.utils.viewport Viewport)))

(defn- render [{:keys [delta-time
                       world-width
                       world-height
                       ^ShapeRenderer shape-renderer
                       view-ports
                       ^SpriteBatch batch
                       ^Texture justina-texture] :as _context}
               {:keys [^Viewport view-port] :as state}]
  (let [rotate-speed               100
        zoom-speed                 1

        ^Viewport new-view-port    (cond
                                     (.isKeyPressed Gdx/input Input$Keys/NUM_1) (first view-ports)
                                     (.isKeyPressed Gdx/input Input$Keys/NUM_2) (second view-ports)
                                     (.isKeyPressed Gdx/input Input$Keys/NUM_3) (nth view-ports 2)
                                     (.isKeyPressed Gdx/input Input$Keys/NUM_4) (nth view-ports 3)
                                     :else view-port)
        ^OrthographicCamera camera (.getCamera new-view-port)]
    (println "new-view port" (type new-view-port))
    (.set (.position camera) (/ world-width 2) (/ world-height 2) 0)
    (when (.isKeyPressed Gdx/input Input$Keys/LEFT)
      (.rotate camera (float (* rotate-speed delta-time))))
    (when (.isKeyPressed Gdx/input Input$Keys/RIGHT)
      (.rotate camera (float (- (* rotate-speed delta-time)))))

    (when (.isKeyPressed Gdx/input Input$Keys/UP)
      (set! (.zoom camera) (float (- (.zoom camera)
                                     (* zoom-speed delta-time)))))
    (when (.isKeyPressed Gdx/input Input$Keys/DOWN)
      (set! (.zoom camera) (float (+ (.zoom camera)
                                     (* zoom-speed delta-time)))))
    (.update view-port (.getWidth Gdx/graphics) (.getHeight Gdx/graphics))
    (.set (.position camera) (/ (.viewportWidth camera) 2) (/ (.viewportHeight camera) 2) 0)


    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.update camera)
    (.apply new-view-port)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (.setProjectionMatrix batch (.combined camera))
    (.begin batch)
    (.draw batch justina-texture (float 0) (float 0))
    (.end batch)
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)
    (.rect shape-renderer (/ world-width 2) (/ world-height 2) 100 100)
    (.end shape-renderer)
    (assoc state :view-port new-view-port)))

(defn- resize [_context {:keys [^Viewport view-port] :as state} width height]
  (let [camera (.getCamera view-port)]
    (println "resizing" width height)
    (.update view-port width height)
    (.set (.position camera) (/ (.viewportWidth camera) 2) (/ (.viewportHeight camera) 2) 0)
    state))

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (println "typed in menu screen" key-code Input$Keys/SPACE)
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  true)

(defn create [{:keys [view-ports] :as context} create-game-screen]
  (let [state (atom {:view-port (first view-ports)})]
    (proxy [Screen] []
      (render [delta]
        (swap! state #(render (assoc context :delta-time delta) %)))
      (show []
        (println "showing menu screen")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyDown [char]
                                (key-down char context state create-game-screen)))))
      (hide []
        (println "hiding menu screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (swap! state (fn [s] (resize context s width height))))
      (pause [])
      (resume [])
      (dispose [])
      )))