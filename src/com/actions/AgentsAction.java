/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.AgentDb;
import com.Parameter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class AgentsAction extends Action
{
    public AgentsAction()
    {
        super("agents");
        addNextParameter(new Parameter<AgentsAction>(0, "(agents)", this)
        {
            @Override
            public ActionResult doAction(AgentsAction sourceAction, List<String> arguments)
            {
                return performSendAgentsList();
            }
        });
    }

    private ActionResult performSendAgentsList()
    {
        String agentsString = getAgentsString();
        String ip = agent.getIp();
        int port = agent.getPort();
        boolean sended = sendMessageToAddress(ip, port, agentsString, ip, port);
        return new ActionResult(agentsString, sended);
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        Pattern p = Pattern.compile("(ack)\\s\"(.+)\"\\s(.+)");
        Matcher m = p.matcher(message);
        if (m.find() && m.groupCount() == 3)
        {
            String solveMsg = m.group(2);
            String res = m.group(3);
            agent.storeMessage(solveMsg + " " + res);
        }
    }

    private String getAgentsString()
    {
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
        return sb.toString();
    }
}
