(ns clostellar.friendbot
  (:require [clostellar.keypair :refer [account-id]])
  (:import [org.stellar.sdk KeyPair]))


(defn create!
  [^KeyPair account]
  (slurp (str "https://horizon-testnet.stellar.org/friendbot?addr=" (account-id account))))
