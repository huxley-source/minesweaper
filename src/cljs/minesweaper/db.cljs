(ns minesweaper.db)

(def default-db
  {:name      "minesweaper"
   :levels    {:beginner     {:rows  8
                              :cols  8
                              :mines 10
                              :best nil}
               :intermediate {:rows  16
                              :cols  16
                              :mines 40
                              :best  nil}
               :expert       {:rows  16
                              :cols  30
                              :mines 99
                              :best  nil}}
   :app-state {:game-state     :paused
               :level-selected :intermediate
               :time-started   0
               :time-elapsed   0
               :mines-total    0
               :flags          0}
   :grid      {:rows       16
               :cols       16
               :mines      []
               :neighbours []
               :flags      []
               :mask       []}
   })
