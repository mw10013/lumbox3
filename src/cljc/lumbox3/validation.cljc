(ns lumbox3.validation
  (:require [struct.core :as st]))

(defn validate-register-user-input [m]
  (st/validate m {:email    [[st/required :message "Missing email."]
                             [st/email :message "Not a valid email."]
                             [st/max-count 254 :message "Email must be 254 characters or less."]]
                  :password [[st/required :message "Missing password."]
                             [st/string :message "Must be a string."]
                             [st/min-count 6 :message "Length must be at least 6."]
                             [st/max-count 50 :message "Length must be 50 characters or less."]]}))

(def validate-login-input validate-register-user-input)

(comment
  (validate-register-user-input {})
  (validate-register-user-input {:email "bee@sting.com"})
  (validate-register-user-input {:email 7 :password 7})
  (validate-register-user-input {:email "bee@sting.com" :password "12345"})
  (validate-register-user-input {:email "bee@sting.com" :password "letmein"})
  )
