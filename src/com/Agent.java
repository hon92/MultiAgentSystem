/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.actions.AckAction;
import com.actions.Action;
import com.actions.ActionResult;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class Agent extends Observable
{
    private static final String MSG_PATTERN = "(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3}):(\\d+)\\s(.+)";
    private static final String ACK_MESSAGE = "(ack)\\s(.+)";
    private static final String RESULT_ACK_MESSAGE = "(ack)\\s\"(.+)\"\\s(.+)";
    private static final String FILE_FOLDER_NAME = "ReceivedFiles";

    private final Pattern ackPattern = Pattern.compile(ACK_MESSAGE);
    private final Pattern resultAckPattern = Pattern.compile(RESULT_ACK_MESSAGE);
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
    private final List<String> knowledges = new ArrayList<>();

    public Agent(String name, int port, String ip)
    {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.agentsDb = new AgentDb();
    }

    public String getFilesFolderPath()
    {
        return FILE_FOLDER_NAME + "-" + getIp() + "-" + getPort();
    }

    public void addAction(Action action)
    {
        action.setAgent(this);
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
        List<String> params = getSenderParameters(msg);
        if (params == null)
        {
            Logger.getInstance().log(Logger.Level.Error,
                    "Unknown message format: " + msg);
            return;
        }

        String senderIp = params.get(0);
        String senderPort = params.get(1);
        String senderMessage = params.get(2);
        String prefix = params.get(3);
        int senderPortNumber = Integer.parseInt(senderPort);

        String debugSeeStr = String.format("%s from %s: %s",
                getName() + ":" + getPort(), senderIp + ":" + senderPort,
                senderMessage);

        Logger.getInstance().log(Logger.Level.Info, debugSeeStr);
        addOtherAgent(senderIp, senderPortNumber);
        try
        {
            if (prefix.equals("ack"))
            {
                handleAck(senderIp, senderPortNumber, senderMessage);
                return;
            }
            Action action = findAction(prefix);
            if (action != null)
            {
                handleAction(action, senderIp, senderPortNumber, senderMessage);
            }
            else
            {
                displayMessage(senderMessage, senderIp + ":" + senderPort);
                sendAck(senderIp, senderPortNumber, senderMessage);
            }
        }
        catch (Exception ex)
        {
            Logger.getInstance().log(Logger.Level.Error, ex.getMessage());
        }
    }

    private void handleAction(Action action,
            String receiverIp,
            int receiverPort,
            String senderMessage)
    {
        action.handle(receiverIp, receiverPort, senderMessage);
    }

    private void handleAck(String senderIp, int senderPort, String senderMessage)
            throws Exception
    {
        Matcher m = ackPattern.matcher(senderMessage);
        Matcher resM = resultAckPattern.matcher(senderMessage);

        String prefix = "";
        if (resM.find() && resM.groupCount() == 3)
        {
            // result ack message
            prefix = getMessagePrefix(resM.group(2));
        }
        else if (m.find() && m.groupCount() == 2)
        {
            // normal ack message
            prefix = getMessagePrefix(m.group(2));
        }
        Action action = findAction(prefix);
        if (action != null)
        {
            action.performAck(senderIp, senderPort, senderMessage);
        }
        displayMessage(senderMessage, senderIp + ":" + senderPort);
    }

    private void addOtherAgent(String agentIp, int agentPort)
    {
        if (!agentIp.equals(getIp()) || agentPort != getPort())
        {
            boolean addedToDb = agentsDb.addAgent(agentIp, agentPort);
            if (addedToDb)
            {
                Logger.getInstance().log(Logger.Level.Info,
                        String.format("Agent %s:%d was added to DB",
                                agentIp,
                                agentPort));
            }
        }
    }

    private List<String> getSenderParameters(String message)
    {
        Pattern p = Pattern.compile(MSG_PATTERN);
        Matcher m = p.matcher(message);
        final int paramLength = 3;
        if (m.find() && m.groupCount() == paramLength)
        {
            List<String> params = new ArrayList<>();
            params.add(m.group(1)); // sender ip
            params.add(m.group(2)); // sender port
            String senderMessage = m.group(3);
            params.add(senderMessage); // sender message
            params.add(getMessagePrefix(senderMessage)); // sender msg prefix
            return params;
        }
        return null;
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
        if (!ip.equals(getIp()) || port != getPort())
        {
            ActionResult ar = new AckAction(getIp(),
                    getPort(),
                    ip,
                    port).perform(msg);
            if (!ar.isPerformed())
            {
                Logger.getInstance().log(Logger.Level.Error, "ACK send failed");
            }
        }
        else
        {
            see(getIp() + ":" + getPort() + " ack " + msg);
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

    public synchronized void displayMessage(String message, String from)
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
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 64);
                    SocketAddress socketAddress = serverChannel.receive(buffer);
                    String receivedMessage = readBuffer(buffer);
                    List<String> receiveParams = getSenderParameters(receivedMessage);
                    if (receiveParams == null)
                    {
                        continue;
                    }
                    String senderMessage = receiveParams.get(2);
                    String ackMessage = "ack " + senderMessage;
                    serverChannel.send(ByteBuffer.wrap(ackMessage.getBytes()),
                            socketAddress);
                    messages.put(receivedMessage);
                }
                catch (IOException ex)
                {
                    if (running)
                    {
                        Logger.getInstance().log(Logger.Level.Error,
                                ex.getMessage());
                    }
                }
                catch (InterruptedException ex)
                {
                    Logger.getInstance().log(Logger.Level.Error,
                            ex.getMessage());
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
                    Logger.getInstance().log(Logger.Level.Error,
                            ex.getMessage());
                }
            }
        }
    };

    public void addMessage(String message) throws InterruptedException
    {
        messages.put(message);
    }

    public void storeMessage(String message)
    {
        knowledges.add(message);
    }

    public List<String> getKnowledges()
    {
        return knowledges;
    }
}
