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
public class StoreAction extends Action
{
    private final String PATTERN = "(store)\\s(.+)";
    private final Agent agent;

    public StoreAction(Agent agent)
    {
        super("store", 1);
        this.agent = agent;
    }

    @Override
    public void perform(String senderIp, int senderPort, String msg) throws Exception
    {
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(msg);
        if (m.find() && m.groupCount() == getParamsLength() + 1)
        {
            String message = m.group(2);
            agent.saveMessage(message);
            String storeMsg = String.format("Agent '%s' push to store '%s'",
                    agent.getName(), message);
            agent.displayMessage(storeMsg, senderIp + ":" + senderPort);
        }
        else
        {
            throw new Exception("Invalid arguments for " + getPrefix() + " action");
        }
    }

}
