package io.github.undefinedmaniac.mcscpserver;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

//Opens a single client MCSCP server and processes commands
public class MCSCPServer extends BukkitRunnable {

    //THREAD-SAFE variable
    private volatile boolean mActive;

    private final JavaPlugin mPlugin;

    private ServerSocket mServer = null;
    private Socket mSocket = null;
    private BufferedReader mBufferedReader = null;

    //THREAD-SAFE object, use synchronized(this) when accessing
    private BufferedWriter mBufferedWriter = null;

    public MCSCPServer(JavaPlugin plugin) {
        mPlugin = plugin;
    }

    @Override
    public void run() {
        mActive = true;
        mainLoop:
        while (mActive) {

            //Try starting the socket server up to 3 times
            int count = 0;
            int maxTries = 3;
            while(true) {
                if (!mActive)
                    break mainLoop;
                if (startSocketServer()) {
                    break;
                } else {
                    if (count++ == maxTries) {
                        logWarningMsg("Unable to start MCSCP server; Stopping");
                        break mainLoop;
                    }
                }
            }

            //Block until we receive a connection
            while (true) {
                if (!mActive)
                    break mainLoop;
                if (waitForConnection())
                    break;
            }
            logInfoMsg("accepted incoming connection from " + mSocket.getInetAddress());

            //Create read and write streams for the client
            if (!mActive || !openSocketStreams()) {
                disconnectFromClient();
                continue;
            }

            //Send our protocol to the client
            if (!mActive || !sendMsgToClient("MCSCP:V1.0")) {
                disconnectFromClient();
                continue;
            }

            //Proceed if we receive "MCSCP:V1.0", otherwise drop
            String response = waitForClientResponse();
            if (!mActive || response == null || !response.equals("MCSCP:V1.0")) {
                disconnectFromClient();
                continue;
            } else {
                sendMsgToClient("HELLO " + mSocket.getInetAddress());
            }

            //Loop while we receive incoming commands
            commandLoop:
            while (true) {
                if (!mActive)
                    break mainLoop;
                String cmd = waitForClientResponse();
                if (cmd == null) {
                    break;
                } else {
                    logInfoMsg("Received command: " + cmd);
                }

                switch (cmd) {
                    case "PING":
                        sendMsgToClient("PONG");
                        break;
                    case "DISCONNECT":
                        sendMsgToClient("BYE");
                        break commandLoop;
                    default:
                        CMDProcessor cmdHandler = new CMDProcessor(cmd, this);
                        cmdHandler.runTask(mPlugin);
                        break;
                }
            }
            disconnectFromClient();
        }
        cancel();
    }

    //THREAD-SAFE
    //Send a message to the client. Return true on success, false on failure
    public synchronized boolean sendMsgToClient(String msg) {
        if (mBufferedWriter == null || mSocket == null || mSocket.isClosed())
            return false;

        int count = 0;
        int maxTries = 3;
        while (true) {
            try {
                mBufferedWriter.write(msg);
                mBufferedWriter.flush();
                return true;
            } catch (IOException e) {
                if (count++ == maxTries) {
                    logWarningMsg("Error: " + e.getMessage() + " when sending data to MCSCP client");
                    return false;
                }
            }
        }
    }

    //THREAD-SAFE
    //Drop all connections and stop server
    public void stop() {
        if (mServer == null || mServer.isClosed())
            return;

        mActive = false;
        try {
            mServer.close();
        } catch (IOException e) {
            logWarningMsg("Error: " + e.getMessage() + " when stopping MCSCP server");
        }
        disconnectFromClient();
    }

    //Disconnect client
    private void disconnectFromClient() {
        if (mSocket == null || mSocket.isClosed())
            return;

        logInfoMsg("Disconnecting client");
        try {
            sendMsgToClient("DISCONNECT");
            mSocket.close();
        } catch (IOException e) {
            logWarningMsg("Error: " + e.getMessage() + " when disconnecting from client");
        }
    }

    //Create a server socket. Does nothing if there is already an operational server socket
    //Return true on success OR if a server socket already exists, return false on failure
    private boolean startSocketServer() {
        if (mServer == null || mServer.isClosed()) {
            try {
                mServer = new ServerSocket(54620);
                logInfoMsg("MCSCP V1.0 Server online on port " + mServer.getLocalPort() + "!");
            } catch (IOException e) {
                logWarningMsg("Error: " + e.getMessage() + " when creating MCSCP server");
                return false;
            }
        }
        return true;
    }

    //Block until a connection is received; return true on success, false on failure
    private boolean waitForConnection() {
        if (mServer == null || mServer.isClosed())
            return false;

        try {
            mSocket = mServer.accept();
            return true;
        } catch (IOException e) {
            //Do not print errors during shutdown; closing the server will throw an IOException here
            if (mActive)
                logWarningMsg("Error: " + e.getMessage() + " when accepting MCSCP connection");
            return false;
        }
    }

    //Open the read and write streams for the current socket; return true on success, false on failure
    private boolean openSocketStreams() {
        if (mSocket == null || mSocket.isClosed())
            return false;

        try {
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            synchronized (this) {
                mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));
            }
            return true;
        } catch (IOException e) {
            logWarningMsg("Error: " + e.getMessage() + "While attempting to open read/write stream to MCSCP client");
            return false;
        }
    }

    //Block until a line is read from the client; return the message on success, null on failure
    private String waitForClientResponse() {
        if (mBufferedWriter == null || mSocket == null || mSocket.isClosed())
            return null;

        try {
            return mBufferedReader.readLine();
        } catch (IOException e) {
            //Do not print errors during shutdown; closing the socket will throw an IOException here
            if (mActive)
                logWarningMsg("Error: " + e.getMessage() + " when reading data from MCSCP client");
            return null;
        }
    }

    //Shortcuts for logging messages
    private void logWarningMsg(String msg) {
        Bukkit.getLogger().warning("[MCSCP V1.0 Server] " + msg);
    }

    private void logInfoMsg(String msg) {
        Bukkit.getLogger().info("[MCSCP V1.0 Server] " + msg);
    }
}
