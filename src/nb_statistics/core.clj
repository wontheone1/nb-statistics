(ns nb-statistics.core
  (:require [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def ^:private retail-available-colors
  [#"회색" #"하늘" #"검정" #"핑크" #"백색" #"혼합"])

(def ^:private whole-sale-available-colors
  [#"OR" #"PK" #"DE" #"NY" #"BU" #"GY" #"AS"])

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

(defn- extract-size-from-string [string]
  (some->> string
           (re-seq #"\d+")
           (map remove-leading-0s)
           (map clojure.edn/read-string)
           (filter (fn [num] (greater-than-and-less-than 60 130 num)))
           last))

(defn- extract-color-from-string [string available-color-patterns default]
  (when string
    (or
      (some (fn [color]
              (-> (re-seq color string)
                  last))
            available-color-patterns)
      default)))

(defn- extract-size-and-color-pairs-from-options [options]
  (map
    (fn [option]
      (let [size (extract-size-from-string option)
            color (extract-color-from-string option retail-available-colors "빈칸")]
        [size color]))
    options))

(defn- extract-size-and-color-pairs-from-product-names [product-names]
  (map
    (fn [product-name]
      (let [size (extract-size-from-string product-name)
            color (extract-color-from-string product-name whole-sale-available-colors "빈칸")]
        [size color]))
    product-names))

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
  [filename]
  (let [{:keys [first-row contents-rows]} (separate-first-row-and-contents-rows
                                            (read-files-as-row-vector filename))
        options (extract-column-from-rows 4 contents-rows)
        size-color-pairs (extract-size-and-color-pairs-from-options options)
        first-row-with-size-and-color (add-size-and-color-columns-to-row first-row)
        contents-rows-with-size-and-color (add-columns-to-rows contents-rows size-color-pairs)]
    (write-results-to-csv-file "retail-output.csv"
                               (cons first-row-with-size-and-color
                                     contents-rows-with-size-and-color))))

(defn run-wholesale-mode! [filename]
  (let [{:keys [first-row contents-rows]} (separate-first-row-and-contents-rows
                                            (read-files-as-row-vector filename))
        product-names (extract-column-from-rows 2 contents-rows)
        size-color-pairs (extract-size-and-color-pairs-from-product-names product-names)
        first-row-with-size-and-color (add-size-and-color-columns-to-row first-row)
        contents-rows-with-size-and-color (add-columns-to-rows contents-rows size-color-pairs)]
    (write-results-to-csv-file "whole-output.csv"
                               (cons first-row-with-size-and-color
                                     contents-rows-with-size-and-color))))

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
