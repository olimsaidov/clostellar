(ns clostellar.asset
  (:import [org.stellar.sdk Asset AssetTypeNative KeyPair]))

(defn create
  "Creates new asset from a given code and issuer KeyPair."
  [^String code ^KeyPair issuer]
  (Asset/createNonNativeAsset code issuer))

(defn native
  "Returns native asset instance."
  ^AssetTypeNative []
  (new AssetTypeNative))
