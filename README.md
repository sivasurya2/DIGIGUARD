\# DIGIGUARD



A Java-based RSA-2048 challenge–response authentication system using SHA256withRSA digital signatures.



\## Features

\- RSA key-pair generation

\- Secure random challenge generation

\- Digital signature verification

\- Java TCP and HTTP servers

\- HTML verification interface



\## Technologies

\- Java

\- HTML, CSS, JavaScript

\- RSA-2048

\- SHA256withRSA



\## Requirements

\- JDK 17 or compatible

\- Windows

\- Web browser



\## Run the Project



\*\*1. Compile\*\*

cmd

javac Server.java WebServer.java Client.java CertGenerator.java





\*\*2. Generate keys\*\*

cmd

java CertGenerator





\*\*3. Start the authentication server\*\*

cmd

java Server





\*\*4. Open another terminal and start the web server\*\*

cmd

java WebServer





\*\*5.\*\* Open `index.html` in your browser and click \*\*Verify\*\*.



\## Security

\- Never upload `private.key` or `public.key` to GitHub.

\- Do not upload `.class` files.

\- This project is an educational demonstration, not a production-ready security system.

