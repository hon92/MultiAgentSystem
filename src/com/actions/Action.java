/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
import com.Logger;
import com.Parameter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;


/**
 *
 * @author Honza
 */
public abstract class Action
{
    private final String prefix;
    protected Agent agent;
    protected Parameter parameter;
    private Parameter lastParameter;

    public Action(String prefix)
    {
        this.prefix = prefix;
    }

    protected final void addNextParameter(Parameter parameter)
    {
        if (this.parameter == null)
        {
            this.parameter = parameter;
            lastParameter = parameter;
        }
        else
        {
            lastParameter.setNext(parameter);
            lastParameter = parameter;
        }
    }

    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }

    public Agent getAgent()
    {
        return agent;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public final void handle(String receiverIp, int receiverPort, String message)
    {
        ActionResult actionResult;
        if (parameter == null)
        {
            actionResult = new ActionResult();
        }
        else
        {
            actionResult = perform(message);
        }

        if (actionResult.isPerformed())
        {
            response(receiverIp, receiverPort, message, actionResult);
        }
    }

    public void response(String receiverIp,
            int receiverPort,
            String message,
            ActionResult actionResult)
    {
        if (actionResult.hasResult())
        {
            new Thread(() ->
            {
                for (String resultMsg : actionResult.getResultMessages())
                {
                    try
                    {
                        Thread.sleep(3);
                    }
                    catch (InterruptedException ex)
                    {
                        Logger.getInstance().log(Logger.Level.Error,
                                ex.getMessage());
                    }
                    sendResultAckMessage(agent.getIp(),
                            agent.getPort(),
                            message,
                            resultMsg,
                            receiverIp,
                            receiverPort);
                }
            }, "UDP-sendingThread").start();
        }
        else
        {
            sendAckMessage(agent.getIp(),
                    agent.getPort(),
                    receiverIp,
                    receiverPort,
                    message);
        }
    }

    public ActionResult perform(String message)
    {
        if (parameter == null)
        {
            return new ActionResult();
        }
        return parameter.match(message);
    }

    public void performAck(String ip, int port, String message)
    {

    }

    protected boolean sendMessageToAddress(String sourceIp,
            int sourcePort,
            String message,
            String targetIp,
            int targetPort)
    {
        String m = String.format("%s:%d %s", sourceIp, sourcePort, message);
        byte[] msgBytes = m.getBytes();

        try (DatagramSocket datagramSocket = new DatagramSocket())
        {
            DatagramPacket datagramPacket = new DatagramPacket(msgBytes,
                    msgBytes.length,
                    new InetSocketAddress(targetIp, targetPort));
            datagramSocket.send(datagramPacket);
            return true;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    private int sendReceive(String targetIp,
            int targetPort,
            byte[] sendBytes,
            byte[] receiveBytes)
    {
        final int RESEND_COUNT = 2;
        final int TIMEOUT = 100;
        int currentResendCount = 0;

        try
        {
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(sendBytes,
                    sendBytes.length,
                    new InetSocketAddress(targetIp, targetPort));
            datagramSocket.send(datagramPacket);

            while (true)
            {
                DatagramPacket dp = new DatagramPacket(receiveBytes,
                        receiveBytes.length);
                try
                {
                    datagramSocket.setSoTimeout(TIMEOUT);
                    datagramSocket.receive(dp);
                    if (sendBytes[sendBytes.length - 1]
                            == dp.getData()[dp.getLength() - 1])
                    {
                        return dp.getLength();
                    }
                }
                catch (SocketTimeoutException ex)
                {
                    currentResendCount++;
                    if (currentResendCount >= RESEND_COUNT)
                    {
                        Logger.getInstance().log(Logger.Level.Warning,
                                "TIMEOUT , PACKET IS LOST");
                        return -1;
                    }
                }
            }
        }

        catch (IOException ex)
        {
            Logger.getInstance().log(Logger.Level.Error, ex.getMessage());
            return -1;
        }
    }

    public boolean sendAckMessage(String sourceIp,
            int sourcePort,
            String targetIp,
            int targetPort,
            String message)
    {
        return sendMessageToAddress(sourceIp,
                sourcePort,
                "ack " + message,
                targetIp,
                targetPort);
    }

    public boolean sendResultAckMessage(String sourceIp,
            int sourcePort,
            String message,
            String result,
            String targetIp,
            int targetPort)
    {
        String msg = String.format("\"%s\" %s", message, result);
        return sendAckMessage(sourceIp, sourcePort, targetIp, targetPort, msg);
    }
}
