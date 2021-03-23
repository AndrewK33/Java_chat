package ru.geekbrains.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;

    private List<ClientHandler> clients;

    public Server (int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket (port)) {
            System.out.println("Server started on port 8189");

            while (true){
                System.out.println("Waiting for clients...");
                Socket socket = serverSocket.accept();
                System.out.println("Client joined");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe (ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public void unsubcribe (ClientHandler clientHandler) {
        clients.remove(clientHandler);

    }

    public void broadcastMessage (String message) throws IOException {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }
}
