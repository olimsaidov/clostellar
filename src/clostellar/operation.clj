(ns clostellar.operation
  (:import [org.stellar.sdk.xdr SignerKey]
           [org.stellar.sdk Asset
                           KeyPair
                           InflationOperation
                           PaymentOperation$Builder
                           SetOptionsOperation$Builder
                           ManageDataOperation$Builder
                           PathPaymentOperation$Builder
                           ManageOfferOperation$Builder
                           ChangeTrustOperation$Builder
                           AccountMergeOperation$Builder
                           BumpSequenceOperation$Builder
                           CreateAccountOperation$Builder
                           CreatePassiveOfferOperation$Builder]))


(defmulti map->operation :type)


(defmethod map->operation :create-account
  [{:keys [^KeyPair destination
           ^String starting-balance
           ^KeyPair source-account]}]
  (.build
    (cond-> (new CreateAccountOperation$Builder
                 destination starting-balance)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :payment
  [{:keys [^KeyPair destination
           ^Asset asset
           ^String amount
           ^KeyPair source-account]}]
  (.build
    (cond-> (new PaymentOperation$Builder
                 destination asset amount)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :path-payment
  [{:keys [^Asset send-asset
           ^String send-max
           ^KeyPair destination
           ^Asset destination-asset
           ^String destination-amount
           path
           ^KeyPair source-account]}]
  (.build
    (cond-> (new PathPaymentOperation$Builder
                 send-asset send-max destination destination-asset destination-amount)
      (some? path)
      (.setPath path)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :manage-offer
  [{:keys [^Asset selling
           ^Asset buying
           ^String amount
           ^String price
           ^long offer-id
           ^KeyPair source-account]}]
  (.build
    (cond-> (new ManageOfferOperation$Builder
                 selling buying amount price)
      (some? offer-id)
      (.setOfferId offer-id)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :create-passive-offer
  [{:keys [^Asset selling
           ^Asset buying
           ^String amount
           ^String price
           ^KeyPair source-account]}]
  (.build
    (cond-> (new CreatePassiveOfferOperation$Builder
                 selling buying amount price)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :set-options
  [{:keys [^KeyPair inflation-destination
           ^int clear-flags
           ^int set-flags
           ^int master-key-weight
           ^int low-threshold
           ^int medium-threshold
           ^int high-threshold
           ^String home-domain
           ^SignerKey signer-key
           ^Integer signer-weight
           ^KeyPair source-account]}]
  (.build
    (cond-> (new SetOptionsOperation$Builder)
      (some? inflation-destination)
      (.setInflationDestination (int inflation-destination))
      (some? clear-flags)
      (.setClearFlags (int clear-flags))
      (some? set-flags)
      (.setSetFlags (int set-flags))
      (some? master-key-weight)
      (.setMasterKeyWeight (int master-key-weight))
      (some? low-threshold)
      (.setLowThreshold (int low-threshold))
      (some? medium-threshold)
      (.setMediumThreshold (int medium-threshold))
      (some? high-threshold)
      (.setHighThreshold (int high-threshold))
      (some? home-domain)
      (.setHomeDomain home-domain)
      (and (some? signer-key) (some? signer-weight))
      (.setSigner signer-key (int signer-weight))
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :change-trust
  [{:keys [^Asset asset
           ^String limit
           ^KeyPair source-account]}]
  (.build
    (cond-> (new ChangeTrustOperation$Builder
                 asset limit)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :account-merge
  [{:keys [^KeyPair destination
           ^KeyPair source-account]}]
  (.build
    (cond-> (new AccountMergeOperation$Builder
                 destination)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :inflation
  [_]
  (new InflationOperation))


(defmethod map->operation :manage-data
  [{:keys [^String name
           ^bytes value
           ^KeyPair source-account]}]
  (.build
    (cond-> (new ManageDataOperation$Builder
                 name value)
      (some? source-account)
      (.setSourceAccount source-account))))


(defmethod map->operation :bump-sequence
  [{:keys [^long bump-to]}]
  (.build
    (new BumpSequenceOperation$Builder
         bump-to)))


(def thresholds
  {:create-account       :medium
   :payment              :medium
   :path-payment         :medium
   :manage-offer         :medium
   :create-passive-offer :medium
   :set-options          :medium                            ;; or :high
   :change-trust         :medium
   :allow-trust          :low
   :account-merge        :high
   :inflation            :low
   :manage-data          :medium
   :bump-sequence        :low})






