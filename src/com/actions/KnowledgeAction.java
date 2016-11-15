/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Parameter;
import java.util.List;

/**
 *
 * @author Honza
 */
public class KnowledgeAction extends Action
{

    public static final String TRUE = "True";
    public static final String FALSE = "False";

    public KnowledgeAction(String prefix)
    {
        super(prefix);
        addNextParameter(new Parameter<KnowledgeAction>(1, "(knowledge)\\s(*)", this)
        {
            @Override
            public ActionResult doAction(KnowledgeAction sourceAction, List<String> arguments)
            {
                return performAllKnowledges(arguments);
            }
        });

        addNextParameter(new Parameter<KnowledgeAction>(1, "(knowledge)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(KnowledgeAction sourceAction, List<String> arguments)
            {
                return performQuery(arguments);
            }
        });
    }

    @Override
    public ActionResult perform(String message)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        System.out.println("not implemented perform ack from knowledge command");
    }

    private ActionResult performAllKnowledges(List<String> params)
    {
        List<String> knowledges = agent.getKnowledges();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < knowledges.size(); i++)
        {
            sb.append(knowledges.get(i));
            if (i < knowledges.size() - 1)
            {
                sb.append(",");
            }
        }
        return new ActionResult(sb.toString(), true);
    }

    private ActionResult performQuery(List<String> params)
    {
        String query = params.get(0);
        List<String> knowledges = agent.getKnowledges();
        for (String knowledge : knowledges)
        {
            String[] s = knowledge.split(" ");
            String q = s[0];
            String res = s[1];
            if (q.equals(query))
            {
                return new ActionResult(TRUE, true);
            }
        }
        return new ActionResult(FALSE, true);
    }

}
