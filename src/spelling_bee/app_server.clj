(ns spelling-bee.app-server
  (:require
   [spelling-bee.game :as game]
   [spelling-bee.game-generator :as gen]
   [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
   [compojure.handler :refer [site]]
   [compojure.route :as route]
   [clojure.java.io :as io]
   [ring.adapter.jetty :as jetty]
   [environ.core :refer [env]]))


#_(defn handler [req]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str
          ;; Use this if you don't have the dictionaries to run
          ;; the game generator.
          ;; (rand-nth [game/game1 game/game2])
          (gen/new-game))})

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str
          ;; Use this if you don't have the dictionaries to run
          ;; the game generator.
          ;; (rand-nth [game/game1 game/game2])
          (gen/new-game))})

(defroutes app
  (GET "/" []
    (handler))
  (ANY "*" []
    (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))