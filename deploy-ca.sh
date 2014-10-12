#!/bin/bash
set -e

CA_CERT="StartSSL-CA"
INT_CERT="StartSSL-Intermediate"

curl 'https://docs.codehaus.org/download/attachments/158859410/startssl-CA.pem?version=1&modificationDate=1277952972158'  > $CA_CERT.pem

sudo keytool -import -alias $CA_CERT -file $CA_CERT.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt

curl 'https://docs.codehaus.org/download/attachments/158859410/startssl-Intermediate.pem?version=1&modificationDate=1277952972182' > $INT_CERT.pem

sudo keytool -import -alias $INT_CERT -file $INT_CERT.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt