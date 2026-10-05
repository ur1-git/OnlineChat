# Chat sencillo, explicado en el código

Necesita Java 8 o posterior. No utiliza librerías externas.

## Cómo abrirlo en Eclipse

1. Ejecuta `src/main/MainSrv.java`: es el servidor. Déjalo abierto.
2. Ejecuta `src/main/Main.java`: es un cliente.
3. Ejecuta `Main.java` otra vez: es el segundo cliente.
4. Selecciona la consola de cada cliente en Eclipse para escribir los comandos de `DEMO.md`.

Para salir de un cliente escribe EXIT. Para parar el servidor pulsa el botón rojo de Eclipse.

## Qué hace cada archivo

- `MainSrv.java`: conserva la estructura del main que crea `Servidor(40000)`, abre las conexiones, llama a `pedirNombres`, entra en el bucle y cierra el servidor si termina.
- `Main.java`: parte del main original del proyecto y abre un cliente.
- `Cliente.java`: manda lo que escribes. Incluye un hilo Receptor para mostrar los mensajes que llegan mientras escribes.
- `servidor/Servidor.java`: escucha las conexiones y procesa los comandos. Incluye un hilo AtenderCliente para cada conexión.

`pedirNombres()` muestra una indicación. Los nombres se reciben con LOGIN dentro de los hilos: no hay una pregunta extra fuera del protocolo.

## Idea del programa

El cliente y el servidor se comunican por un socket, como un canal entre ambos. Cada comando y respuesta ocupa una línea. `println` envía una línea y `readLine` la recibe.

Un hilo permite hacer otra tarea al mismo tiempo. El servidor usa uno por conexión y el cliente usa uno para recibir. La lista `clientes` guarda las sesiones autenticadas. `synchronized` permite procesar un comando a la vez para proteger la lista y el archivo.

Las cuentas se buscan directamente en `usuarios.txt`, recorriendo sus líneas con Scanner. REGISTER añade una línea al archivo. Es menos eficiente que una base de datos, pero resulta sencillo para este ejercicio.

## Protocolo

```text
REGISTER ana 1234
LOGIN ana 1234
SETNICK Anita
SAY Hola a todos
EXIT
```

REGISTER, LOGIN y SETNICK devuelven OK o KO. REGISTER no inicia sesión. SAY requiere LOGIN y entrega `HEAR Anita Hola a todos` a todos los clientes autenticados, incluido quien lo envía. EXIT cierra la conexión sin respuesta. Los comandos se escriben en mayúsculas y sin punto y coma.

Usuarios, nicks y contraseñas son una palabra sin espacios. Un nick no puede coincidir con el nick o la cuenta de otra persona conectada. No se puede iniciar sesión dos veces con la misma cuenta. SETNICK modifica el nombre visible de la sesión; la cuenta del archivo se mantiene. Un comando incorrecto devuelve KO.

El servidor crea `usuarios.txt` al registrar la primera cuenta, en la carpeta desde la que se ejecuta. Las cuentas siguen disponibles al reiniciar. Para mantener el ejercicio simple, las contraseñas se guardan en texto plano: utiliza contraseñas inventadas para la demo.

## Desde una terminal

Compila desde la carpeta del proyecto:

```sh
javac -encoding UTF-8 -d bin src/main/*.java src/servidor/*.java
```

En una terminal abre el servidor:

```sh
java -cp bin main.MainSrv
```

En otras dos terminales abre un cliente en cada una:

```sh
java -cp bin main.Main
```

Se utiliza localhost y el puerto 40000, siguiendo MainSrv. No hay menús de configuración ni dependencias.

## Entrega

`entrega/OnlineChat.zip` contiene el código, los archivos del proyecto Eclipse y estos documentos. `DEMO.md` incluye la sesión de ejemplo y resultados de una prueba real con dos conexiones.
