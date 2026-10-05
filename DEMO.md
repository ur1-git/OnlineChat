# Demostración del chat

**Objetivo:** registrar dos cuentas, iniciar sesión, intercambiar mensajes, cambiar un nick y salir. Esta demo se reproduce con un servidor y dos clientes, siguiendo `README.md`. Las líneas OK, KO y HEAR son respuestas del servidor; las demás son comandos escritos por la persona.

## 1. Iniciar el servidor

La consola muestra:

```text
Servidor escuchando en el puerto 5000
Cuentas: .../usuarios.txt
```

La ruta depende de dónde se ejecuta el programa. Abrir ahora dos clientes.

## 2. Registrar e iniciar sesión

En el cliente de Ana:

```text
REGISTER ana 1234
OK
REGISTER ana 1234
KO
LOGIN ana mal
KO
LOGIN ana 1234
OK
```

El registro duplicado y la contraseña incorrecta se rechazan.

En el cliente de Luis:

```text
REGISTER luis 5678
OK
LOGIN luis 5678
OK
```

Si esas cuentas ya existen de una demo anterior, REGISTER devuelve KO; se puede continuar con LOGIN y la contraseña original.

## 3. Cambiar el nick

Ana escribe:

```text
SETNICK luis
KO
SETNICK Anita
OK
```

El primer nombre está ocupado por Luis. El cambio a Anita solo afecta a esta sesión.

## 4. Conversar

Ana escribe:

```text
SAY Hola a todos
```

**Los dos clientes** reciben:

```text
HEAR Anita Hola a todos
```

Luis escribe:

```text
SAY Hola Ana
```

**Los dos clientes** reciben:

```text
HEAR luis Hola Ana
```

Esto demuestra que el servidor distribuye los mensajes y que el cliente puede recibir mientras espera entradas del teclado.

## 5. Salir y comprobar el archivo

Cada cliente escribe `EXIT`; su programa termina. El servidor sigue esperando nuevas conexiones.

El archivo `usuarios.txt` contiene estas cuentas (puede tener otras de pruebas anteriores):

```text
ana 1234
luis 5678
```

Detener y volver a abrir el servidor. Abrir un cliente e introducir `LOGIN ana 1234`: devuelve OK sin volver a registrar. La cuenta sigue siendo ana, aunque antes usó el nick Anita.

## Capturas para acompañar la entrega

Se pueden añadir tres capturas propias: servidor y dos clientes abiertos; intercambio de mensajes HEAR; archivo usuarios.txt con las cuentas ficticias. Este documento contiene el guion y los resultados esperados, no capturas de Eclipse.

## Comprobación realizada

Se compiló el código con `javac` y se probó el servidor mediante dos conexiones TCP locales, usando un archivo temporal de cuentas. Esta es la salida real de la comprobación (comando → respuesta):

```text
SAY sin login -> KO
REGISTER ana 1234 -> OK
REGISTER ana 1234 -> KO
LOGIN ana mal -> KO
LOGIN ana 1234 -> OK
LOGIN ana 1234 -> KO
REGISTER luis 5678 -> OK
LOGIN luis 5678 -> OK
SETNICK luis -> KO
SETNICK Anita -> OK
SAY Hola a todos -> HEAR Anita Hola a todos
SAY Hola Ana -> HEAR luis Hola Ana
SETNICK nombre con espacios -> KO
SAY  -> KO
LOGIN ana 1234 -> OK
```

El segundo LOGIN de ana se hizo desde la otra conexión y se rechazó porque la cuenta ya estaba conectada. El último LOGIN se hizo tras reiniciar el servidor. También se comprobó que ambos clientes recibían cada HEAR y que EXIT cerraba ambas conexiones. La prueba no usó la interfaz de Eclipse.
