(ns minesweaper.utils
  (:require [clojure.set]))

(defn grid-empty
  [rows cols]
  (into [] (repeat rows (into [] (repeat cols 0)))))


(defn grid-mines
  [rows cols mines]
  (let [board (grid-empty rows cols)
        mines (take mines (distinct (repeatedly (fn [] [(rand-int rows) (rand-int cols)]))))]
    (reduce (fn [board [row col]]
              (assoc-in board [row col] 1))
            board mines)))


(defn neighbours-coords
  [[row col] grid]
  (let [rows (count grid)
        cols (count (first grid))
        indices [[-1 -1] [0 -1] [1 -1] [-1 0] [1 0] [-1 1] [0 1] [1 1]]]
    (reduce (fn [coords [i j]]
              (let [x (+ row i)
                    y (+ col j)]
                (if (and (< -1 x rows) (< -1 y cols))
                  (conj coords [x y])
                  coords)))
            []
            indices)))


(defn grid-neighbours
  [mines-grid]
  (vec (for [[i row] (map vector (range) mines-grid)]
         (vec (for [[j cell] (map vector (range) row)]
                (->> (neighbours-coords [i j] mines-grid)
                     (map (fn [[i j]] (get-in mines-grid [i j] 0)))
                     (reduce +)))))))


(defn grid-sum
  [grid]
  (reduce + (flatten grid)))


(defn grid-count-val
  [val grid]
  (count (filter #(= val %) (flatten grid))))


(defn grid->coords
  [grid]
  (for [[i row] (map vector (range) grid)
        [j col] (map vector (range) row)]
    [[i j] (get-in grid [i j])]))


(defn sweep-cell
  ([[row col] grid]
   (sweep-cell [row col] grid #{}))
  ([[row col] grid region]
   (let [neighbours (neighbours-coords [row col] grid)
         region (conj region [row col])]
     (if (zero? (get-in grid [row col]))
       (reduce (fn [region [nx ny]]
                 (if (not (contains? region [nx ny]))
                   (clojure.set/union region (sweep-cell [nx ny] grid region))
                   region))
               region
               neighbours)
       region))))
(clojure.set/intersection [1 2 3] [1])

(defn calc-game-state
  [mines mask]
  (let [grid-size (->> mines (flatten) (count))
        mines-coords (->> mines (grid->coords) (filter #(pos? (second %))) (map first))
        mines-count (->> mines (flatten) (filter pos?) (count))
        unhided-count (->> mask (flatten) (filter pos?) (count))
        unhided-coords (->> mask (grid->coords) (filter #(pos? (second %))) (map first))
        unhided-mines (clojure.set/intersection (set mines-coords) (set unhided-coords))]
    (cond
      (not-empty unhided-mines) :game-over
      (= mines-count (- grid-size unhided-count)) :victory
      (pos? unhided-count) :started
      :else :paused)))

(defn get-time
  []
  (let [millis (.getTime (js/Date.))]
    (/ millis 1000)))

(defn calc-elapsed-time
  [time-started]
  (int (- (get-time) time-started)))
