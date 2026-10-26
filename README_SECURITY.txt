DIGIGUARD SECURITY NOTES

- This is an educational local demonstration, not production authentication software.
- Server.java trusts only the public key stored in local public.key. The key must be registered/managed securely for any real use.
- WebServer.java signs challenges using local private.key. Do not claim the browser itself holds the private key.
- Never upload private.key or real private credentials to public GitHub. If exposed, consider them compromised.
- SecureRandom generates the server challenge; message lengths and socket timeouts are checked.
- The HTTP endpoint binds to 127.0.0.1 and should not be exposed directly to a network.
