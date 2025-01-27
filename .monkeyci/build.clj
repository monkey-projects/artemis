(ns build
  (:require [monkey.ci.build.v2 :as m]
            [monkey.ci.plugin.kaniko :as pk]))

(def plugin-version "2.1.0")
(def plugin-dir "artemis-prometheus-metrics-plugin")

(def artemis-version "2.39.0")

(def plugin-lib-artifact
  (m/artifact "plugin-lib"
              (format "%s/artemis-prometheus-metrics-plugin/target/original-artemis-prometheus-metrics-plugin-%s.jar"
                      plugin-dir
                      plugin-version)))

(def plugin-war-artifact
  (m/artifact "plugin-war"
              (str plugin-dir "/artemis-prometheus-metrics-plugin-servlet/target/metrics.war")))

(def metrics-plugin
  (-> (m/container-job "metrics-plugin")
      (m/image "docker.io/maven:3.9-eclipse-temurin-21")
      (m/script
       [(format "git clone -b v%s https://github.com/rh-messaging/artemis-prometheus-metrics-plugin.git"
                plugin-version)
        (format "cd %s && mvn install" plugin-dir)])
      (m/save-artifacts [plugin-lib-artifact
                         plugin-war-artifact])))

(def docker-dir (format "apache-artemis-%s/artemis-docker" artemis-version))
(def target-dir (str docker-dir "/target/artemis/" artemis-version))

(def docker-files-artifact
  (m/artifact "docker-files" target-dir))

(def prepare-image
  "Downloads the necessary files to build the artemis image, and copies plugin files to
   their appropriate locations."
  (let [download-url (format "https://www.apache.org/dyn/closer.cgi?filename=activemq/activemq-artemis/%s/apache-artemis-%s-source-release.tar.gz&action=download" artemis-version artemis-version)]
    ;; We could also use an action job for this
    (-> (m/container-job "prepare-image")
        (m/image "docker.io/alpine/curl:latest")
        (m/script [(format "curl -L '%s' -o artemis-src.tgz" download-url)
                   "tar xzf artemis-src.tgz"
                   (format "cd %s && ./prepare-docker.sh --from-release --artemis-version %s"
                           docker-dir artemis-version)
                   (format "cp %s %s/lib" (:path plugin-lib-artifact) target-dir)
                   (format "cp %s %s/web" (:path plugin-war-artifact) target-dir)])
        (m/depends-on (m/job-id metrics-plugin))
        (m/restore-artifacts [plugin-lib-artifact
                              plugin-war-artifact])
        (m/save-artifacts [docker-files-artifact]))))

(def build-image
  (pk/multi-platform-image-job
   {:target-img (str "fra.ocir.io/frjdhmocn5qi/artemis:" artemis-version)
    :archs [:arm :amd]
    :dockerfile (str target-dir "/docker/Dockerfile-alpine-21-jre")
    :subdir target-dir
    :container-opts {:dependencies ["prepare-image"]
                     :restore-artifacts [docker-files-artifact]}}))

(def debug
  (-> (m/container-job "debug")
      (m/image "docker.io/alpine:latest")
      (m/script ["pwd"
                 "ls -l"
                 (str "ls -l " target-dir)])
      (m/depends-on "prepare-image")
      (m/restore-artifacts [docker-files-artifact])))

[metrics-plugin
 prepare-image
 build-image
 debug]
