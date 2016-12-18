(ns minesweaper.playground
  (:require [re-frame.core :as rf]
            [minesweaper.utils :as utils]))

@(rf/subscribe [:best-score])
@(rf/subscribe [:grid-flags])
@(rf/subscribe [:neighbours-value [0]])
(get-in @re-frame.db/app-db [:app-state :time-started])

(utils/calc-elapsed-time 1482092122)

(defn empty-board [rows cols]
  (into [] (repeat rows (into [] (repeat cols 0)))))

(defn mines-board [rows cols mines]
  (let [board (empty-board rows cols)
        mines (take mines (distinct (repeatedly (fn [] [(rand-int rows) (rand-int cols)]))))]
    (reduce (fn [board [row col]]
              (assoc-in board [row col] 1))
            board mines)))

(def tmp-mines
  [[0 0 1 0 1 1 0 0 0 0]
   [1 0 0 1 0 0 1 1 1 0]
   [1 1 1 0 1 0 1 1 0 0]
   [1 1 0 1 0 1 0 0 0 0]
   [1 1 0 0 0 1 0 0 1 1]
   [0 1 1 1 0 0 0 0 1 0]
   [0 0 0 1 0 0 0 0 1 0]
   [1 0 0 0 0 0 0 1 0 1]
   [1 0 0 0 0 1 0 1 1 1]
   [0 0 0 0 1 0 1 0 1 0]])

(cond-> []
        true (conj 1)
        true (conj 2))

(defn grid->cords
  [grid]
  (for [[i row] (map vector (range) grid)
        [j col] (map vector (range) row)]
    [[i j] (get-in grid [i j])]))

(grid->cords tmp-mines)


(def empty (empty-board 10 10))

(sort-by (juxt first second) (utils/sweep-cell [0 0] tmp-mines))

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

(defn count-surrounding
  [[row col] grid value]
  (reduce (fn [count cell]
            (if (= value (get-in grid cell))
              (inc count)
              count))
          0
          (neighbours-coords [row col] grid)))


(defn flatten-grid
  [grid]
  (for [[i row] (map-indexed vector grid)
        [j val] (map-indexed vector row)]
    [[i j] val]))

(defn sum-grid
  [grid]
  (->> grid
       (flatten)
       (reduce +)))


(defn sum-grid2
  [grid]
  (reduce (fn [sum [[_ _] n]]
            (+ sum n))
          0
          (flatten-grid grid)))

(defn count-in-grid
  [grid value]
  (reduce (fn [total [_ v]]
            (if (= value v)
              (inc total)
              total))
          0
          (flatten-grid grid)))

(defn neighbours-grid
  [mines-grid]
  (for [[i row] (map vector (range) mines-grid)]
    (for [[j cell] (map vector (range) row)]
      (->> (neighbours-coords [i j] mines-grid)
           (map (fn [[i j]] (get-in mines-grid [i j] 0)))
           (reduce +)))))



(def tmp-grid (mines-board 10 10 10))
(reduce + (flatten tmp-grid))
(sum-grid2 tmp-grid)

(time (dotimes [n 1000] (sum-grid tmp-grid)))
(time (dotimes [n 1000] (sum-grid2 tmp-grid)))
(flatten-grid tmp-grid)



