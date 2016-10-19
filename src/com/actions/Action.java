/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
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
public abstract class Action
{
    private final String prefix;
    private final int paramsLength;
    private Agent agent;

    public Action(String prefix, int paramsLength, Agent agent)
    {
        this.prefix = prefix;
        this.paramsLength = paramsLength;
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

    public int getParamsLength()
    {
        return paramsLength;
    }

    public abstract void perform(String msg) throws Exception;

    protected boolean sendMessageToAddress(String msg, String ip, int port)
    {
        Socket s = null;

        try
        {
            Agent a = getAgent();
            String aip = a.getIp();
            int aport = a.getPort();
            s = new Socket(ip, port);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())))
            {
                bw.write(msg);
                bw.flush();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
            return false;
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
                    Logger.getLogger(Action.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return true;
    }

}
