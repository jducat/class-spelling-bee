(ns spelling-bee.game
  (:require [clojure.string :as s]))

(def game1
  {::letters #{"r" "a" "b" "f" "o" "p" "y"}
   ::middle-letter "r"
   ::rankings [[0 "Beginner"]
               [2 "Good Start"]
               [5 "Moving Up"]
               [7 "Good"]
               [14 "Solid"]
               [23 "Nice"]
               [37 "Great"]
               [47 "Amazing"]
               [65 "Genius"]]
   ::words #{"babyproof" "afar" "affray" "afro" "arbor" "array"
             "arroyo" "barb" "barf" "boar" "boor" "bray" "farro"
             "fora" "foray" "fray" "parry" "poor" "pray" "proof"
             "prop" "pyro" "roar" "roof" "ropy"}})

(def game2
  {::letters #{"c" "a" "j" "k" "o" "p" "t" }
   ::middle-letter "c"
   ::words #{"jackpot" "attack" "cacao" "capo" "coact" "coat" "coca"
             "cock" "cockapoo" "cockatoo" "cocoa" "cook" "cooktop"
             "coop" "coopt" "coot" "jack" "jock" "pack" "pact" "pock"
             "tack" "taco" "tact" "toccata" "tock" "topcoat"}
   ::rankings [[0  "Beginner"]
               [2  "Good Start"]
               [5  "Moving Up "]
               [8  "Good "]
               [15 "Solid "]
               [25 "Nice "]
               [39 "Great "]
               [49 "Amazing "]
               [69 "Genius "]]})


(defn valid-word? [word game]
  (contains? (::words game)
             (s/lower-case word)))

(defn valid-letter? [letter game]
  (contains? (::letters game)
             (s/lower-case letter)))

(defn middle-letter? [letter game]
  (= (::middle-letter game)
     (s/lower-case letter)))

(defn has-wrong-letters? [word game]
  (not (every? #(valid-letter? % game) word)))

(defn missing-middle-letter? [word game]
  (not (some #(middle-letter? % game) word)))

(def pangram-bonus 7)

(defn pangram? [word letters]
  (= (set word) letters))

(defn too-short? [word]
  (< (count word) 4))

(defn score-word [word game]
  (let [count (count word)]
    (condp contains? count
      #{0 1 2 3}    0
      #{4}          1
      (+ count
         (if (pangram? word (::letters game)) pangram-bonus 0)))))

(defn max-score [game]
  (reduce + (map #(score-word % game) (::words game))))

(def ranking-percentiles [0 2 6 9 18 31 50 63 87])

(def ranking-labels ["Beginner" "Good Start" "Moving Up" "Good" "Solid" "Nice" "Great" "Amazing" "Genius"])

(defn rankings [game]
  (let [m (max-score game)]
    (mapv (fn [x l] [(quot (* x m) 100) l])
          ranking-percentiles
          ranking-labels)))

(defn compute-rank [score {rank ::rankings}]
  (->> rank
       (take-while #(<= (first %) score))
       last
       second))

;;;; Test forms.

(score-word "abc" game1)
(score-word "abcd" game1)
(score-word "abcde" game1)
(score-word "abcdefgh" game1)
(score-word "babyproof" game1)

(has-wrong-letters? "Foobar" game1)
(has-wrong-letters? "roarq" game1)

(valid-letter? "q" game1)
(valid-letter? "r" game1)
(middle-letter? "r" game1)
(middle-letter? "q" game1)
(valid-word? "foo" game1)
(valid-word? "roar" game1)
