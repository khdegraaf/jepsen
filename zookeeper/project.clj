(defproject jepsen.zookeeper "0.1.0-SNAPSHOT"
  :description "A Jepsen test for Zookeeper"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repl-options {:init-ns jepsen.zookeeper}
  :main jepsen.zookeeper
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojars.khdegraaf/jepsen "0.1.5.2-SNAPSHOT"]
                 [avout "0.5.4"]])
