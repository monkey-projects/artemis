(ns build-test
  (:require [clojure.test :refer [deftest testing is]]
            [build :as sut]
            [monkey.ci.build.v2 :as m]))

(deftest metrics-plugin
  (testing "provides container job"
    (is (m/container-job? sut/metrics-plugin))))

(deftest prepare-image
  (testing "provides container job"
    (is (m/container-job? sut/prepare-image))))
