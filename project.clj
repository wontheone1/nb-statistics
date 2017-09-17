(defproject nb_statistics "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :main nb-statistics.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.4"]]
  :profiles {:dev     {:repl-options {:init-ns user}
                       :dependencies [[midje "1.8.3"]]
                       :plugins      [[lein-midje "3.2.1"]]}
             :uberjar {:aot :all}})
