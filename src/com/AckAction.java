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
public class AckAction implements Action
{
    private final String ip;
    private final int port;

    public AckAction(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void perform()
    {
        Socket s = null;
        try
        {
            s = new Socket(ip, port);
            String msg = String.format("ack %s %d", ip, port);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())))
            {
                bw.write(msg);
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
