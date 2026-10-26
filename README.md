# DIGIGUARD – Secure Authentication System

DIGIGUARD is a Java-based project that demonstrates client identity verification using RSA-2048 keys and SHA256withRSA digital signatures.

## Features
- RSA public and private key generation
- Random challenge generation
- Digital signature verification
- Java TCP and HTTP servers
- Interactive HTML verification interface

## Technologies Used
- Java
- HTML, CSS, JavaScript
- RSA-2048 and SHA256withRSA
- TCP and HTTP networking

## Prerequisites
- JDK 17 or compatible
- Windows
- Modern web browser

## How to Run

**1. Compile the source files**
```cmd
javac Server.java WebServer.java Client.java CertGenerator.java
```

**2. Generate RSA keys**
```cmd
java CertGenerator
```

**3. Start the authentication server**
```cmd
java Server
```
Keep this terminal open.

**4. Start the web server**

Open a second terminal in the same project folder:
```cmd
java WebServer
```
Keep this terminal open.

**5. Open the website**

Open `index.html` in your browser and click **Verify Client Identity**. Ensure ports `5000` and `8080` are available.

## Security Notes
- Never upload `private.key` to GitHub.
- Keep generated key files and compiled `.class` files out of the repository.
- This project is an educational demonstration and requires further security review before production use.

## Author
Developed as a Computer Networks and Security project.
