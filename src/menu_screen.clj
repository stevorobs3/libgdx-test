(ns menu-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.graphics.glutils ShapeRenderer ShapeRenderer$ShapeType)))

(defn- render [{:keys [delta-time
                       world-width
                       world-height
                       ^ShapeRenderer shape-renderer
                       ^OrthographicCamera camera] :as _context} _state]
  (let [rotate-speed 100
        zoom-speed   1
        num-rects 10
        rect-size    (/ world-height num-rects)
        rand-float   (fn [] (float (/ (rand-int Integer/MAX_VALUE) Integer/MAX_VALUE)))]
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


    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.update camera)
    (.setProjectionMatrix shape-renderer (.combined camera))
    (.begin shape-renderer ShapeRenderer$ShapeType/Filled)

    (doseq [i (range num-rects)
            j (range num-rects)]
      (.setColor shape-renderer (rand-float) 0 0 1)
      (.rect shape-renderer
             (* i rect-size)
             (* j rect-size)
             rect-size
             rect-size))

    (.end shape-renderer)))

(defn- resize [_context width height]
  (println "resizing" width height))

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (println "typed in menu screen" key-code Input$Keys/SPACE)
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  true)

(defn create [context create-game-screen]
  (let [state (atom {:r 1
                     :g 1
                     :b 1})]
    (proxy [Screen] []
      (render [delta]
        (render (assoc context :delta-time delta) @state))
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
        (resize context width height)
        )
      (pause [])
      (resume [])
      (dispose [])
      )))