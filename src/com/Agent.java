/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.actions.AckAction;
import com.actions.Action;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class Agent extends Observable
{
    private static final String MSG_PATTERN = "(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3}):(\\d+)\\s(.+)";
    private String state;
    private final AgentDb agentsDb;
    private DatagramChannel serverChannel;
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

    public void start() throws InterruptedException, IOException
    {
        if (envCheckThread != null)
        {
            envCheckThread.join();
        }
        if (listeningThread != null)
        {
            listeningThread.join();
        }
        running = true;
        serverChannel = DatagramChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        envCheckThread = new Thread(envCheckWorker);
        envCheckThread.setDaemon(true);
        envCheckThread.start();
        listeningThread = new Thread(listeningWorker);
        listeningThread.setDaemon(true);
        listeningThread.start();
    }

    public void stop() throws IOException, InterruptedException
    {
        running = false;
        if (listeningThread != null && listeningThread.isAlive())
        {
            serverChannel.close();
        }

        if (envCheckThread != null && envCheckThread.isAlive())
        {
            messages.put("");
        }
    }
    public void see(String msg)
    {
        Pattern p = Pattern.compile(MSG_PATTERN);
        Matcher m = p.matcher(msg);
        if (m.find() && m.groupCount() == 3)
        {
            String senderIp = m.group(1);
            String senderPort = m.group(2);
            String senderMessage = m.group(3);
            String prefix = getMessagePrefix(senderMessage);
            int senderPortNumber = Integer.parseInt(senderPort);

            try
            {
                if (!prefix.equals("ack"))
                {
                    sendAck(senderIp, senderPortNumber, senderMessage);
                }
                else
                {
                    System.err.println("save to db" + senderIp + ":" + senderPort);
                    agentsDb.addAgent(senderIp, senderPortNumber);
                }
            }
            catch (Exception ex)
            {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }

            Action action = findAction(prefix);
            if (action != null)
            {
                try
                {
                    action.perform(senderIp, senderPortNumber, senderMessage);
                    return;
                }
                catch (Exception ex)
                {
                    Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            displayMessage(senderMessage, senderIp + ":" + senderPort);
        }
        else
        {
            System.err.println("Unknown message format: " + msg);
        }
    }

    private Action findAction(String prefix)
    {
        for (Action a : availableActions)
        {
            if (a.getPrefix().equals(prefix))
            {
                return a;
            }
        }
        return null;
    }

    private void sendAck(String ip, int port, String msg) throws Exception
    {
        if (!ip.equals(getIp()) && port != getPort())
        {
            new AckAction(getIp(), getPort()).perform(ip, port, msg);
        }
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

    public void displayMessage(String message, String from)
    {
        final String MSG = "Agent '%s' get msg from '%s': '%s'";
        setChanged();
        notifyObservers(String.format(MSG, name, from, message));
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
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketAddress socketAddress = serverChannel.receive(buffer);
                    String newMsg = readBuffer(buffer);
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

    private String readBuffer(ByteBuffer buffer) throws IOException
    {
        buffer.flip();
        int limit = buffer.limit();
        byte[] bytes = new byte[limit];
        buffer.get(bytes, 0, limit);
        return new String(bytes);
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
