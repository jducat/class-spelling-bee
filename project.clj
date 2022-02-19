(defproject spelling-bee "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "https://class-spell-bee.herokuapp.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.764"]
                 [cljs-http "0.1.46"]
                 [reagent "0.10.0"]
                 [re-frame "0.12.0"]
                 [environ "1.1.0"]]

  :source-paths ["src" "test"]

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "spelling-bee.test-runner"]}

  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "class-spelling-bee.jar"
  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.5"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]}
             :production {:env {:production true}}})
