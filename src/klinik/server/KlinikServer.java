package klinik.server;

import java.net.*;

public class KlinikServer {

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(5000)) {
            System.out.println("=== SERVER KLINIK JALAN di port 5000 ===");
            System.out.println("Menunggu client terhubung...");
            while (true) {
                Socket client = server.accept();
                System.out.println("Client terhubung: " + client.getInetAddress());
                new ClientHandler(client).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
