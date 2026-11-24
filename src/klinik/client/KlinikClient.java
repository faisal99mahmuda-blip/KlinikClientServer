package klinik.client;

import klinik.common.*;
import java.io.*;
import java.net.*;

public class KlinikClient {

    private static KlinikClient instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public static KlinikClient getInstance() {
        if (instance == null) {
            instance = new KlinikClient();
        }
        return instance;
    }

    public boolean connect() {
        try {
            socket = new Socket("localhost", 5000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized Response kirim(Request req) {
        try {
            out.writeObject(req);
            out.flush();
            return (Response) in.readObject();
        } catch (Exception e) {
            return new Response(false, "Server tidak terhubung / mati. Jalankan KlinikServer dulu!", null);
        }
    }
}
