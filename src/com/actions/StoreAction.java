/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
import java.util.List;

/**
 *
 * @author Honza
 */
public class StoreAction extends Action
{

    private final Agent agent;

    public StoreAction(Agent agent)
    {
        super("store", 1, "(store)\\s(.+)", false);
        this.agent = agent;
    }

    @Override
    public ActionResult perform(String senderIp, int senderPort, String msg) throws Exception
    {
        List<String> parameters = getMessageParameters(msg);
        if (parameters == null)
        {
            return new ActionResult();
        }
        String message = parameters.get(0);
        agent.saveMessage(message);
        String storeMsg = String.format("Agent '%s' push to store '%s'",
                agent.getName(), message);
        agent.displayMessage(storeMsg, senderIp + ":" + senderPort);
        return new ActionResult(true);
    }

}
