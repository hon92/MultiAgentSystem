/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.actions.Action;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Honza
 */
public class Agent extends Observable
{
    private String state;
    private final AgentDb agentsDb;
    private ServerSocket serverSocket;
    private final String name;
    private final String ip;
    private final int port;
    private Thread listeningThread = null;
    private Thread envCheckThread = null;
    private boolean running = false;
    private final BlockingQueue<String> messages = new ArrayBlockingQueue<>(1000);
    private final List<Action> availableActions = new ArrayList<>();
    private final List<String> savedMessages = new ArrayList<>();

    public Agent(String name, int port, String ip)
    {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.agentsDb = new AgentDb();
        setState("init");
    }

    private void setState(String newState)
    {
        this.state = newState;
        //System.out.println("Agent state changed to " + newState);
    }

    public void addAction(Action action)
    {
        availableActions.add(action);
    }

    public String getName()
    {
        return this.name;
    }

    public int getPort()
    {
        return port;
    }

    public String getIp()
    {
        return ip;
    }

    public AgentDb getAgentDb()
    {
        return agentsDb;
    }

    public void start() throws IOException
    {
        if (envCheckThread != null)
        {
            try
            {
                envCheckThread.join();
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (listeningThread != null)
        {
            try
            {
                listeningThread.join();
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        running = true;
        serverSocket = new ServerSocket(port);
        envCheckThread = new Thread(envCheckWorker);
        envCheckThread.setDaemon(true);
        envCheckThread.start();
        listeningThread = new Thread(listeningWorker);
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    public void stop()
    {
        running = false;
        if (listeningThread != null && listeningThread.isAlive())
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (envCheckThread != null && envCheckThread.isAlive())
        {
            try
            {
                messages.put("exit");
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void see(String msg)
    {
        String prefix = getMessagePrefix(msg);

        for (Action a : availableActions)
        {
            if (a.getPrefix().equals(prefix))
            {
                try
                {
                    a.perform(msg);
                }
                catch (Exception ex)
                {
                    System.err.println(ex.getMessage());
                }
                return;
            }
        }

        displayMessage(msg);
    }

    private String getMessagePrefix(String msg)
    {
        int i = msg.indexOf(" ");
        if (i > -1)
        {
            return msg.substring(0, i);
        }
        else
        {
            return msg;
        }
    }

    public void displayMessage(String message)
    {
        final String MSG = "Agent '%s' get msg: '%s'";
        setChanged();
        notifyObservers(String.format(MSG, name, message));
    }

    private final Runnable listeningWorker = new Runnable()
    {
        @Override
        public void run()
        {
            while (running)
            {
                try
                {
                    Socket socket = serverSocket.accept();
                    String newMsg = readSocket(socket);
                    InetAddress localAddress = socket.getInetAddress();
                    int localPort = socket.getLocalPort();
                    messages.put(newMsg);
                }
                catch (IOException ex)
                {
                    if (running)
                    {
                        Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    private String readSocket(Socket socket) throws IOException
    {
        StringBuilder msgBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {
            String line = "";
            while ((line = br.readLine()) != null)
            {
                msgBuilder.append(line);
            }
        }
        return msgBuilder.toString();
    }

    private final Runnable envCheckWorker = new Runnable()
    {
        @Override
        public void run()
        {
            while (running)
            {
                try
                {
                    String msg = messages.take();
                    see(msg);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    };

    public void addMessage(String message) throws InterruptedException
    {
        messages.put(message);
    }

    public void saveMessage(String message)
    {
        savedMessages.add(message);
    }

}
