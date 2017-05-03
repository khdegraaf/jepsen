(ns jepsen.galera
  "Tests for Mariadb Galera Cluster"
  (:gen-class)
  (:require [clojure.tools.logging :refer :all]
            [clojure.core.reducers :as r]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]]
            [knossos.op :as op]
            [jepsen [client :as client]
             [core :as jepsen]
             [db :as db]
             [cli        :as cli]
             [tests :as tests]
             [control :as c :refer [|]]
             [checker :as checker]
             [nemesis :as nemesis]
             [generator :as gen]
             [util :refer [timeout meh]]]
            [jepsen.control.util :as cu]
            [jepsen.control.net :as cn]
            [jepsen.os.debian :as debian]
            [clojure.java.jdbc :as j]
            [honeysql [core :as sql]
                      [helpers :as h]]))

(def log-files
  ["/var/log/syslog"
   "/var/log/mysql.log"
   "/var/log/mysql.err"
   "/var/lib/mysql/queries.log"])

(def dir "/var/lib/mysql")
(def stock-dir "/var/lib/mysql-stock")

(defn install!
  "Downloads and installs the galera packages."
  [node version]
  (debian/add-repo!
    :galera
    "deb http://sfo1.mirrors.digitalocean.com/mariadb/repo/10.0/debian jessie main"
    "keyserver.ubuntu.com"
    "0xcbcb082a1bb943db")

  (c/su
    (c/exec :echo "mariadb-galera-server-10.0 mysql-server/root_password password jepsen" | :debconf-set-selections)
    (c/exec :echo "mariadb-galera-server-10.0 mysql-server/root_password_again password jepsen" | :debconf-set-selections)
    (c/exec :echo "mariadb-galera-server-10.0 mysql-server-5.1/start_on_boot boolean false" | :debconf-set-selections)

    (debian/install [:rsync])

    (when-not (debian/installed? :mariadb-galera-server)
      (info node "Installing galera")
      (debian/install [:mariadb-galera-server])

      (c/exec :service :mysql :stop)
      ; Squirrel away a copy of the data files
      (c/exec :rm :-rf stock-dir)
      (c/exec :cp :-rp dir stock-dir))))

(defn cluster-address
  "Connection string for a test."
  [test]
  (str "gcomm://" (str/join "," (map name (:nodes test)))))

(defn configure!
  "Sets up config files"
  [test node]
  (c/su
    ; my.cnf
    (c/exec :echo (-> (io/resource "jepsen.cnf")
                      slurp
                      (str/replace #"%CLUSTER_ADDRESS%"
                                   (cluster-address test)))
            :> "/etc/mysql/conf.d/jepsen.cnf")))

(defn stop!
  "Stops sql daemon."
  [node]
  (info node "stopping mysqld")
  (meh (cu/grepkill! "mysqld")))

(defn eval!
  "Evals a mysql string from the command line."
  [s]
  (c/exec :mysql :-u "root" "--password=jepsen" :-e s))

(defn setup-db!
  "Adds a jepsen database to the cluster."
  [node]
  (eval! "create database if not exists jepsen;")
  (eval! (str "GRANT ALL PRIVILEGES ON jepsen.* "
              "TO 'jepsen'@'%' IDENTIFIED BY 'jepsen';")))

(defn db
  "Sets up and tears down Galera."
  [version]
  (reify db/DB
    (setup! [_ test node]
      (install! node version)
      (configure! test node)

      (when (= node (jepsen/primary test))
        (c/su (c/exec :service :mysql :start :--wsrep-new-cluster)))

      (jepsen/synchronize test)
      (when (not= node (jepsen/primary test))
        (c/su (c/exec :service :mysql :start)))

      (jepsen/synchronize test)
      (setup-db! node)

      (info node "Install complete")
      (Thread/sleep 5000))

    (teardown! [_ test node]
      (c/su
        (stop! node)
        (apply c/exec :truncate :-c :--size 0 log-files))
        (c/exec :rm :-rf dir)
        (c/exec :cp :-rp stock-dir dir))

    db/LogFiles
    (log-files [_ test node] log-files)))
