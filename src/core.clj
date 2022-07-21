(ns core
  (:import
    [com.badlogic.gdx Game]
    (com.badlogic.gdx.graphics.g2d BitmapFont SpriteBatch)))

(defn create-game [create-initial-screen-fn]
  (let [font  (atom nil)
        batch (atom nil)]
    (proxy [Game] []
      (create []
        (.setScreen this (create-initial-screen-fn {:game  this
                                                    :font  (reset! font (BitmapFont.))
                                                    :batch (reset! batch (SpriteBatch.))}))
        (prn "creating game!"))
      (dispose []
        (prn "disposing")
        (when-let [b @batch] (.dispose b))
        (when-let [f @font] (.dispose f))))))