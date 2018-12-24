(ns clostellar.network
  (:import [org.stellar.sdk Network]))

(defn current
  "Returns currently selected network."
  ^Network []
  (Network/current))

(defn id
  "Returns Network ID. Network ID is SHA-256 of network passphrase."
  ^String [^Network network]
  (String/valueOf (.getNetworkId network)))

(defn passphrase
  "Returns network passphrase."
  ^String [^Network network]
  (.getNetworkPassphrase network))

(defn use-public!
  "Use Stellar Public Network."
  []
  (Network/usePublicNetwork))

(defn use-test!
  "Use Stellar Test Network."
  []
  (Network/useTestNetwork))

(defn use-passphrase!
  "Use the network with the given passphrase."
  [^String passphrase]
  (Network/use (new Network passphrase)))
