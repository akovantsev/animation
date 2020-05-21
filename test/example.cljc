(ns example
  (:require [akovantsev.animation :as a]))


(let [anim [{:duration 30 :attrs {:cr 10} :easing ::a/linear}
            #{[{:duration 3}
               {:duration 10 :attrs {:cr 70} :easing ::a/cubic}]
              [{:duration 10 :attrs {:cx 300} :easing ::a/cubic}
               {:duration 15 :attrs {:cy 50 :cx 400} :easing ::a/out-back}]}
            {:duration 3}
            {:duration 20 :attrs {:cx 50} :easing ::a/out-back}
            {:duration 3}
            {:duration 10 :attrs {:cr 0 :cy 150} :easing ::a/cubic}]
      len  (a/length anim)
      f    (time (a/make anim))]
  (time (f (rand-int len)))
  (time (mapv #(vector % (f %)) (range (+ 10 len)))))


(extend-protocol a/AnimationStep
  clojure.lang.IPersistentList
  (animation-step-type [_] ::a/parallel))


(let [anim [{:duration 30 :attrs {:cr 10} :easing ::a/linear}
            '([{:duration 3}
               {:duration 10 :attrs {:cr 70} :easing ::a/cubic}]
              [{:duration 10 :attrs {:cx 300} :easing ::a/cubic}
               {:duration 15 :attrs {:cy 50 :cx 400} :easing ::a/out-back}])
            {:duration 3}
            {:duration 20 :attrs {:cx 50} :easing ::a/out-back}
            {:duration 3}
            {:duration 10 :attrs {:cr 0 :cy 150} :easing ::a/cubic}]
      init {:cx 0 :cy 10 :cr 20}
      f    (a/make init anim)]
  (map-indexed
    #(vector %1 (f %2))
    (range (inc (a/length anim)))))
