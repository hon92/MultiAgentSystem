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
    public StoreAction(String prefix, int paramsLength, Agent agent)
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
            String message = m.group(2);
            getAgent().saveMessage(message);
            getAgent().displayMessage("store message: " + message);
        }
        else
        {
            throw new Exception("Invalid arguments for " + getPrefix() + " action");
        }
    }

}
