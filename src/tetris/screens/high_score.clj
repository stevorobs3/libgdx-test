(ns tetris.screens.high-score
  (:require [libgdx.screen :as gdx-screen]
            [libgdx.input-adapter :as gdx-input])
  (:import (com.badlogic.gdx Gdx Input$Keys)
           (com.badlogic.gdx.graphics GL20 OrthographicCamera Color)
           (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch GlyphLayout)
           (com.badlogic.gdx.utils.viewport Viewport)
           (com.badlogic.gdx.utils Align)))

(defn- render [{:keys [world-width
                       world-height
                       ^SpriteBatch sprite-batch
                       ^BitmapFont font
                       ^Viewport view-port] :as _context}
               {:keys [score] :as state}]
  (let [{:keys [points level lines-cleared]} score
        ^OrthographicCamera camera (.getCamera view-port)
        display-text               (str "Nice work, you scored " points " points, and got to level "
                                        level " by clearing " lines-cleared " lines.")
        ^GlyphLayout layout        (GlyphLayout. font display-text Color/PINK (float 210) Align/center true)]
    (.glClearColor Gdx/gl 0 0 0 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)
    (.setProjectionMatrix sprite-batch (.combined camera))
    (.begin sprite-batch)
    (.setScale (.getData font) 1.5)
    (.draw font sprite-batch layout (float (- (/ world-width 2)
                                              (/ (.width layout) 2)))
           (float (+ (/ world-height 2)
                     (/ (.height layout) 2))))

    (.end sprite-batch)
    state))

(defn- resize [{:keys [^Viewport view-port] :as _context} state width height]
  (println "resizing" width height)
  (.update view-port width height true)
  state)

(defn- key-down [key-code {:keys [game] :as context} state create-game-screen]
  (println "typed in end game screen" key-code)
  (when (= key-code Input$Keys/ESCAPE)
    (.setScreen game (create-game-screen context)))
  state)

(defn create [context score create-game-screen]
  (let [state         (atom {:score score})
        input-adapter (gdx-input/create
                        {:key-down (fn [key-code]
                                     (swap! state #(key-down key-code context % create-game-screen))
                                     true)})]
    (gdx-screen/create
      input-adapter
      {:render  (fn [delta]
                  (swap! state #(render (assoc context :delta-time delta) %)))
       :resize  (fn [width height]
                  (swap! state #(resize context % width height)))
       :dispose (fn [])})))