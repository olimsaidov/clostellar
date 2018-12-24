(ns clostellar.response
  (:import [shadow.com.google.gson.annotations SerializedName]
           [java.util ArrayList Map]
           [org.stellar.sdk KeyPair]))


(defn convert [object]
  (let [class (when object (.getClass object))]
    (cond
      (nil? object)
      nil

      (or (instance? java.lang.Boolean object)
          (instance? java.lang.Number object)
          (instance? java.lang.String object))
      object

      (or (instance? ArrayList object) (.isArray class))
      (mapv convert object)

      (instance? Map object)
      (into {} object)

      (instance? KeyPair object)
      (.getAccountId object)

      (-> class .getPackage .getName (.startsWith "org.stellar.sdk"))
      (reduce
        (fn [acc field]
          (if-let [ann (.getAnnotation field SerializedName)]
            (do
              (.setAccessible field true)
              (assoc acc (keyword (.value ann))
                         (convert (.get field object))))
            acc))
        (with-meta {} {::class class})
        (concat (.getDeclaredFields class)
                (some-> class .getSuperclass .getDeclaredFields)))

      :else object)))
