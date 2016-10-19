/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class AckAction extends Action
{

    private final String PATTERN = "(ack)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)";
    public AckAction(String prefix, int paramsLength, Agent agent)
    {
        super(prefix, paramsLength, agent);
    }

    @Override
    public void perform(String msg) throws Exception
    {
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(msg);
        if (m.find() && m.groupCount() == getParamsLength() + 1)
        {
            String ip = m.group(2);
            String portStr = m.group(3);
            int port = 0;
            try
            {
                port = Integer.parseInt(portStr);
            }
            catch (NumberFormatException e)
            {
                throw new Exception("Invalid port number");
            }
            getAgent().getAgentDb().addAgent(ip, port);
            getAgent().displayMessage("ack from " + ip + " " + port);
        }
        else
        {
            throw new Exception("Invalid arguments for " + getPrefix() + " action");
        }
    }

}
