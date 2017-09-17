(ns nb-statistics.core
  (:require [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def ^:private retail-available-colors
  [#"회색" #"하늘" #"검정" #"핑크" #"백색" #"혼합"])

(defn greater-than-and-less-than [min max num]
  (and num (> num min) (> max num)))

(defn remove-leading-0s [num]
  (loop [n num]
    (if (= (first n) \0)
      (recur (apply str (rest n)))
      n)))

(defn- read-files-as-row-vector [file-name]
  (vec (csv/read-csv (slurp file-name))))

(defn- separate-first-row-and-contents-rows [rows]
  {:first-row (first rows) :contents-rows (rest rows)})

(defn- extract-column-from-rows [col-index rows]
  (map (fn [row] (row col-index)) rows))

(defn- extract-size-and-color-pairs [options]
  (map
    (fn [option]
      (let [size (some->> option
                          (re-seq #"\d+")
                          (map remove-leading-0s)
                          (map clojure.edn/read-string)
                          (filter (fn [num] (greater-than-and-less-than 60 130 num)))
                          first)
            color (when option
                    (or
                      (some (fn [color]
                              (re-find color option))
                            retail-available-colors)
                      "빈칸"))]
        [size color]))
    options))

(defn- add-size-and-color-columns-to-row [row]
  (concat row ["사이즈" "색상"]))

(defn- add-columns-to-rows [original-rows new-columns]
  (map (fn [original-row
            new-cols]
         (concat original-row
                 new-cols))
       original-rows new-columns))

(defn- write-results-to-csv-file [filename result]
  (with-open [writer (io/writer filename)]
    (csv/write-csv writer
                   result)))

(defn run-retail-mode!
  [file-name]
  (let [{:keys [first-row contents-rows]} (separate-first-row-and-contents-rows
                                            (read-files-as-row-vector file-name))
        options (extract-column-from-rows 4 contents-rows)
        size-color-pairs (extract-size-and-color-pairs options)
        first-row-with-size-and-color (add-size-and-color-columns-to-row first-row)
        contents-rows-with-size-and-color (add-columns-to-rows contents-rows size-color-pairs)]
    (write-results-to-csv-file "retail-output.csv"
                               (cons first-row-with-size-and-color
                                     contents-rows-with-size-and-color))))

(defn run-wholesale-mode! [filename]
  (println (str filename " not implemented")))

(defn -main
  [& args]
  (let [program-mode (first args)
        file-name (second args)]
    (case program-mode
      ("wholesale" "wh") (run-wholesale-mode! file-name)
      ("retail" "re") (run-retail-mode! file-name)
      (println (str program-mode " is not supported." \newline
                    "Try, 'wholesale', 'wh' for wholesale mode," \newline
                    "or 'retail', 're' for retail mode.")))))
