package servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Servidor {
    // Esta lista guarda los clientes que han hecho LOGIN.
    private static ArrayList<AtenderCliente> clientes = new ArrayList<>();

    private int puerto;
    private ServerSocket servidor;

    // MainSrv nos pasa el puerto, en este caso 40000.
    public Servidor(int puerto) {
        this.puerto = puerto;
    }

    public void initConnections() throws IOException {
        // Abrimos el puerto para que puedan conectarse los clientes.
        servidor = new ServerSocket(puerto);
        System.out.println("Servidor preparado en el puerto " + puerto + ".");
    }

    public void pedirNombres() {
        // Conservamos este método de tu MainSrv.
        // El nombre se obtiene de LOGIN, dentro del hilo de cada cliente.
        System.out.println("Cada cliente debe usar REGISTER y después LOGIN.");
    }

    public void bucleRun() throws IOException {
        while (true) {
            // accept espera hasta que alguien se conecta.
            Socket socket = servidor.accept();
            // Cada cliente tiene su hilo: así pueden conversar varios.
            AtenderCliente hilo = new AtenderCliente(socket);
            hilo.start();
        }
    }

    public void close() throws IOException {
        // Si falló la apertura, servidor todavía puede ser null.
        if (servidor != null) {
            servidor.close();
        }
    }

    // synchronized permite que solo un hilo a la vez ejecute este método.
    // Evita modificar el archivo o la lista desde dos clientes a la vez.
    public static synchronized void procesar(String linea, AtenderCliente cliente) {
        // Separamos como máximo en 3 partes: SAY puede tener espacios.
        // Ejemplo: REGISTER ana 123 -> [REGISTER, ana, 123].
        String[] partes = linea.split(" ", 3);
        String comando = partes[0];

        try {
            if (comando.equals("REGISTER")) {
                if (partes.length != 3 || !unaPalabra(partes[1])
                        || !unaPalabra(partes[2]) || buscarClave(partes[1]) != null) {
                    cliente.salida.println("KO");
                    return;
                }
                // true significa añadir al final, sin borrar las cuentas anteriores.
                try (PrintWriter archivo = new PrintWriter(new OutputStreamWriter(
                        new java.io.FileOutputStream("usuarios.txt", true), "UTF-8"))) {
                    archivo.println(partes[1] + " " + partes[2]);
                    // PrintWriter no lanza excepciones al escribir: comprobamos el error.
                    if (archivo.checkError()) {
                        cliente.salida.println("KO");
                        return;
                    }
                }
                cliente.salida.println("OK");

            } else if (comando.equals("LOGIN")) {
                if (partes.length != 3 || cliente.usuario != null
                        || !partes[2].equals(buscarClave(partes[1]))
                        || ocupado(partes[1], cliente)) {
                    cliente.salida.println("KO");
                    return;
                }
                cliente.usuario = partes[1];
                cliente.nick = partes[1]; // El nick inicial es el usuario.
                clientes.add(cliente);
                cliente.salida.println("OK");

            } else if (comando.equals("SETNICK")) {
                if (cliente.usuario == null || partes.length != 2
                        || !unaPalabra(partes[1]) || ocupado(partes[1], cliente)) {
                    cliente.salida.println("KO");
                    return;
                }
                cliente.nick = partes[1]; // La cuenta del archivo no cambia.
                cliente.salida.println("OK");

            } else if (comando.equals("SAY")) {
                if (cliente.usuario == null || partes.length < 2
                        || linea.substring(4).trim().isEmpty()) {
                    cliente.salida.println("KO");
                    return;
                }
                // Quitamos "SAY " y conservamos el mensaje completo.
                String mensaje = linea.substring(4);
                for (AtenderCliente persona : clientes) {
                    persona.salida.println("HEAR " + cliente.nick + " " + mensaje);
                }
            } else {
                cliente.salida.println("KO"); // Comando desconocido.
            }
        } catch (IOException e) {
            cliente.salida.println("KO");
            System.out.println("Error al leer o guardar usuarios: " + e.getMessage());
        }
    }

    // Para simplificar, buscamos la cuenta directamente en el archivo.
    // Si no existe, devolvemos null. No hace falta cargar un mapa en memoria.
    private static String buscarClave(String usuario) throws IOException {
        File archivo = new File("usuarios.txt");
        if (!archivo.exists()) {
            return null;
        }
        try (Scanner lector = new Scanner(archivo, "UTF-8")) {
            while (lector.hasNextLine()) {
                String[] cuenta = lector.nextLine().split(" ", 2);
                if (cuenta.length == 2 && cuenta[0].equals(usuario)) {
                    return cuenta[1];
                }
            }
            if (lector.ioException() != null) {
                throw lector.ioException();
            }
        }
        return null;
    }

    // Los usuarios, contraseñas y nicks no pueden tener espacios.
    private static boolean unaPalabra(String texto) {
        return !texto.isEmpty() && !texto.matches(".*\\s.*");
    }

    // Evitamos repetir una cuenta conectada o el nombre visible de otra persona.
    private static boolean ocupado(String nombre, AtenderCliente actual) {
        for (AtenderCliente otro : clientes) {
            if (otro != actual && (nombre.equals(otro.nick) || nombre.equals(otro.usuario))) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void quitar(AtenderCliente cliente) {
        clientes.remove(cliente);
    }
}

// Es un hilo normal, separado de Servidor para ver claramente su tarea.
class AtenderCliente extends Thread {
    private Socket socket;
    PrintWriter salida;
    String usuario; // null significa que todavía no ha hecho LOGIN.
    String nick;

    public AtenderCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // try cierra la conexión al terminar, incluso si hay un error.
        try (Socket conexion = socket) {
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(conexion.getInputStream(), "UTF-8"));
            salida = new PrintWriter(new OutputStreamWriter(
                    conexion.getOutputStream(), "UTF-8"), true);

            String linea = entrada.readLine();
            while (linea != null) {
                if (linea.equals("EXIT")) {
                    break;
                }
                Servidor.procesar(linea, this);
                linea = entrada.readLine();
            }
        } catch (IOException e) {
            System.out.println("Se desconectó un cliente.");
        } finally {
            // Se elimina tanto al recibir EXIT como al perder la conexión.
            Servidor.quitar(this);
        }
    }
}
