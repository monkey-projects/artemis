(ns build-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.string :as cs]
            [build :as sut]
            [monkey.ci.build.v2 :as m]))

(deftest metrics-plugin
  (testing "provides container job"
    (is (m/container-job? sut/metrics-plugin))))

(deftest prepare-image
  (let [job sut/prepare-image]
    (testing "provides container job"
      (is (m/container-job? job)))

    (testing "downloads artemis sources"
      (let [cmd (-> job :script first)]
        (is (cs/starts-with? cmd "curl"))
        (is (cs/ends-with? cmd " -o artemis-src.tgz"))))))
