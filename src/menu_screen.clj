(ns menu-screen
  (:import (com.badlogic.gdx Screen Gdx InputAdapter Input$Keys)
           (com.badlogic.gdx.graphics GL20)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)))

(defn- render [{:keys [^SpriteBatch batch
                       ^BitmapFont font] :as context} state]
  (let [text         "Menu Screen"
        font-data    (.getData font)
        glyph-layout (GlyphLayout.)]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.begin batch)
    (.setColor font 1 1 0 1)
    (.setScale font-data 4)
    (.setText glyph-layout font text)

    (.draw font batch glyph-layout
           (float (- (/ (.getWidth Gdx/graphics) 2)
                     (/ (.width glyph-layout) 2)))
           (float (+ (/ (.getHeight Gdx/graphics) 2)
                     (/ (.height glyph-layout) 2))) )
    (.end batch)))

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (prn "typed in menu screen" key-code Input$Keys/SPACE)
  (when (= key-code Input$Keys/SPACE)
    (.setScreen game (create-game-screen context)))
  true)

(defn create [context create-game-screen]
  (let [state (atom {:r 1
                     :g 1
                     :b 1})]
    (proxy [Screen] []
      (render [delta]
        (render context @state))
      (show []
        (prn "showing menu screen")
        (.setInputProcessor Gdx/input
                            (proxy [InputAdapter] []
                              (keyDown [char]
                                (key-down char context state create-game-screen)))))
      (hide []
        (prn "hiding menu screen")
        (.setInputProcessor Gdx/input nil))
      (resize [width height]
        (prn "resizing" width height)
        )
      (pause [])
      (resume [])
      (dispose [])
      )))