package ru.geekbrains.march.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private  AuthenticationProvider authenticationProvider;


    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server (int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.authenticationProvider = new InMemoryAuthenticationProvider();
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

    public synchronized void subscribe (ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastMessage("Client " + clientHandler.getUsername() + " is connected to chat");
        broadcastClientsList();
    }

    public synchronized void unsubcribe (ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Client " + clientHandler.getUsername() + " is disconnected from chat");
        broadcastClientsList();
    }

    public synchronized void broadcastMessage (String message)  {
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(receiverUsername)) {
                c.sendMessage("From: " + sender.getUsername() + " Private message: " + message);
                sender.sendMessage("To user: " + receiverUsername + " Pivate message: " + message);
                return;
            }
        }
        sender.sendMessage("It is not possible to send a message to " + receiverUsername + " , user is offline");
    }


    public synchronized boolean isOnline(String username) {
        for (ClientHandler clientHandler: clients) {
            if (clientHandler.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList()  {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        for (ClientHandler c: clients) {
            stringBuilder.append(c.getUsername()).append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(clientsList);
        }

    }

}
