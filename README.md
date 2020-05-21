### About

Calculate attribute values based on animation steps and point in time. <br>
Like https://bollu.github.io/mathemagic/declarative/index.html but with data api.


### Usage

```clojure
(ns the.next.big.thing
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
      init {:cx 0 :cy 10 :cr 20}
      f    (a/make init anim)]
  (map-indexed
    #(vector %1 (f %2))
    (range (inc (a/length anim)))))

;=>
[,,,
 [33 {:cx 1971/10, :cy 10, :cr 10}]
 [34 {:cx 1176/5, :cy 10, :cr 1313/50}]
 ,,,]
```


### Misc

- `Animation` is a sequence or (vector/list/seq) of `steps` or a single step.
- Step is a map with: 
    - number `:duration`. Probably, don't use negative ones.
    - `:easing` keyword (`::a/linear` is default, `::a/out-back`, `::a/cubic`, or whichever you implement yourself as defmethod)
        ```clojure
        (defmethod a/interpolate ::my-instant-easing [_ value-from t value-to]
          value-to)
        ```
    - and `:attrs` map, which is a mapping of animated attributes to their values at the end of the step.
- Duration is unitless, whatever you decide it to be.
- `Delays` are just steps without `:attrs` key:
    ```clojure
    {:duration 100}
    ```
- Steps can be parallel (wrap them in set `#{}`).
- Steps can be composed arbitrarily, those are just vectors, sets and maps:
    ```clojure
    #{{:duration 100 ::attrs {:cy 15 :cr 100}}
      [{:duration 40 ::attrs {:cx 15} :easing ::a/cubic}
       {:duration 70 ::attrs {:cx 15} :easing :my.custom/cubic-easing}]}
    ```
- Durations of the parallel steps – is the duration of the longest one.
- Parallel steps can animate the same property, which one wins is undefined (sets, eh.). Probably don't do this. <br>
But if you need it to be deterministic, do something like this, and then use `()` instead of `#{}`:
    ```clojure
    (extend-protocol a/AnimationStep
      clojure.lang.IPersistentList
      (animation-step-type [_] ::a/parallel))
    ```
- Read the source, it is short.
- For non-numerical attribute values (colors?) extrapolation – supply custom easing multimethod implementation. 
- Helpful list of easings: https://easings.net/