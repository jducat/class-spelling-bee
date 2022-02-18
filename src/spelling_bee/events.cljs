(ns spelling-bee.events
  (:require
   [re-frame.core :as rf]
   [spelling-bee.game :as game]
   [cljs.core.async :refer [go <! >! chan timeout]]
   [cljs-http.client :as http]))

(defn maybe-update-in [m k f v]
  ;; Like update-in, but does nothing if V is nil.
  (if v
    (update-in m k f v)
    m))

(defn chop [v]
  ;; Remove last letter in vector of letters v
  (subvec v 0 (max 0 (- (count v) 1))))

;; This implements the game rules.
(defn validate-word [word found-words {::game/keys [letters] :as game}]
  ;; Returns a vector of 3 values:
  ;; - status (if word is good or not)
  ;; - score to add up
  ;; - message to popup
  (cond
    (game/too-short? word)                    [false 0 "Word needs at least 4 letters."]
    (game/has-wrong-letters? word game)       [false 0 "One or more letters are invalid!"]
    (game/missing-middle-letter? word game)   [false 0 "You need the middle letter!"]
    (contains? found-words word)              [false 0 "Already found!"]
    (not (game/valid-word? word game))        [false 0 "No such word."]
    (game/pangram? word letters)              (let [score (game/score-word word game)]
                                                [true  score (str "Pangram! +" score)])
    :else                                     (let [score (game/score-word word game)]
                                                [true score (str (rand-nth ["Great!" "Good!" "Awesome!"])
                                                                 "+" score)])))

(def popup-duration 1000) ; milliseconds.

;;;; Event handlers

(defn add-word-handler [{{:keys [found-words game letters-so-far] :as db} :db} _]
  (let [word (apply str letters-so-far)
        [ok? score message] (validate-word word found-words game)]
    {:dispatch-later [{:ms popup-duration :dispatch [:dismiss-popup]}]
     :dispatch [:show-popup message]
     :db (as-> db db
           (maybe-update-in db [:found-words] conj (and ok? word))
           (update db :score + score)
           (assoc db :level (game/compute-rank (:score db) (:game db))
                  :letters-so-far []))}))

(rf/reg-event-fx :add-word add-word-handler)

;;; New game
(def empty-db
  {:found-words #{}
   :score 0
   :letters-so-far []
   :level "Beginner"
   :redisplay-board 0
   :message ""
   :game nil})

(rf/reg-event-fx :initialise-db
  (fn [_ _]
    {:db empty-db
     :dispatch [:show-popup "Initializing game..."]
     :fetch-new-game nil}))

;;; Generic popup utility.

(rf/reg-event-db :show-popup
  (fn [db [_ text]]
    (assoc db :message text)))

(rf/reg-event-db :dismiss-popup
  (fn [db [_ _]]
    (assoc db :message "")))


;;; Handling letters and keypresses

(rf/reg-event-fx :add-letter
  (fn [{db :db} [_ letter]]
    {:db (-> db
             (assoc :last-clicked letter)
             (maybe-update-in [:letters-so-far] conj (re-find #"[a-zA-Z]" letter)))
     :dispatch-later [{:ms 300 :dispatch [:dismiss-last-click]}]}))

(rf/reg-event-db :delete-letter
  (fn [db [_ _]]
    (update db :letters-so-far chop)))

(rf/reg-event-db
    :dismiss-last-click
    (fn [db _]
      (dissoc db :last-clicked)))

;;; The rankings popup

(rf/reg-event-db
    :display-rankings
    (fn [db _]
      (assoc db :display-rankings true)))

(rf/reg-event-db
    :dismiss-rankings
    (fn [db _]
      (dissoc db :display-rankings)))

;;; Misc.

(rf/reg-event-db
    :shuffle-board
  (fn [db _]
      (update db :redisplay-board inc))) ; forces a redraw, and we shuffle every time.

;;; Ajax

(rf/reg-event-fx :fetch-game-event
  (fn [_ _]
    (println "Getting test-fetch fx")
    {:fetch-new-game nil                ; <== the cofx
     :dispatch [:show-popup "Fetching game..."]}))

(rf/reg-fx
 :fetch-new-game
 (fn [_]
   (go
     (let [r (<! (http/get "http://localhost:9500/api/new-game"))]
       (<! (timeout 1500)) ; simulate a somewhat slow server
       ;; Should look at code to do error handling, etc.
       (rf/dispatch [:new-game (:body r)])))))

(rf/reg-event-db
    :new-game
  (fn [db [_ game]]
    (assoc empty-db :game game)))
