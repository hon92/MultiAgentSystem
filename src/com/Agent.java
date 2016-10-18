/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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

    public String getName()
    {
        return this.name;
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
        Action action = next(msg, state);
        if (action != null)
        {
            action.perform();
        }
    }

    private Action next(String message, String prevState)
    {
        final String MSG = "Agent '%s' get msg: '%s'";
        setChanged();

        if (message.startsWith("send"))
        {
            // send 127.0.0.1 25000 message
            String[] s = message.split(" ");
            if (s.length >= 4)
            {
                String agentAddress = s[1];
                String p = s[2];
                int agentPort;
                String msg = s[3];
                for (int i = 4; i < s.length; i++)
                {
                    msg += " " + s[i];
                }
                try
                {
                    agentPort = Integer.parseInt(p);
                    setState("sending");
                    notifyObservers(String.format(MSG, name, String.format("sending msg: '%s' on agent on %s:%d",
                            msg,
                            agentAddress,
                            agentPort)));
                    return new SendAgentMessage(ip,
                            agentAddress,
                            port,
                            agentPort,
                            msg);
                }
                catch (NumberFormatException ex)
                {
                    setState(prevState);
                    return null;
                }
            }
        }

        if (message.startsWith("receive"))
        {
            // receive 127.0.0.1 25000 message
            String[] s = message.split(" ");
            if (s.length >= 4)
            {
                String agentAddress = s[1];
                String p = s[2];
                int agentPort;
                String msg = s[3];
                for (int i = 4; i < s.length; i++)
                {
                    msg += " " + s[i];
                }
                try
                {
                    agentPort = Integer.parseInt(p);
                    setState("receive");
                    notifyObservers(String.format(MSG, name, String.format("received msg: '%s' from agent on %s:%d",
                            msg,
                            agentAddress,
                            agentPort)));
                    return new AckAction(agentAddress, agentPort);
                }
                catch (NumberFormatException ex)
                {
                    setState(prevState);
                    return null;
                }
            }
        }

        if (message.startsWith("ack"))
        {
            //ack 127.0.0.1 2000
            String[] s = message.split(" ");
            if (s.length == 3)
            {
                String agentAddress = s[1];
                String p = s[2];
                int agentPort;
                try
                {
                    agentPort = Integer.parseInt(p);
                    setState("ack");
                    notifyObservers(String.format(MSG, name, String.format("ack from %s:%d", agentAddress, agentPort)));
                    return null;
                }
                catch (NumberFormatException ex)
                {
                    setState(prevState);
                    return null;
                }
            }
        }

        setState(prevState);
        notifyObservers(String.format(MSG, name, message));
        return null;
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
                    agentsDb.addAgent(socket);
                    String newMsg = readSocket(socket);
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
}
