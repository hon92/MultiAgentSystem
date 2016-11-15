/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.actions.Action;
import com.actions.ActionResult;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public abstract class Parameter<T>
{
    private final int paramsCount;
    private final String regex;
    private Parameter nextParameter;
    private final Action action;

    public <T extends Action> Parameter(int paramsCount, String regex, T action)
    {
        this.paramsCount = paramsCount;
        this.regex = regex;
        this.action = action;
    }

    public abstract ActionResult doAction(T sourceAction, List<String> arguments);

    public ActionResult match(String msg)
    {
        List<String> arguments = getArguments(msg);
        if (arguments != null)
        {
            if (precondition(arguments))
            {
                return doAction((T) action, arguments);
            }
        }

        if (nextParameter != null)
        {
            return nextParameter.match(msg);
        }
        return new ActionResult();
    }

    public List<String> getArguments(String message)
    {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);

        if (m.matches() && m.groupCount() == paramsCount + 1)
        {
            List<String> parameters = new ArrayList<>();
            for (int i = 2; i < paramsCount + 2; i++)
            {
                parameters.add(m.group(i));
            }
            return parameters;
        }
        return null;

    }

    public void setNext(Parameter parameter)
    {
        nextParameter = parameter;
    }

    public boolean precondition(List<String> arguments)
    {
        return true;
    }
}
