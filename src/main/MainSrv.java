package main;
 
import java.io.IOException;
 
import servidor.Servidor;
 
public class MainSrv {
 
    public static void main(String[] args) {
        // Creamos el servidor en el mismo puerto que usa el cliente.
        Servidor srv = new Servidor(40000);
        
        try {
            // Primero abrimos el puerto.
            srv.initConnections();
            // El nombre de cada persona se recibe mediante LOGIN.
            srv.pedirNombres();
            // Nos quedamos esperando conexiones.
            srv.bucleRun();
        } catch (IOException e) {
            System.err.println("ERROR FATAL");
            e.printStackTrace();
        } finally {
            // Cerramos el servidor si termina o si ocurre un error.
            try {
                srv.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            System.out.println("[LOG] Goodbye!");
        }
    }
 
}