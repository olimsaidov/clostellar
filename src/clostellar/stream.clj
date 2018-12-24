(ns clostellar.stream
  (:import [org.stellar.sdk.requests RequestBuilder]
           [shadow.okhttp3.sse EventSourceListener]
           [shadow.okhttp3 Request$Builder]
           [org.stellar.sdk.responses GsonSingleton]
           [shadow.okhttp3.internal.sse RealEventSource]
           [java.util.concurrent TimeUnit]
           [java.io IOException]))


(defn declared-method
  [class name]
  (->> (.getDeclaredMethods class)
       (filter #(= name (.getName %)))
       (first)))


(defn invoke-method
  ([class-or-object object-or-name & [name-or-arg & rest-args :as full-args]]
   (let [method (declared-method
                  (if (class? class-or-object) class-or-object (.getClass class-or-object))
                  (if (class? class-or-object) name-or-arg object-or-name))]
     (.setAccessible method true)
     (.invoke method
              (if (class? class-or-object) object-or-name class-or-object)
              (if (class? class-or-object) rest-args full-args)))))


(defn get-field
  ([object name]
   (get-field (.getClass object) object name))
  ([class object name]
   (let [field (.getDeclaredField class name)]
     (.setAccessible field true)
     (.get field object))))


(defn stream-class
  [request-builder]
  (some->
    (.getClass request-builder)
    (declared-method "stream")
    (.getGenericReturnType)
    (.getActualTypeArguments)
    (first)))


(defn parse-event-data
  [class data]
  (when-not (contains? #{"\"hello\"" "\"goodbye\"" "\"byebye\""} data)
    (.fromJson (GsonSingleton/getInstance) data class)))


(defn http-client
  [request-builder]
  (-> (get-field RequestBuilder request-builder "httpClient")
      (.newBuilder)
      (.readTimeout 0 TimeUnit/MILLISECONDS)
      (.build)))


(defn build-request
  [url event-id]
  (-> (new Request$Builder)
      (.url url)
      (.header "Accept", "text/event-stream")
      (cond-> event-id (.header "Last-Event-ID" event-id))
      (.build)))


(defn stream
  [request-builder on-event on-close]
  (let [url           (invoke-method RequestBuilder request-builder "buildUri")
        class         (stream-class request-builder)
        client        (http-client request-builder)
        closed        (atom nil)
        event-id      (atom nil)
        event-source  (atom nil)
        last-response (atom nil)]
    (future
      (loop []
        (when-not @closed
          (let [done (promise)]
            (->> (doto
                   (new RealEventSource
                        (build-request url @event-id)
                        (proxy [EventSourceListener] []
                          (onOpen [_ response]
                            (reset! last-response response))
                          (onClosed [_]
                            (deliver done nil))
                          (onFailure [_ error response]
                            (deliver done (or error (new IOException (str "Unexpected code " response)))))
                          (onEvent [_ id _ data]
                            (reset! event-id id)
                            ;(.cursor request-builder id)
                            (when-let [data (parse-event-data class data)]
                              (on-event data)))))
                   (.connect client))
                 (reset! event-source))
            (if-let [error @done]
              (on-close (when-not @closed error))
              (recur))))))
    (fn []
      (reset! closed true)
      (when-let [o @event-source] (.cancel o))
      (when-let [o @last-response] (.close o)))))


