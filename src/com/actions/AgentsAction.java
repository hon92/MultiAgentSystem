/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
import com.AgentDb;
import java.util.List;

/**
 *
 * @author Honza
 */
public class AgentsAction extends Action
{

    private final Agent agent;

    public AgentsAction(Agent agent)
    {
        super("agents", 2, "(agents)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)", true);
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

        String ip = parameters.get(0);
        String portStr = parameters.get(1);
        int port = Integer.parseInt(portStr);
        AgentDb db = agent.getAgentDb();
        List<String> agentsInfo = db.getAgentsList();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < agentsInfo.size(); i++)
        {
            if (i + 1 < agentsInfo.size())
            {
                sb.append(agentsInfo.get(i));
                sb.append(",");
            }
            else
            {
                sb.append(agentsInfo.get(i));
            }
        }
        sb.append("}");
        boolean sended = sendMessageToAddress(senderIp, senderPort, sb.toString(), ip, port);
        return new ActionResult(sb.toString(), sended);
    }

}
