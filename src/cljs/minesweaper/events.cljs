(ns minesweaper.events
  (:require [re-frame.core :as re-frame]
            [minesweaper.db :as db]
            [minesweaper.utils :as utils]))


(re-frame/reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))


(re-frame/reg-event-fx
  :select-level
  (fn [{:keys [db]} [_ level]]
    (let [{:keys [rows cols mines] :as m} (get-in db [:levels level])]
      {:db         (assoc-in db [:app-state :level-selected] level)
       :dispatch-n [[:set-grid-rows rows]
                    [:set-grid-cols cols]
                    [:set-mines mines]
                    [:populate-grid]]})))


(re-frame/reg-event-db
  :set-grid-rows
  (fn [db [_ rows]]
    (assoc-in db [:grid :rows] rows)))


(re-frame/reg-event-db
  :set-grid-cols
  (fn [db [_ cols]]
    (assoc-in db [:grid :cols] cols)))

(re-frame/reg-event-db
  :set-mines
  (fn [db [_ mines]]
    (assoc-in db [:app-state :mines-total] mines)))


(re-frame/reg-event-fx
  :populate-grid
  (fn [{:keys [db]} _]
    (let [rows (get-in db [:grid :rows])
          cols (get-in db [:grid :cols])
          mines (get-in db [:app-state :mines-total])
          grid (utils/grid-mines rows cols mines)
          neighbours (utils/grid-neighbours grid)
          empty (utils/grid-empty rows cols)]
      {:db       (-> db
                     (assoc-in [:grid :mines] grid)
                     (assoc-in [:grid :neighbours] neighbours)
                     (assoc-in [:grid :flags] empty)
                     (assoc-in [:grid :mask] empty))
       :dispatch [:pause-game]})))

(re-frame/reg-event-db
  :flag-cell
  (fn [db [_ [row col]]]
    (assoc-in db [:grid :flags row col] 1)))

(re-frame/reg-event-db
  :unflag-cell
  (fn [db [_ [row col]]]
    (assoc-in db [:grid :flags row col] 0)))


(re-frame/reg-event-fx
  :unhide-cell
  (fn [{:keys [db]} [_ [row col]]]
    (let [grid (get-in db [:grid :neighbours])
          cells (utils/sweep-cell [row col] grid)
          mask (get-in db [:grid :mask])]
      {:db       (assoc-in db [:grid :mask]
                           (reduce (fn [m [row col]]
                                     (assoc-in m [row col] 1))
                                   mask cells))
       :dispatch [:game-state]})))


(re-frame/reg-event-fx
  :game-state
  (fn [{:keys [db]} _]
    (let [mines (get-in db [:grid :mines])
          mask (get-in db [:grid :mask])
          state (utils/calc-game-state mines mask)]
      {:db       db
       :dispatch (case state
                   :game-over [:game-over]
                   :victory [:victory]
                   :started [:start-game]
                   :paused [:pause-game])})))


(re-frame/reg-event-db
  :start-game
  (fn [db _]
    (if (not= :started (get-in db [:app-state :game-state]))
      (-> db
          (assoc-in [:app-state :game-state] :started)
          (assoc-in [:app-state :time-started] (utils/get-time)))
      db)))


(re-frame/reg-event-db
  :pause-game
  (fn [db _]
    (-> db
        (assoc-in [:app-state :game-state] :paused)
        (assoc-in [:app-state :time-started] 0)
        (assoc-in [:app-state :time-elapsed] 0))))


(re-frame/reg-event-db
  :victory
  (fn [db _]
    (let [level (get-in db [:app-state :level-selected])
          best (get-in db [:levels level :best])
          time-elapsed (get-in db [:app-state :time-elapsed])
          new-best-score (if best (min best time-elapsed) time-elapsed)]
      (println :victory best time-elapsed new-best-score)
      (-> db
          (assoc-in [:app-state :game-state] :victory)
          (assoc-in [] new-best-score)))))


(re-frame/reg-event-db
  :update-time
  (fn [db _]
    (let [game-state (get-in db [:app-state :game-state])
          time-started (get-in db [:app-state :time-started])]
      (println :update-time game-state time-started)
      (if (and (= :started game-state) (pos? time-started))
        (do (println :update-time1 game-state time-started)
            (assoc-in db [:app-state :time-elapsed] (utils/calc-elapsed-time time-started)))
        db))))


(re-frame/reg-event-fx
  :game-over
  (fn [{:keys [db]} _]
    (let [mines (get-in db [:grid :mines])
          cords (->> mines
                     (utils/grid->coords)
                     (filter #(pos? (second %)))
                     (map first))
          mask (get-in db [:grid :mask])]
      {:db (-> db
               (assoc-in [:grid :mask]
                         (reduce (fn [m [row col]]
                                   (assoc-in m [row col] 1))
                                 mask cords))
               (assoc-in [:app-state :game-state] :game-over))})))