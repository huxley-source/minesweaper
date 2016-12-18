(ns minesweaper.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [minesweaper.utils :as utils]))

(rf/reg-sub
  :name
  (fn [db _]
    (:name db)))


(rf/reg-sub
  :time-elapsed
  (fn [db _]
    (get-in db [:app-state :time-elapsed])))


(rf/reg-sub
  :best-score
  (fn [db _]
    (let [level (get-in db [:app-state :level-selected])]
      (get-in db [:levels level :best]))))


(rf/reg-sub
  :grid-rows
  (fn [db _]
    (get-in db [:grid :rows])))


(rf/reg-sub
  :grid-cols
  (fn [db _]
    (get-in db [:grid :cols])))


(rf/reg-sub
  :mines-total
  (fn [db _]
    (get-in db [:app-state :mines-total])))


(rf/reg-sub
  :grid-mines
  (fn [db _]
    (get-in db [:grid :mines])))

(rf/reg-sub
  :grid-neighbours
  (fn [db _]
    (get-in db [:grid :neighbours])))

(rf/reg-sub
  :grid-flags
  (fn [db _]
    (get-in db [:grid :flags])))

(rf/reg-sub
  :flags-count
  (fn [db _]
    (utils/grid-sum (get :grid-flags db))))

(rf/reg-sub
  :grid-mask
  (fn [db _]
    (get-in db [:grid :mask])))

(rf/reg-sub
  :mine-value
  (fn [db [_ [row col]]]
    (get-in db [:grid :mines row col])))

(rf/reg-sub
  :neighbours-value
  (fn [db [_ [row col]]]
    (get-in db [:grid :neighbours row col])))

(rf/reg-sub
  :flag-value
  (fn [db [_ [row col]]]
    (get-in db [:grid :flags row col])))

(rf/reg-sub
  :mask-value
  (fn [db [_ [row col]]]
    (get-in db [:grid :mask row col])))

(rf/reg-sub
  :mines-remaining
  (fn [db _]
    (let [total (get-in db [:app-state :mines-total])
          flags (->> (get-in db [:grid :flags])
                     (utils/grid->coords)
                     (map second)
                     (reduce +))]
      (- total flags))))


(rf/reg-sub
  :levels
  (fn [db _]
    (get db :levels)))


(rf/reg-sub
  :level-selected
  (fn [db _]
    (get-in db [:app-state :level-selected])))


(rf/reg-sub
  :game-state
  (fn [db _]
    (get-in db [:app-state :game-state])))
