# Chat sencillo en Java

Aplicación de consola para practicar sockets TCP, hilos y archivos. No necesita librerías externas. Usa un JDK 8 o posterior.

## Ejecutar en Eclipse

1. Abrir este proyecto Java en Eclipse.
2. Ejecutar `src/main/Servidor.java` con **Run As > Java Application**.
3. Ejecutar `src/main/Cliente.java` dos veces para tener dos personas conectadas. `Main.java` también abre un cliente.
4. En la vista Console, usar el selector de consolas para elegir cada cliente. Escribir los comandos y pulsar Enter.
5. Seguir la demo de `DEMO.md`.

El servidor se deja abierto durante la demo. Para detenerlo, usar el botón rojo de Eclipse. `EXIT` cierra solo el cliente que lo envía.

## Ejecutar en una terminal

Desde la carpeta del proyecto, compilar una vez:

```sh
javac -encoding UTF-8 -d bin src/main/*.java
```

Abrir tres terminales en esa carpeta. En la primera:

```sh
java -cp bin main.Servidor
```

En cada una de las otras dos:

```sh
java -cp bin main.Cliente
```

El puerto por defecto es 5000. Se puede cambiar con `java -cp bin main.Servidor 6000` y conectar con `java -cp bin main.Cliente localhost 6000`. Para conectar desde otro ordenador, sustituir `localhost` por la IP del servidor; ambos equipos deben tener conectividad y el puerto debe estar permitido.

## Cómo entender el código

- **Servidor.java:** abre un `ServerSocket` y espera conexiones con `accept()`. Cada conexión tiene un hilo `Conexion`, que lee comandos, comprueba las cuentas y responde. El mapa `usuarios` contiene las cuentas; la lista `conectados` contiene las sesiones que han hecho LOGIN.
- **Cliente.java:** abre un `Socket`. El hilo principal lee el teclado y envía líneas; otro hilo recibe respuestas y mensajes. Así se puede recibir un mensaje sin tener que escribir primero.
- **Main.java:** llama a `Cliente.main(args)` para aprovechar la clase que ya tenía el proyecto.

`BufferedReader.readLine()` recibe una línea. `PrintWriter.println()` envía una línea; el parámetro `true` hace que salga inmediatamente. Ambos extremos usan UTF-8. `synchronized` evita que dos hilos modifiquen las cuentas o sesiones al mismo tiempo.

## Decisiones del protocolo

Cada comando ocupa una línea, sin punto y coma al final. Los comandos se escriben en mayúsculas. REGISTER crea una cuenta, pero no inicia sesión. LOGIN inicia sesión y usa el nombre de usuario como nick inicial. SAY envía `HEAR <nick> <mensaje>` a todos los clientes autenticados, incluido el emisor; no envía un OK adicional. SETNICK cambia solo el nombre visible de la sesión, sin modificar la cuenta. EXIT cierra la conexión sin respuesta.

Usuarios y nicks admiten de 1 a 20 letras sin acentos, números o guiones bajos. Las contraseñas son una palabra sin espacios. Los mensajes pueden contener espacios y acentos. No se admiten sesiones simultáneas de la misma cuenta ni nicks repetidos. Un comando desconocido, mal formado o no permitido recibe KO. SAY y SETNICK requieren LOGIN.

`usuarios.txt` se crea al registrar la primera cuenta en la carpeta desde la que se ejecuta el servidor. Al reiniciar, el servidor vuelve a leerlo. Las contraseñas se guardan en texto plano para mantener sencillo este ejercicio: usar cuentas inventadas, nunca contraseñas reales. No hay cifrado, historial, interfaz gráfica ni recuperación automática de conexiones.

## Entrega

`entrega/OnlineChat.zip` incluye el proyecto Eclipse, el código del cliente y servidor, esta guía y `DEMO.md`. El documento de demo explica una sesión reproducible; se pueden añadir capturas propias y exportarlo a PDF si el profesor pide ese formato.
