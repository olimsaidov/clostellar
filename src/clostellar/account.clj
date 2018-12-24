(ns clostellar.account
  (:import [org.stellar.sdk KeyPair Account]))

(defn from-keypair
  ^Account [^KeyPair keypair sequence-number]
  (new Account keypair sequence-number))
