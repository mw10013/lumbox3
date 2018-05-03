(ns lumbox3.validation
  (:require [struct.core :as st]))

;; Validators

(def users-group
  {:message  "Must include users group."
   :optional true
   :validate #(some #{:users} %)})

;; Rules

(def email-rules [[st/required :message "Missing email."]
                  [st/email :message "Not a valid email."]
                  [st/max-count 254 :message "Email must be 254 characters or less."]])

(defn validate-register-user-input [m]
  (st/validate m
               {:email    email-rules
                :password [[st/required :message "Missing password."]
                           [st/string :message "Must be a string."]
                           [st/min-count 6 :message "Length must be at least 6."]
                           [st/max-count 50 :message "Length must be 50 characters or less."]]}
               {:strip true}))

(def validate-login-input validate-register-user-input)

(defn validate-edit-user-input [m]
  (st/validate m
               {:id     [[st/required :message "Missing id."]]
                :email  email-rules
                :groups [[st/every #{:admins :users :members} :message "Invalid group(s)."]
                         users-group]
                :note   [[st/string :message "Must be a string."]
                         [st/max-count 10000 :message "Length must be 10,0000 characters or less."]]}
               {:strip true}))

(comment
  (validate-register-user-input {})
  (validate-register-user-input {:email "bee@sting.com"})
  (validate-register-user-input {:email 7 :password 7})
  (validate-register-user-input {:email "bee@sting.com" :password "12345"})
  (validate-register-user-input {:email "bee@sting.com" :password "letmein"})

  (validate-edit-user-input {})
  (validate-edit-user-input {:email "bee@sting.com" :groups #{:admins :users}})
  (validate-edit-user-input {:email "bee@sting.com" :groups #{:admins}})
  (validate-edit-user-input {:email "bee@sting.com" :groups #{:users}})
  )
