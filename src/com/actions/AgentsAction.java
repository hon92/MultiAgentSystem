/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.AgentDb;
import com.Parameter;
import java.util.List;

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
        return new ActionResult(agentsString, true);
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
