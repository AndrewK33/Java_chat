package ru.geekbrains.march.chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String username;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());


        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/login ")) {
                        String[] tokens = msg.split("\\s+");
                        if (tokens.length !=3) {
                            sendMessage("/login_failed Type login and pass ");
                            continue;
                        }
                        String login = tokens[1];
                        String password = tokens[2];
                        String userNickname = server.getAuthenticationProvider().getNicknameByLoginAndPassword(login, password);

                        if (userNickname == null) {
                            sendMessage("/login_failed Typed uncorrected login/pass ");
                            continue;
                        }

                        if (server.isOnline(userNickname)) {
                            sendMessage("/login_failed This account is already used ");
                            continue;
                        }
                        username = userNickname;
                        sendMessage("/login_ok " + username);
                        server.subscribe(this);
                        break;
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith("/")) {
                        executeCommand(msg);
                        continue;
                    }
                    server.broadcastMessage(username + ": " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }


    private void executeCommand(String cmd) {
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s+", 3);
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
            return;
        }
        if (cmd.startsWith("/change_nick ")) {
            String[] tokens = cmd.split("\\s+");
            if(tokens.length !=2){
                sendMessage("Uncorrected command ");
                return;
            }
            String newNickname = tokens[1];
            if (server.isOnline(newNickname)) {
                sendMessage("This nickname is busy ");
                return;
            }
            username = newNickname;
            server.getAuthenticationProvider().changeNickname(username, newNickname);
            username = newNickname;
            sendMessage("Nickname successfully changed for " + newNickname);

            server.broadcastClientsList();

        }
    }


    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            disconnect();
        }

    }


    public void disconnect() {
        server.unsubcribe(this);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
