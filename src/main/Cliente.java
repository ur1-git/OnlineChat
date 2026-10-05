package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static void iniciar(Scanner teclado) {
        // localhost significa que el servidor está en este ordenador.
        // El puerto debe ser el mismo que usa el servidor.
        try (Socket socket = new Socket("localhost", 40000)) {
            // entrada lee lo que manda el servidor.
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));

            // salida envía líneas al servidor.
            // true hace que cada println se envíe inmediatamente.
            PrintWriter salida = new PrintWriter(
                    new java.io.OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            // Necesitamos recibir mensajes mientras escribimos.
            // Por eso un segundo hilo se encarga de escuchar al servidor.
            Receptor receptor = new Receptor(entrada);
            receptor.setDaemon(true); // No impide terminar al salir del cliente.
            receptor.start(); // start pone en marcha el método run del hilo.

            System.out.println("REGISTER <user> <pwd>");
            System.out.println("LOGIN <user> <pwd>");
            System.out.println("SAY <mensaje>");
            System.out.println("SETNICK <nombre>");
            System.out.println("EXIT");

            // El hilo principal solo se ocupa del teclado.
            while (teclado.hasNextLine()) {
                String comando = teclado.nextLine();
                salida.println(comando);
                if (comando.equals("EXIT")) {
                    break; // Sale del bucle y se cierra el socket.
                }
            }
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
}

// Esta clase tiene una única tarea: mostrar las líneas que llegan.
class Receptor extends Thread {
    private BufferedReader entrada;

    public Receptor(BufferedReader entrada) {
        this.entrada = entrada;
    }

    @Override
    public void run() {
        try {
            String mensaje = entrada.readLine();
            // readLine devuelve null cuando el servidor cierra la conexión.
            while (mensaje != null) {
                System.out.println(mensaje);
                mensaje = entrada.readLine();
            }
            System.out.println("Conexión cerrada. Escribe EXIT para terminar.");
        } catch (IOException e) {
            // También llegamos aquí si cerramos el socket al escribir EXIT.
        }
    }
}
