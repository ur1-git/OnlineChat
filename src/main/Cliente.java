package main;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Cliente {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        try (Socket socket = new Socket(host, puerto);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter salida = new PrintWriter(new OutputStreamWriter(
                     socket.getOutputStream(), StandardCharsets.UTF_8), true)) {
            System.out.println("Conectado a " + host + ":" + puerto);
            System.out.println("REGISTER <user> <pwd> | LOGIN <user> <pwd>");
            System.out.println("SAY <mensaje> | SETNICK <nombre> | EXIT");

            // Este hilo recibe mensajes mientras el hilo principal lee el teclado.
            Thread receptor = new Thread(() -> {
                try {
                    String respuesta;
                    while ((respuesta = entrada.readLine()) != null) {
                        System.out.println(respuesta);
                    }
                    System.out.println("El servidor ha cerrado la conexión. Escribe EXIT para salir.");
                } catch (IOException e) {
                    if (!socket.isClosed()) System.out.println("Se perdió la conexión.");
                }
            });
            receptor.setDaemon(true);
            receptor.start();

            BufferedReader teclado = new BufferedReader(new InputStreamReader(
                    System.in, StandardCharsets.UTF_8));
            String comando;
            while ((comando = teclado.readLine()) != null) {
                salida.println(comando);
                if (comando.equals("EXIT")) break;
            }
        } catch (IOException e) {
            System.out.println("No se pudo conectar o comunicar: " + e.getMessage());
        }
    }
}
