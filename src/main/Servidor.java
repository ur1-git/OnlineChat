package main;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Servidor {
    // Todo el estado compartido se protege con synchronized.
    private static final Map<String, String> usuarios = new HashMap<>();
    private static final List<Conexion> conectados = new ArrayList<>();
    private static final Path ARCHIVO = Paths.get("usuarios.txt");

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        if (Files.exists(ARCHIVO)) {
            for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                String[] datos = linea.split(" ", 2);
                if (datos.length == 2) usuarios.put(datos[0], datos[1]);
            }
        }
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            System.out.println("Servidor escuchando en el puerto " + puerto);
            System.out.println("Cuentas: " + ARCHIVO.toAbsolutePath());
            while (true) {
                // Un hilo por cliente permite atender a varias personas a la vez.
                new Conexion(servidor.accept()).start();
            }
        }
    }

    private static class Conexion extends Thread {
        private final Socket socket;
        private PrintWriter salida;
        private String usuario; // Cuenta con la que se ha iniciado sesión.
        private String nick;    // Nombre que aparece en el chat.

        Conexion(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (Socket conexion = socket;
                 BufferedReader entrada = new BufferedReader(new InputStreamReader(
                         conexion.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter escritor = new PrintWriter(new OutputStreamWriter(
                         conexion.getOutputStream(), StandardCharsets.UTF_8), true)) {
                salida = escritor;
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    if (linea.equals("EXIT")) break;
                    procesar(linea);
                }
            } catch (IOException e) {
                System.out.println("Cliente desconectado: " + e.getMessage());
            } finally {
                synchronized (Servidor.class) { conectados.remove(this); }
            }
        }

        private void procesar(String linea) {
            String[] partes = linea.split(" ", 2);
            String comando = partes[0];
            String argumento = partes.length == 2 ? partes[1] : "";
            synchronized (Servidor.class) {
                switch (comando) {
                    case "REGISTER": {
                        String[] datos = argumento.split(" ", -1);
                        if (datos.length != 2 || !nombreValido(datos[0])
                                || datos[1].isEmpty() || usuarios.containsKey(datos[0])) {
                            salida.println("KO");
                            break;
                        }
                        try {
                            // Guardar primero: solo responder OK si se escribió el archivo.
                            Files.write(ARCHIVO, (datos[0] + " " + datos[1] + "\n")
                                    .getBytes(StandardCharsets.UTF_8),
                                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                            usuarios.put(datos[0], datos[1]);
                            salida.println("OK");
                        } catch (IOException e) {
                            salida.println("KO");
                            System.err.println("No se pudo guardar la cuenta: " + e.getMessage());
                        }
                        break;
                    }
                    case "LOGIN": {
                        String[] datos = argumento.split(" ", -1);
                        if (usuario != null || datos.length != 2
                                || !datos[1].equals(usuarios.get(datos[0]))
                                || cuentaEnUso(datos[0]) || nickEnUso(datos[0])) {
                            salida.println("KO");
                        } else {
                            usuario = datos[0];
                            nick = usuario;
                            conectados.add(this);
                            salida.println("OK");
                        }
                        break;
                    }
                    case "SETNICK":
                        if (usuario == null || !nombreValido(argumento) || nickEnUso(argumento)) {
                            salida.println("KO");
                        } else {
                            nick = argumento;
                            salida.println("OK");
                        }
                        break;
                    case "SAY":
                        if (usuario == null || argumento.trim().isEmpty()) {
                            salida.println("KO");
                        } else {
                            for (Conexion cliente : conectados) {
                                cliente.salida.println("HEAR " + nick + " " + argumento);
                            }
                        }
                        break;
                    default:
                        salida.println("KO");
                }
            }
        }

        private boolean cuentaEnUso(String nombre) {
            for (Conexion cliente : conectados) {
                if (nombre.equals(cliente.usuario)) return true;
            }
            return false;
        }

        private boolean nickEnUso(String nombre) {
            for (Conexion cliente : conectados) {
                if (cliente != this && nombre.equals(cliente.nick)) return true;
            }
            return false;
        }
    }

    private static boolean nombreValido(String nombre) {
        // Un nombre es una sola palabra: así HEAR se puede interpretar fácilmente.
        return nombre.matches("[a-zA-Z0-9_]{1,20}");
    }
}
