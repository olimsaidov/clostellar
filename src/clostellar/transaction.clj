(ns clostellar.transaction
  (:require [clostellar.operation :refer [map->operation]])
  (:import [org.stellar.sdk Memo
                           KeyPair
                           TimeBounds
                           Transaction$Builder
                           TransactionBuilderAccount]))


(defmulti map->memo :type)


(defmethod map->memo :text
  [{:keys [^String payload]}]
  (Memo/text payload))


(defmethod map->memo :id
  [{:keys [^long payload]}]
  (Memo/id payload))


(defmethod map->memo :hash
  [{:keys [^bytes payload]}]
  (Memo/hash payload))


(defmethod map->memo :return-hash
  [{:keys [payload]}]
  (Memo/returnHash payload))


(defn map->time-bounds
  [{:keys [^long min-time
           ^long max-time]}]
  (new TimeBounds min-time max-time))


(defn map->transaction
  [^TransactionBuilderAccount source-account
   {:keys [operations timeout memo time-bounds signers]}]
  (let [builder (new Transaction$Builder source-account)]
    (-> builder
        (.setTimeout (if (some? timeout)
                       (long timeout) 0)))
    (doseq [operation operations]
      (-> builder
          (.addOperation (map->operation operation))))
    (when (some? memo)
      (-> builder
          (.addMemo (map->memo memo))))
    (when (some? time-bounds)
      (-> builder
          (.addTimeBounds (map->time-bounds time-bounds))))
    (let [transaction (.build builder)]
      (doseq [^KeyPair signer signers]
        (.sign transaction signer))
      transaction)))
