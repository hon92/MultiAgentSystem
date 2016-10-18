/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Honza
 */
public class SendAgentMessage implements Action
{
    private final String fromAddress;
    private final String toAddress;
    private final int fromPort;
    private final int toPort;
    private final String msg;

    public SendAgentMessage(String fromAddress, String toAddress, int fromPort, int toPort, String msg)
    {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.fromPort = fromPort;
        this.toPort = toPort;
        this.msg = msg;
    }

    @Override
    public void perform()
    {
        Socket s = null;
        try
        {
            s = new Socket(toAddress, toPort);
            String m = String.format("receive %s %d %s", fromAddress, fromPort, msg);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())))
            {
                bw.write(m);
                bw.flush();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(SendAgentMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(SendAgentMessage.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

}
