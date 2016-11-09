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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
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
    private final Queue<AckMessage> waitingMessages = new ArrayDeque<>();

    public Agent(String name, int port, String ip)
    {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.agentsDb = new AgentDb();
        setState("init");
    }

    public boolean isRunning()
    {
        return running;
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
        System.err.println(getName() + ":" + msg);

        List<String> params = getSenderParameters(msg);
        if (params == null)
        {
            System.err.println("Unknown message format: " + msg);
            return;
        }

        String senderIp = params.get(0);
        String senderPort = params.get(1);
        String senderMessage = params.get(2);
        String prefix = params.get(3);
        int senderPortNumber = Integer.parseInt(senderPort);

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
                //checkAckMessage(new AckMessage(senderMessage, senderIp, senderPortNumber, senderMessage, System.currentTimeMillis()));
                //sendAck(senderIp, senderPortNumber, senderMessage);
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleAction(Action action, String senderIp, int senderPort, String senderMessage) throws Exception
    {
        ActionResult ar = action.perform(senderIp, senderPort, senderMessage);
        
        if (ar.isPerformed())
        {
            if (ar.hasResult())
            {
                displayMessage("ack \"" + senderMessage + "\" " + ar.getResultMessage(), senderIp + ":" + senderPort);
                //System.out.println(getName() + " waiting for result ack:" + ar.getResultMessage());
                //waitingMessages.add(new AckMessage(senderMessage, senderIp, senderPort, ar.getResultMessage(), System.currentTimeMillis()));
                //sendResultAck(senderIp, senderPort, senderMessage, ar.getResultMessage());
            }
            else
            {
                displayMessage("ack " + senderMessage, senderIp + ":" + senderPort);
                //System.out.println(getName() + " waiting for ack:" + "ack " + senderMessage);
                //waitingMessages.add(new AckMessage(senderMessage, senderIp, senderPort, senderMessage, System.currentTimeMillis()));
                //sendAck(senderIp, senderPort, senderMessage);
            }
        }
        else
        {
            System.err.println("Action result error");
        }

//        if (action.hasResult())
//        {
//            waitingMessages.add(new AckMessage(senderMessage, senderIp, senderPort, "window", System.currentTimeMillis()));
//            sendResultAck(senderIp, senderPort, senderMessage, "window");
//        }
//        else
//        {
//            waitingMessages.add(new AckMessage(senderMessage, senderIp, senderPort, "", System.currentTimeMillis()));
//            sendAck(senderIp, senderPort, senderMessage);
//        }
    }

    private void handleAck(String senderIp, int senderPort, String senderMessage) throws Exception
    {
        String resultMsgRegex = "(ack)\\s\"(.+)\"\\s(.+)";
        final int paramLength = 2;
        Pattern pa = Pattern.compile(resultMsgRegex);
        Matcher ma = pa.matcher(senderMessage);

        if (ma.find() && ma.groupCount() == paramLength + 1)
        {
            String resultMessage = ma.group(ma.groupCount());
            //checkAckMessage(new AckMessage(ma.group(paramLength), senderIp, senderPort, resultMessage));
            //System.err.println("RESULT MSG: " + resultMessage);
            String ackSourceMessage = ma.group(paramLength);
            String prefix = getMessagePrefix(ackSourceMessage);
//            Action a = findAction(prefix);
//            if (a != null)
//            {
//                a.perform(senderIp, senderPort, ackSourceMessage);
//            }
        }
        else
        {
            String seeMsg = senderMessage.substring(4);
            //checkAckMessage(new AckMessage(seeMsg, senderIp, senderPort, seeMsg, System.currentTimeMillis()));
            //System.err.println("ACK SENDER MESSAGE: " + seeMsg);
            String ackSourceMessage = seeMsg;
            String prefix = getMessagePrefix(ackSourceMessage);

//            Action a = findAction(prefix);
//            if (a != null)
//            {
//                a.perform(senderIp, senderPort, ackSourceMessage);
//            }
        }
        //System.err.println("save to db" + senderIp + ":" + senderPort);
        agentsDb.addAgent(senderIp, senderPort);
        displayMessage(senderMessage, senderIp + ":" + senderPort);
    }

    private void checkAckMessage(AckMessage ackMessage)
    {
        AckMessage lastWaitingMessage = waitingMessages.peek();
        if (lastWaitingMessage != null)
        {
            if (lastWaitingMessage.equals(ackMessage))
            {
                System.out.println(getName() + " ack complete: " + lastWaitingMessage.messageToResend + " res: " + lastWaitingMessage.resultMessage);
                waitingMessages.poll();
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

    private void sendResultAck(String ip, int port, String msg, String result) throws Exception
    {
        sendAck(ip, port, "\"" + msg + "\" " + result);
    }

    private void sendAck(String ip, int port, String msg) throws Exception
    {
        if (!ip.equals(getIp()) || port != getPort())
        {
            ActionResult ar = new AckAction(getIp(), getPort()).perform(ip, port, msg);
            if (!ar.isPerformed())
            {
                System.err.println("ACK send failed");
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
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketAddress socketAddress = serverChannel.receive(buffer);

                    //Thread.sleep(2000);
                    buffer.flip();

                    byte[] bufferData = new byte[buffer.limit() - 1];
                    buffer.get(bufferData, 0, buffer.limit() - 1);
                    byte indexByte = buffer.get(buffer.limit() - 1);
                    buffer.clear();
                    buffer.put("ack ".getBytes());
                    buffer.put(bufferData);
                    buffer.put(indexByte);
                    buffer.flip();
                    serverChannel.send(buffer, socketAddress);
                    messages.put(new String(bufferData));
//                    if (buffer.limit() == 0)
//                    {
//                        continue;
//                    }
//                    byte[] allBytes = new byte[buffer.limit()];
//                    byte index = buffer.get(buffer.limit() - 1);
//
//                    byte[] msgBytes = new byte[buffer.limit() - 1];
//                    buffer.get(msgBytes, 0, buffer.limit() - 1);
//                    String newMsg = new String(msgBytes);//readBuffer(buffer);
//                    String ackMsg = "ack " + newMsg;
//                    byte[] ackMsgBytes = ackMsg.getBytes();
//
//                    byte[] ackBytes = new byte[ackMsgBytes.length + 1];
//                    for (int i = 0; i < ackMsgBytes.length; i++)
//                    {
//                        ackBytes[i] = ackMsgBytes[i];
//                    }
//                    ackBytes[ackMsgBytes.length] = index;
//                    serverChannel.send(ByteBuffer.wrap(ackBytes), socketAddress);
//                    messages.put(newMsg);
                    //Thread.sleep(1000);

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
