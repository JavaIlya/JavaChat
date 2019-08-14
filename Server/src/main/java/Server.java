import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Server implements TCPConnectionListener {
    private ArrayList<TCPConnection> connections = new ArrayList<>();
    private static final int MAXIMUM_USERS = 3;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        try {
            Database.Connect();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to database...");
        }
        System.out.println("Server running ... ");

        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Database.Disconnect();
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        if (connections.size() == MAXIMUM_USERS) {
            tcpConnection.sendString("Maximum number of connected users exceeded.");
            tcpConnection.disconnect();
        } else {
            connections.add(tcpConnection);
            tcpConnection.sendString("Server: " + tcpConnection + " connected");

            ArrayList<String> lastMessages = Database.getLastMessages();

            if (lastMessages != null) {
                final int size = lastMessages.size();

                for (int i = 0; i < size; i++) {
                    tcpConnection.sendString(lastMessages.get(i));
                }
            }
        }

    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendMessageToUsers(tcpConnection,"Server: " + tcpConnection + " has been disconnected");
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        e.printStackTrace();
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String message) {
        if (!message.isEmpty()) {
            Database.addMessage(message);
            sendMessageToUsers(tcpConnection, message);
        }
    }

    public void sendMessageToUsers(TCPConnection tcpConnection ,String message) {
        final int size = connections.size();
        String currentTime = new Timestamp(System.currentTimeMillis()).toString();
        for (int i = 0; i < size; i++) {
            if (!connections.get(i).equals(tcpConnection))  connections.get(i).sendString("User: " + currentTime + ": " + message); // do not send ur message to urself
        }
    }

}
