(ns minesweaper.views
  (:require [goog.string :as gstring]
            [goog.string.format]
            [re-frame.core :as rf]
            [reagent.ratom :refer [reaction]]))

(defn title []
  (let [name (rf/subscribe [:name])]
    (fn []
      [:h1 @name])))


(defn cell-flagged
  [[row col]]
  (fn [[row col]]
    [:div {:class           "ms-cell ms-cell-hidden ms-cell-flagged"
           :on-click        #(rf/dispatch [:unhide-cell [row col]])
           :on-context-menu #(rf/dispatch [:unflag-cell [row col]])}]))

(defn cell-hidden
  [[row col]]
  (fn [[row col]]
    [:div {:class           "ms-cell ms-cell-hidden"
           :on-click        #(rf/dispatch [:unhide-cell [row col]])
           :on-context-menu #(rf/dispatch [:flag-cell [row col]])}]))


(defn cell-unhidden
  [[row col]]
  (let [neighbour-value (rf/subscribe [:neighbours-value [row col]])
        neighbour-class (str "ms-cell-neighbour-" @neighbour-value)]
    (fn [[row col]]
      [:div {:class           (str "ms-cell ms-cell-unhidden " neighbour-class)
             :on-context-menu #(rf/dispatch [:flag-cell [row col]])}
       (when (pos? @neighbour-value) @neighbour-value)])))


(defn cell-mine
  []
  [:div {:class "ms-cell ms-cell-unhidden ms-cell-mine"}])


(defn cell
  [[row col]]
  (let [flaged (rf/subscribe [:flag-value [row col]])
        unhidden (rf/subscribe [:mask-value [row col]])
        mine (rf/subscribe [:mine-value [row col]])]
    (fn []
      (if (pos? @unhidden)
        (if (pos? @mine)
          [cell-mine]
          [cell-unhidden [row col]])
        (if (pos? @flaged)
          [cell-flagged [row col]]
          [cell-hidden [row col]])))))


(defn grid []
  (let [rows (rf/subscribe [:grid-rows])
        cols (rf/subscribe [:grid-cols])
        game-state (rf/subscribe [:game-state])
        grid-class (reaction (case @game-state
                               :paused "ms-grid-paused"
                               :game-over "ms-grid-game-over"
                               :victory "ms-grid-victory"
                               :started "ms-grid-started"))]
    (println @game-state)
    (fn []
      [:div {:class @grid-class}
       [:div.col-xs-12
        (doall
          (for [row (range @rows)]
            ^{:key (str "row_" (inc row))}
            [:div.row
             (doall
               (for [col (range @cols)]
                 ^{:key (str "cell_" (* (inc row) (inc col)))}
                 [cell [row col]]))]))]])))


(defn main-page []
  (let [time (rf/subscribe [:time-elapsed])
        best-score (rf/subscribe [:best-score])
        mines-remaining (rf/subscribe [:mines-remaining])
        level-selected (rf/subscribe [:level-selected])
        levels (rf/subscribe [:levels])]
    (fn []
      [:div.container-fluid {:on-context-menu #(.preventDefault %)}
       [:div.row.center-xs
        [:div.col-xs-12
         [title]]]
       [:div.row.center-xs
        [:div.col-xs-2
         (str "time: " (gstring/format "%03d" (or @time 0)))]
        [:div.col-xs-2
         (str "best: " (gstring/format "%03d" (or @best-score 0)))]
        [:div.col-xs-2
         (str "mines: " (gstring/format "%03d" (or @mines-remaining 0)))]]
       [:div.row.center-xs.center-xs {:style {:margin-top "15px"}}
        [:div.col-xs-1
         [:select {:value     (name @level-selected)
                   :on-change #(rf/dispatch [:select-level (-> % .-target .-value (keyword))])}
          (for [level @levels]
            ^{:key (name (first level))}
            [:option {:value (first level)} (name (first level))])]]
        [:div.col-xs-1
         [:button {:on-click #(rf/dispatch [:populate-grid])}
          "reset"]]]
       [:div.row.center-xs {:style {:margin-top "25px"}}
        [grid]]])))

