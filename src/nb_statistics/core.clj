(ns nb-statistics.core
  (:require [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def available-colors
  [#"회색" #"하늘" #"검정" #"핑크" #"백색" #"혼합"])

(defn greater-than-and-less-than [min max num]
  (and num (> num min) (> max num)))

(defn remove-leading-0s [num]
  (loop [n num]
    (if (= (first n) \0)
      (recur (apply str (rest n)))
      n)))

(defn run-retail-mode!
  [file-name]
  (let [rows (vec (csv/read-csv (slurp file-name)))
        first-row (first rows)
        contents-rows (rest rows)
        option-columns (map (fn [vec] (vec 4)) contents-rows)
        numbers-in-option-column (map
                                   (fn [option]
                                     (some->> option
                                              (re-seq #"\d+")
                                              (map remove-leading-0s)
                                              (map clojure.edn/read-string)
                                              (filter (fn [num] (greater-than-and-less-than 60 130 num)))
                                              first))
                                   option-columns)
        colors-in-option-column (map
                                  (fn [option]
                                    (when option
                                      (or
                                        (some (fn [color]
                                                (re-find color option))
                                              available-colors)
                                        "빈칸")))
                                  option-columns)
        number-color-pairs (map (fn [num col] [num col]) numbers-in-option-column colors-in-option-column)]
    (let [first-row-with-size-and-color (concat first-row ["사이즈" "색상"])
          contents-rows-with-size-and-color (map (fn [original-row
                                                      num-color-pair]
                                                   (concat original-row
                                                           num-color-pair))
                                                 contents-rows number-color-pairs)]
      (with-open [writer (io/writer "out-file.csv")]
        (csv/write-csv writer
                       (cons first-row-with-size-and-color
                             contents-rows-with-size-and-color))))))

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
