(ns spelling-bee.subs
  (:require [re-frame.core :as rf ]))

(def subscription-keys
  [:found-words :score :letters-so-far :level
   :redisplay-board :last-clicked :game :message :display-rankings])

(defonce subscriptions
  (doall
   (map (fn [key]
          (rf/reg-sub
           key
           (fn [db _]
             (key db))))
        subscription-keys)))
