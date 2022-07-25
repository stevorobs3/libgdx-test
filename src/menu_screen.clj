(ns menu-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.utils Align)))

(defn- render [{:keys [world-width
                       world-height
                       ^SpriteBatch batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               state]
  (let [^OrthographicCamera camera (.getCamera view-port)
        display-text               (str "Welcome to TETRIS!\nPress SPACE to start the game")
        ^GlyphLayout layout        (GlyphLayout. font display-text Color/PINK (float 210) Align/center true)]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix batch (.combined camera))
    (.begin batch)
    (.setScale (.getData font) 1.5)
    (.draw font batch layout (float (- (/ world-width 2)
                                       (/ (.width layout) 2)))
           (float (+ (/ world-height 2)
                     (/ (.height layout) 2))))

    (.end batch)
    state))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (println "typed in menu screen" key-code Input$Keys/SPACE)
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  state)

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
                                (swap! state (fn [s] (key-down char context s create-game-screen)))
                                true))))
      (hide []
        (println "hiding menu screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (swap! state (fn [s] (resize context s width height))))
      (pause [])
      (resume [])
      (dispose [])
      )))