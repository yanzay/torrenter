(ns torrenter.core
  (:gen-class)
  (:require [feedparser-clj.core :refer [parse-feed]]
            [clojure.java.io :as io]
            [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn parsed []
  (parse-feed "http://torrentrss.net/getrss.php?rsslink=rPEELP"))

(defn feed []
  (:entries (parsed)))

(defn torrent-links []
  (map :link (feed)))

(defn file-name [resp]
  (str/replace
    (last (str/split (:content-disposition (:headers resp)) #"="))
    #"\"" ""))

(defn makedata [resp]
    (hash-map :name (file-name resp) :stream (:body resp)))

(defn get-files []
  (let [urls (torrent-links)
      futures (doall (map (fn [url] (http/get url {:as :byte-array})) urls))]
  (for [resp futures]
    (makedata @resp)
    )))

(defn save-file [file]
  (let [filepath (str "/home/osmc/watchdir/" (:name file))]
    (with-open [w (io/output-stream filepath)]
      (.write w (:stream file)))))

(defn save-files []
  (let [files (get-files)]
      (doseq [file files]
        (save-file file))))

(defn -main
  [& args]
    (save-files))

