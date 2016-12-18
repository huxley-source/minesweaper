(ns minesweaper.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [minesweaper.events]
              [minesweaper.subs]
              [minesweaper.utils :as utils]
              [minesweaper.views :as views]
              [minesweaper.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (reagent/render [views/main-page]
                  (.getElementById js/document "app")))


(defonce timer
         (js/setInterval #(re-frame/dispatch [:update-time]) 1000))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])

  (dev-setup)
  (mount-root))
