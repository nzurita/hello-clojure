(ns app.main.api)

(defn get-characters [page]
  (-> (js/fetch (str "/api/rpc/command/get-characters?page=" page))
      (.then (fn [resp] (.json resp)))))

(defn get-character [id]
  (-> (js/fetch (str "/api/rpc/command/get-character?id=" id))
      (.then (fn [resp] (.json resp)))))

(defn get-planet [id]
  (-> (js/fetch (str "/api/rpc/command/get-planet?id=" id))
      (.then (fn [resp] (.json resp)))))
