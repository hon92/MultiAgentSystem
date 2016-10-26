/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Honza
 */
public abstract class Action
{
    private final String prefix;
    private final int paramsLength;

    public Action(String prefix, int paramsLength)
    {
        this.prefix = prefix;
        this.paramsLength = paramsLength;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public int getParamsLength()
    {
        return paramsLength;
    }

    public abstract void perform(String senderIp, int senderPort, String msg) throws Exception;

    protected boolean sendMessageToAddress(String senderIp,
            int senderPort,
            String msg,
            String ip,
            int port)
    {
        DatagramChannel channel = null;
        try
        {
            channel = DatagramChannel.open();
            channel.bind(null);

            String m = String.format("%s:%d %s", senderIp, senderPort, msg);
            ByteBuffer buffer = ByteBuffer.wrap(m.getBytes());
            channel.send(buffer, new InetSocketAddress(ip, port));
            return true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (channel != null)
            {
                try
                {
                    channel.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return false;
    }

}
