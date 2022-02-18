(ns spelling-bee.app-server
  (:require
   [spelling-bee.game :as game]
   [spelling-bee.game-generator :as gen]))


(defn handler [req]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str
          ;; Use this if you don't have the dictionaries to run
          ;; the game generator.
          ;; (rand-nth [game/game1 game/game2])
          (gen/new-game))})
