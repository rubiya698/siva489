(ns simple.dev.start-jetty-server
  (:require simple.core
            [lein-reload.util.tracker :as tracker]
            [clojure.java.io :as io]
            [robert.hooke :as hooke])
  (:use ring.adapter.jetty
        ring.middleware.reload-modified
        ring.middleware.stacktrace
        clojure.test))

(defonce server (run-jetty (wrap-stacktrace (wrap-reload-modified #'simple.core/app ["src/main/clojure"])) {:port 8080 :join? false}))

(defn stop []
  (.stop server))

(defn start []
  (.start server))

(defonce changed-ns (tracker/tracker [(io/file "src/test/clojure") (io/file "src/main/clojure")] (System/currentTimeMillis)))

(defn reload-tests [f & args]
  (doseq [ns (changed-ns)] (require ns :reload))
  (apply f args))

(hooke/add-hook #'clojure.test/run-tests #'reload-tests)
(hooke/add-hook #'clojure.test/run-all-tests #'reload-tests)