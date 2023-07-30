(ns tetris.schema
  (:require [schema.core :as s])
  (:import (com.badlogic.gdx.math Vector2)
           (com.badlogic.gdx.graphics Color)))

(s/defschema Grid
  "A grid used for laying out cells"
  {
   :num-rows                  s/Int
   :num-cols                  s/Int
   :rect-size                 s/Int
   :x-offset                  s/Int
   (s/optional-key :y-offset) s/Int
   ;todo: separate out render from layout data
   s/Any                      s/Any
   ;:line-thickness            s/Int
   ;:cell-vertex-pairs         [[Vector2 Vector2]]
   ;:fill-color                Color
   ;:outline-color             Color

   })


