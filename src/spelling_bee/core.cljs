(ns ^:figwheel-hooks spelling-bee.core
  (:require [reagent.dom]
            [re-frame.core :as rf]
            [devtools.core :as devtools]
            [spelling-bee.views :as views]
            [spelling-bee.events]
            [spelling-bee.subs]))


;; -- Debugging aids ----------------------------------------------------------
(devtools/install!)       ;; we love https://github.com/binaryage/cljs-devtools

(enable-console-print!)   ;; so that println writes to `console.log`

(rf/dispatch-sync [:initialise-db])

(defn get-app-element []
  (.getElementById js/document "app"))

(defn mount [el]
  (reagent.dom/render [views/spelling-bee] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (rf/clear-subscription-cache!)
  (mount-app-element))
