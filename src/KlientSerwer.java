import java.io.*;
import java.net.*;

public class KlientSerwer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())
            ) {
                String request = (String) inputStream.readObject();
                if (request.equals("getProdukt")) {
                    int produktId = inputStream.readInt();
                    Produkt produkt = ProduktDAO.getProdukt(produktId, 1); // Przykładowe wywołanie DAO z uprawnieniami admina
                    outputStream.writeObject(produkt);
                } else if (request.equals("dodajProdukt")) {
                    Produkt produkt = (Produkt) inputStream.readObject();
                    ProduktDAO.dodajProdukt(produkt, 1); // Przykładowe wywołanie DAO z uprawnieniami admina
                } else {
                    outputStream.writeObject("Nieznane żądanie");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
