package main;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // Partimos del main vacío del proyecto original.
        // Main abre un cliente. MainSrv abre el servidor.
        Scanner teclado = new Scanner(System.in);
        // El cliente usa este teclado para leer los comandos.
        Cliente.iniciar(teclado);
    }
}
