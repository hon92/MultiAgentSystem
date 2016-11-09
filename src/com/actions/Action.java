/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public abstract class Action
{

    private final String prefix;
    private final int paramsLength;
    private final String regex;
    private final boolean hasResult;
    //private static int MSG_INDEX = 0;

    public Action(String prefix, int paramsLength, String regex, boolean hasResult)
    {
        this.prefix = prefix;
        this.paramsLength = paramsLength;
        this.regex = regex;
        this.hasResult = hasResult;
    }

    public List<String> getMessageParameters(String message)
    {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);

        if (m.find() && m.groupCount() == getParamsLength() + 1)
        {
            List<String> parameters = new ArrayList<>();
            for (int i = 2; i < getParamsLength() + 2; i++)
            {
                parameters.add(m.group(i));
            }
            return parameters;
        }
        return null;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public int getParamsLength()
    {
        return paramsLength;
    }

    public boolean hasResult()
    {
        return hasResult;
    }

    public abstract ActionResult perform(String sourceIp, int sourcePort, String message) throws Exception;

    protected boolean sendMessageToAddress(String sourceIp,
            int sourcePort,
            String message,
            String targetIp,
            int targetPort)
    {
//        try
//        {
        String m = String.format("%s:%d %s", sourceIp, sourcePort, message);
        //ByteBuffer buffer = ByteBuffer.wrap(m.getBytes());
        //byte i = new Integer(MSG_INDEX).byteValue();
        //buffer.put(i);
        //MSG_INDEX++;

        //ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
        //byte[] bytes = new byte[buffer.limit()];
        //buffer.get(bytes, 0, buffer.limit());
        byte[] msgBytes = m.getBytes();
        //byte index = new Integer(MSG_INDEX).byteValue();
        //MSG_INDEX++;
        //byte[] sendBytes = new byte[msgBytes.length + 1];
        //System.arraycopy(msgBytes, 0, sendBytes, 0, msgBytes.length);
        //sendBytes[msgBytes.length] = index;

        byte[] receiveBytes = new byte[1024];
        int receivedBytes = sendReceive(targetIp, targetPort, msgBytes, receiveBytes);
        if (receivedBytes != -1)
        {
            byte[] finalReceivedBytes = new byte[receivedBytes];
            System.arraycopy(receiveBytes, 0, finalReceivedBytes, 0, receivedBytes);
            System.out.println("delivered:" + new String(finalReceivedBytes));
            return true;
        }
        else
        {
            return false;
        }

//            final DatagramChannel channel = DatagramChannel.open();
//            channel.bind(null);
//            String m = String.format("%s:%d %s", sourceIp, sourcePort, message);
//            Integer index = this.MSG_INDEX;
//            int msgLen = m.getBytes().length;
//            byte[] sendBytes = new byte[msgLen + 1];
//            byte[] msgBytes = m.getBytes();
//            for (int i = 0; i < msgLen; i++)
//            {
//                sendBytes[i] = msgBytes[i];
//            }
//            sendBytes[msgLen] = index.byteValue();
//            ByteBuffer buffer = ByteBuffer.wrap(sendBytes);
//            System.out.println("sending: " + new String(msgBytes) + " number " + index);
//            channel.send(buffer, new InetSocketAddress(targetIp, targetPort));
//            boolean waiting = true;
//
//            Timer timer = new Timer(10, new ActionListener()
//            {
//                @Override
//                public void actionPerformed(ActionEvent e)
//                {
//                    try
//                    {
//                        //resend
//                        if (index < MSG_INDEX)
//                        {
//                            System.out.println("resend: " + new String(msgBytes) + " number " + index);
//                            channel.send(buffer, new InetSocketAddress(targetIp, targetPort));
//                        }
//                    }
//                    catch (IOException ex)
//                    {
//                        Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            });
//            timer.start();
//
//            ByteBuffer b = ByteBuffer.allocate(1024);
//            channel.receive(b);
//            timer.stop();
//            b.flip();
//            byte indexByte = b.get(b.limit() - 1);
//            if (index == (int) indexByte)
//            {
//                System.out.println("accepted ack for number " + index);
//                MSG_INDEX++;
//            }
//            else
//            {
//                return false;
//            }
//            byte[] bytes = new byte[b.limit() - 1];
//            b.get(bytes, 0, b.limit() - 1);
//            System.out.println("received: " + new String(bytes));
//            channel.close();
//            return true;
//        }
//        catch (IOException ex)
//        {
//            return false;
//        }
    }

    private int sendReceive(String targetIp, int targetPort, byte[] sendBytes, byte[] receiveBytes)
    {
        final int RESEND_COUNT = 2;
        final int TIMEOUT = 100;
        int currentResendCount = 0;

        try
        {
            DatagramSocket datagramSocket = new DatagramSocket();
            DatagramPacket datagramPacket = new DatagramPacket(sendBytes, sendBytes.length,
                    new InetSocketAddress(targetIp, targetPort));
            datagramSocket.send(datagramPacket);

            while (true)
            {
                DatagramPacket dp = new DatagramPacket(receiveBytes, receiveBytes.length);
                try
                {
                    datagramSocket.setSoTimeout(TIMEOUT);
                    datagramSocket.receive(dp);
                    System.out.println("receive data");
                    if (sendBytes[sendBytes.length - 1] == dp.getData()[dp.getLength() - 1])
                    {
                        return dp.getLength();
                    }

                }
                catch (SocketTimeoutException ex)
                {
                    System.out.println("TIMEOUT");
                    currentResendCount++;
                    if (currentResendCount >= RESEND_COUNT)
                    {
                        System.out.println("TIMEOUT , PACKET IS LOST");
                        return -1;
                    }
                }
            }
        }

        catch (IOException ex)
        {
            Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
}
