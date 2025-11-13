¿Que falta por hacer?
Hacer documentacion teniendo en cuenta todo el proyecto
Comentar todo el codigo en ingles
Probar todo el flujo, pa ver si funciona

En documentacion poner:

Acceso a la base de datos para ver el salt y todo eso
Explicar la encriptacion
Hacer el reporte
 java -cp target/classes com.example.usermanagement.util.DevPasswordUtil admin  
 Esto para generar hash y salt
 Se generaron certificados entonces se esta manejando https en front y back, explicar eso
 Explicar que se generaron certificados autofirmados y se agregaron como confiables , toca hacer eso en cada pc para que el navegador lo reconozca como confiable

 Para el certificado del backend 

 keytool -genkeypair -alias localdev -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore src/main/resources/keystore.p12 -validity 3650 -dname "CN=localhost, OU=Dev, O=Icesi, L=Cali, ST=CA, C=CO" -storepass changeit -keypass changeitcd 

 para el frontend: mkdir -p certs && openssl pkcs12 -in backend/src/main/resources/keystore.p12 -nocerts -nodes -passin pass:changeit -out certs/localhost-key.pem && openssl pkcs12 -in backend/src/main/resources/keystore.p12 -clcerts -nokeys -passin pass:changeit -out certs/localhost.pem && ls -l certs

 Luego toco hacer esto, para el frontend:

 Why/what/outcome: I'll inspect the extracted certificate to check its Subject and Subject Alternative Names (SAN). Chrome requires SANs to match the host; if SANs are missing, Chrome will still warn even if the cert is trusted. I'll run openssl to show the cert details and then give exact steps to fix it.


openssl x509 -in "certs/localhost.pem" -noout -text

openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout "certs/localhost-key.pem" -out "certs/localhost.pem" -subj "/CN=localhost" -addext "subjectAltName = DNS:localhost, IP:127.0.0.1, IP:::1"

openssl x509 -in certs/localhost.pem -noout -text
Summarized conversation history

He verificado el certificado regenerado: ahora incluye SubjectAltName (DNS: localhost, IPs 127.0.0.1 y ::1), por lo que cumple el requisito de Chrome. Siguientes pasos breves para que el navegador acepte HTTPS en desarrollo y las cookies Secure funcionen:

Y luego para asegurar que el del backend fuera igual a front toco hacer esto:

openssl pkcs12 -export -out backend/src/main/resources/keystore.p12 -inkey certs/localhost-key.pem -in certs/localhost.pem -passout pass:changeit -name tomcat








 explicar todo el proyecto
