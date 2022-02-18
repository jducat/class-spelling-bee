(ns spelling-bee.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :refer [subscribe dispatch]]
   [clojure.string :as str]
   [spelling-bee.game :as game]))


(defn letters [so-far game-letters middle-letter]
  (let [class #(cond
                 (= middle-letter %)        :middle-letter
                 (contains? game-letters %) :valid-letter
                 :else                      :invalid-letter)]
    [:div
     (for [[l k] (map vector so-far (range))]
       ^{:key k} [:span.input {:class (class l)} (str/upper-case l)])
     [:span.input.cursor "|"]]))

(defn word-input [game-letters middle-letter]
  (let [letters-so-far @(subscribe [:letters-so-far])]
   [:div.word-input>span#word-input
    [letters letters-so-far game-letters middle-letter]]))


(defn message-box []
  (let [text @(subscribe [:message])]
    (if (= text "")
      [:div]
      [:div.message.animated.fade-out text])))

(defn board-letters [letters extra-class]
  (let [last-clicked @(subscribe [:last-clicked])]
    [:div.align-center
     (for [l letters]
       ^{:key l} [:div.letter {:class [extra-class (when (= last-clicked l) "clicked")]
                               :on-click #(dispatch [:add-letter l])}
                  (str/upper-case (or l ""))])]))

(defn display-letters [letters middle]
  (let [_ @(subscribe [:redisplay-board])
        _ @(subscribe [:game])
        ;; The shuffle will take care of shuffling on
        ;; any redisplay on shuffle click.
        [top bottom] (partition 3 (shuffle (remove #{middle} letters)))]
    [:div.letters.padded
     [board-letters top]
     [board-letters [middle] " middle"]
     [board-letters bottom]]) )

(defn display-buttons []
  [:div#buttons
   [:button {:on-click #(dispatch [:delete-letter])} "delete"]
   [:button {:on-click #(dispatch [:shuffle-board])} "shuffle"]
   [:button {:on-click #(dispatch [:add-word])} "enter"]])

(defn board []
  (let [{::game/keys [letters middle-letter]} @(subscribe [:game])]
    [:div#board.padded
     [word-input letters middle-letter]
     [message-box]
     [display-letters letters middle-letter]
     [display-buttons]]))

(defn format-todays-score [rankings]
  [:div
   [:h2 "Rankings"]
   [:p "Ranks are based on a percentage of points."]
   [:p "Today's minimum scores are:"]
   [:ul
    (for [[score level] rankings]
      ^{:key level} [:li (str level " (" score ")")])]])

(defn score-bar [rank score]
  [:div
   [:h4 {:on-click #(dispatch [:display-rankings])}
    (str rank ": " score (if (= 1 score)
                           " point."
                           " points."))]
   [:input#score-bar
    {:type :range
     :min 0
     :read-only true
     :value score
     :max (game/max-score @(subscribe [:game]))}]])

(defn report-panel []
  (let [words @(subscribe [:found-words])
        count (count words)]
    [:div#report-panel.padded
     [score-bar @(subscribe [:level]) @(subscribe [:score])]
     [:h3 "You have found " count
      (if (= 1 count) " word." " words.")]
     [:ul
      (for [w (sort words)]
        ^{:key w} [:li (str/capitalize w)])]
     ;; on-click would keep the focus, :on-mouse-up doesn't.
     [:button {:on-mouse-up #(dispatch [:fetch-game-event])} "new game"]]))

(defn keyboard-handler [event]
  (case (.-keyCode event)
    8    (dispatch [:delete-letter])    ; backspace
    27   (dispatch [:delete-letter])    ; rubout
    13   (dispatch [:add-word])         ; Enter
    (dispatch [:add-letter (.-key event)]))) ; Default; assume it's OK.

(defn show-rankings [rankings visible?]
  (let [dismiss-fn  #(dispatch [:dismiss-rankings])]
    (reagent/with-let [_ (.addEventListener js/document "mousedown" dismiss-fn)]
      (when visible?
        [:div.overlay>center>div.rankings
         [format-todays-score rankings]])
      (finally
        (.removeEventListener js/document "mousedown" dismiss-fn)))))

(defn spelling-bee []
  (reagent/with-let [_ (.addEventListener js/document "keyup" keyboard-handler)]
    [:span
     [show-rankings (::game/rankings @(subscribe [:game])) @(subscribe [:display-rankings])]
     [:center>h1 "Welcome to the spelling bee!"]
     [board]
     [report-panel]]
    (finally
      (.removeEventListener js/document "keyup" keyboard-handler))))
