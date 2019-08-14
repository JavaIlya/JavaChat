import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

public class TCPConnection {

        private final Socket socket;
        private final TCPConnectionListener tcpConnectionListener;
        private final Thread thread;
        private final BufferedReader in;
        private final BufferedWriter out;

        public TCPConnection(final TCPConnectionListener eventListener, String ip, int port) throws IOException {
            this(eventListener, new Socket(ip,port));
        }

        public TCPConnection(final TCPConnectionListener eventListener, Socket socket) throws IOException {
            this.tcpConnectionListener = eventListener;
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            thread = new Thread(new Runnable() {
                public void run() {
                        try {
                            tcpConnectionListener.onConnectionReady(TCPConnection.this);
                            while (!thread.isInterrupted()) {
                                String message = in.readLine();
                                if (message == null) disconnect();
                                else
                                eventListener.onReceiveString(TCPConnection.this, message);
                            }
                        } catch (IOException e) {
                            eventListener.onException(TCPConnection.this, e);
                        } finally {
                            eventListener.onDisconnect(TCPConnection.this);
                        }
                }
            });
            thread.start();
        }

        public synchronized void sendString(String value) {
            try {
                out.write( value + "\r\n");
                out.flush();
            } catch (IOException e) {
                tcpConnectionListener.onException(this,e);
                disconnect();
            }
        }

        public synchronized void disconnect() {
            thread.interrupt();
            try {
                this.socket.close();
            } catch (IOException e) {
                tcpConnectionListener.onException(this,e);

            }
        }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + " " + socket.getPort();
    }
}
