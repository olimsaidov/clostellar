(ns clostellar.server
  (:refer-clojure :exclude [test])
  (:require [clostellar.transaction :refer [map->transaction]])
  (:import [org.stellar.sdk Server]
           [org.stellar.sdk.requests RequestBuilder$Order]))


(def test-uri "https://horizon-testnet.stellar.org")


(def public-uri "https://horizon.stellar.org/")


(defn from-uri
  ^Server [^String uri]
  (new Server uri))


(defn test
  ^Server []
  (from-uri test-uri))


(defn public
  ^Server []
  (from-uri public-uri))


(defn accounts
  [^Server server & {:keys [account cursor limit order]}]
  (let [request-builder (.accounts server)]
    (if (some? account)
      (.account request-builder account)
      (cond-> request-builder
        (some? cursor) (.cursor cursor)
        (some? limit) (.limit limit)
        (some? order) (.order ({:asc  RequestBuilder$Order/ASC
                                :desc RequestBuilder$Order/DESC} order))))))


(defn transactions
  [^Server server & {:keys [transaction account ledger cursor limit order]}]
  (let [request-builder (.transactions server)]
    (if (some? transaction)
      (.transaction request-builder transaction)
      (cond-> request-builder
        (some? account) (.forAccount account)
        (some? ledger) (.forLedger ledger)
        (some? cursor) (.cursor cursor)
        (some? limit) (.limit limit)
        (some? order) (.order ({:asc  RequestBuilder$Order/ASC
                                :desc RequestBuilder$Order/DESC} order))))))


(defn payments
  [^Server server & {:keys [transaction account ledger cursor limit order]}]
  (let [request-builder (.payments server)]
    (cond-> request-builder
      (some? account) (.forAccount account)
      (some? ledger) (.forLedger ledger)
      (some? transaction) (.forTransaction transaction)
      (some? cursor) (.cursor cursor)
      (some? limit) (.limit limit)
      (some? order) (.order ({:asc  RequestBuilder$Order/ASC
                              :desc RequestBuilder$Order/DESC} order)))))


(defn operations
  [^Server server & {:keys [operation account ledger transaction cursor limit order]}]
  (let [request-builder (.operations server)]
    (if (some? operation)
      (.operation request-builder operation)
      (cond-> request-builder
        (some? account) (.forAccount account)
        (some? ledger) (.forLedger ledger)
        (some? transaction) (.forTransaction transaction)
        (some? cursor) (.cursor cursor)
        (some? limit) (.limit limit)
        (some? order) (.order ({:asc  RequestBuilder$Order/ASC
                                :desc RequestBuilder$Order/DESC} order))))))


(defn assets
  [^Server server {:keys [asset-code asset-issuer]}]
  (cond-> (.assets server)
    (some? asset-code) (.assetCode asset-code)
    (some? asset-issuer) (.assetIssuer asset-issuer)))


(defn submit-transaction
  [^Server server source-account transaction]
  (->> (update transaction :signers #(or % [source-account]))
       (map->transaction (accounts server :account source-account))
       (.submitTransaction server)))
