(ns akovantsev.animation
  (:require [clojure.set :as set]))


;(def interpolate nil)
(defmulti interpolate (fn [easing vfr t vto] easing)) ;; (<= 0 t 1)
(defmethod interpolate ::cubic [_ vfr t vto]
  (let [cube (* (- 1 t) (- 1 t) (- 1 t))]
    (+ (* vfr cube)
       (* vto (- 1 cube)))))

(defmethod interpolate ::linear [_ vfr t vto]
  (+ (* vfr (- 1 t))
     (* vto t)))

(defmethod interpolate ::out-back [_ vfr t vto]
  (let [x (+ 1
            (* 1.70158 (* (- t 1) (- t 1)))
            (* 2.70158 (* (- t 1) (- t 1) (- t 1))))]
    (+ (* vfr (- 1 x))
       (* vto x))))


(declare calculate)

(defn -calculate-sequential [start init t steps]
  (reduce
    (fn rf [[start init] step]
      (let [[rt attrs :as res] (calculate start init t step)]
        (if (= rt t)
          (reduced res)
          res)))
    [start init]
    steps))


(defn -dissoc-unchanged [before after]
  (into {} (set/difference (set after) (set before))))

(defn -calculate-parallel [start init t steps]
  (let [results (map #(calculate start init t %) steps)
        t'      (->> results (map first) (reduce max 0))
        attrs   (->> results
                  (map #(-dissoc-unchanged init (second %)))
                  (reduce into init))]
    [t' attrs]))


(defn -calculate-single [start init t step]
  (let [{:keys [duration easing attrs]} step
        end     (+ start duration)]
    (cond
      (<= t start)    [t init]
      (<= end t)      [end (into init attrs)]
      (< start t end) (if (empty? attrs)
                        [t init]
                        (let [tnorm  (-> t (- start) (/ duration))
                              easing (or easing ::linear)
                              rf     (fn rf [m k vto]
                                       (let [vfr (get m k)
                                             v   (interpolate easing vfr tnorm vto)]
                                         (assoc m k v)))
                              attrs' (reduce-kv rf init attrs)]
                          [t attrs'])))))

(defprotocol AnimationStep
  (animation-step-type [_]))

(extend-protocol AnimationStep
  clojure.lang.IPersistentMap
  (animation-step-type [_] ::single)
  clojure.lang.IPersistentSet
  (animation-step-type [_] ::parallel)
  clojure.lang.IPersistentVector
  (animation-step-type [_] ::sequential))


(defn calculate [start init t step]
  (case (animation-step-type step)
    ::sequential (-calculate-sequential start init t step)
    ::parallel   (-calculate-parallel start init t step)
    ::single     (-calculate-single start init t step)))


(defn length [anim]
  (case (animation-step-type anim)
    ::single     (:duration anim 0)
    ::parallel   (->> anim (map length) (reduce max 0))
    ::sequential (->> anim (map length) (reduce + 0))))


(defn used-attributes [anim]
  (case (animation-step-type anim)
    (::parallel ::sequential)
    (->> anim (mapcat used-attributes) (into #{}))
    (::single)
    (->> anim :attrs keys)))


(defn -make-defaults [anim]
  (zipmap (used-attributes anim) (repeat 0)))

(defn make
  ([anim]
   (make (-make-defaults anim) anim))
  ([init anim]
   (fn [t]
     (let [[_tr attrs] (calculate 0 init t anim)]
       attrs))))
