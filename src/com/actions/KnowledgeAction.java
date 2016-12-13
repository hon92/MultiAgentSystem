/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Parameter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class KnowledgeAction extends Action
{

    public static final String TRUE = "True";
    public static final String FALSE = "False";

    public KnowledgeAction()
    {
        super("knowledge");
        addNextParameter(new Parameter<KnowledgeAction>(0, "(knowledge)\\s\\*",
                this)
        {
            @Override
            public ActionResult doAction(KnowledgeAction sourceAction,
                    List<String> arguments)
            {
                return performAllKnowledges();
            }
        });

        addNextParameter(new Parameter<KnowledgeAction>(1, "(knowledge)\\s(.+)",
                this)
        {
            @Override
            public ActionResult doAction(KnowledgeAction sourceAction,
                    List<String> arguments)
            {
                return performQuery(arguments);
            }
        });
    }

    private ActionResult performAllKnowledges()
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
        Pattern p = Pattern.compile("(.+)\\s(.+)");

        List<String> knowledges = agent.getKnowledges();
        for (String knowledge : knowledges)
        {
            Matcher m = p.matcher(knowledge);
            if (m.matches() && m.groupCount() == 2)
            {
                String q = m.group(1);
                String res = m.group(2);
                if (q.equals(query))
                {
                    return new ActionResult(res, true);
                }
            }
            else
            {
                if (knowledge.equals(query))
                {
                    return new ActionResult("", true);
                }
            }
        }
        return new ActionResult(FALSE, true);
    }

}
