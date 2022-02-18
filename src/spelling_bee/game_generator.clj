(ns spelling-bee.game-generator
  (:require
   [clojure.set :as set]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [spelling-bee.game :as g]))


(declare good-candidate-word)

(def wordlist "/usr/share/dict/words")

(def words (delay (line-seq (io/reader wordlist))))

(def candidates (delay (->> @words
                            (filter good-candidate-word))))

(defn alphabetical [w]
  ;; Using only lower case letters throws out
  ;; the proper names.
  (re-find #"^[a-z]+$" w))

(defn has-n-letters [n word]
  (= n (count (set word))))

(defn doesnt-include [letters word]
  (empty? (set/intersection letters (set word))))

(def frequent-letters #{\s \e})

(def good-candidate-word
  (every-pred alphabetical
              (partial has-n-letters 7)
              (partial doesnt-include frequent-letters)))

(defn test-candidate [candidate central]
  (let [candidate-set (set candidate)
        central-s (str central)
        long-enough?              (fn [word] (>= (count word) 4))
        same-letters-as-candidate (fn [word] (set/subset? (set word) candidate-set))
        has-middle-letter         (fn [word] (s/includes? word central-s))]
    (every-pred long-enough?
                has-middle-letter
                same-letters-as-candidate)))

(defn all-words-matching [candidate central words]
  (->> words
       (filter (test-candidate candidate central))))

;;; A good puzzle is one whereby there are between 25 and 40 words
;;; that could be found.

(defn good-puzzle? [words [candidate central]]
  (let [hits (all-words-matching candidate central words)]
    (if (<= 25 (count hits) 40)
      [candidate central (count hits) hits]
      nil)))

(defn puzzle-args [candidate]
  (map #(vector candidate %) (set candidate)))

(defn try-candidate [words candidate]
  (map (partial good-puzzle? words)
        (puzzle-args candidate)))

(defn search [candidates words]
  (->> (shuffle candidates)
       (keep (partial try-candidate words)) ; Each returns a subseq of all attempts for 1 candidate
       (apply concat)                       ; so we need to re-concatenate them
       (keep identity)
       (take 1)))

(defn new-game []
  (let [[target middle num-words words] (first (search @candidates @words))
        game {::g/letters (set target)
              ::g/middle-letter middle
              ::g/words (set words)}]
    (-> game
        (assoc ::g/rankings (g/rankings game)))))
