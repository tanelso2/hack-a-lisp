(ns hack-a-lisp.snippets.docker-compose
  (:require
    [clojure.string :as str]
    [clj-toolbox.prelude :refer :all]
    [hack-a-lisp.util :refer [remove-empty-map-entries]]))


(defn service
  [& {:keys [image
             ports
             volumes
             environment
             devices
             cap_add
             restart
             hostname
             extra_hosts]
      :or {image "nginx:latest"
           ports []
           volumes []
           environment []
           devices []
           cap_add []
           restart nil
           hostname nil
           extra_hosts []}}]
  (remove-empty-map-entries
    {:image image
     :ports ports
     :volumes volumes
     :environment environment
     :devices devices
     :cap_add cap_add
     :restart restart
     :hostname hostname
     :extra_hosts extra_hosts}))

(defn mount
  ([path]
   (mount path path))
  ([host-path container-path]
   (if (nil? host-path)
      (if (some? container-path)
        (mount container-path)
        nil)
      (format "%s:%s" host-path container-path))))

(defn forwarding-tailscale-service
  [& {:keys [tailscale/hostname
             tailscale/authkey
             tailscale/volume
             tailscale/tags
             tailscale/destip]}]
  (assert (some? authkey) "tailscale/authkey not provided")
  (assert (some? destip) "tailscale/destip not provided")
  (let [state-dir "/var/lib/tailscale"]
    (service
      :image "tailscale/tailscale:latest"
      :hostname hostname
      :environment (remove-empty-map-entries
                     {"TS_AUTHKEY" authkey
                      "TS_EXTRA_ARGS" (when (some? tags)
                                        (format "--advertise-tags=%s" (str/join "," tags)))
                      "TS_STATE_DIR" state-dir
                      "TS_DEST_IP" destip
                      "TS_USERSPACE" "false"
                      "TS_ENABLE_METRICS" "true"})
      :ports ["9002:9002"]
      :volumes [(mount volume state-dir)]
      :cap_add [:net_admin]
      :devices [(mount "/dev/net/tun")]
      :restart :unless-stopped)))

(defn prometheus-service
  [& {:keys [prom/version
             prom/config-volume
             prom/tsdb-volume
             prom/retention]
      :or {version "2.35.0"
           retention "90d"}}]
  (let [config-path "/etc/prometheus/prometheus.yml"
        tsdb-path "/var/lib/prometheus"]
    (service
      :image (format "prom/prometheus:v%s" version)
      :volumes [(if (nil? config-volume)
                    (mount config-path)
                    (mount config-volume config-path))
                (if (nil? tsdb-volume)
                    (mount tsdb-path)
                    (mount tsdb-volume tsdb-path))]
      :command [(format "--config.file=%s" config-path)
                (format "--storage.tsdb.path=%s/" tsdb-path)
                (format "--storage.tsdb.retention.time=%s" retention)]
      :restart :unless-stopped
      :ports [(mount 9090)]
      :user "1001"
      :extra_hosts [(mount "host.docker.internal" "host-gateway")])))

(defn grafana-service
  [& {:keys [grafana/version
             grafana/config-volume
             grafana/storage-volume]
      :or {version "11.4.0"}}]
  (let [config-path "/etc/grafana/grafana.ini"
        storage-path "/var/lib/grafana"]
    (service
      :image (format "grafana/grafana:%s" version)
      :volumes [(mount config-volume config-path)
                (mount storage-volume storage-path)]
      :restart :unless-stopped
      :ports [(mount 9000)]
      :user "133")))
