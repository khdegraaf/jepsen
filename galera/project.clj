(defproject jepsen.galera "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main jepsen.galera.test
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [jepsen "0.1.5"]
                 [honeysql "0.6.1"]
                 [org.clojure/java.jdbc "0.4.1"]
                 [org.mariadb.jdbc/mariadb-java-client "1.2.0"]])
