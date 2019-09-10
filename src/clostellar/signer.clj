(ns clostellar.signer
  (:import [org.stellar.sdk Signer KeyPair]
           [org.stellar.sdk.xdr SignerKey]))

(defn from-keypair
  ^SignerKey [^KeyPair keypair]
  (Signer/ed25519PublicKey keypair))
