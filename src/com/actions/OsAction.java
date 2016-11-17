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
public class OsAction extends Action
{
    private final String type;

    public OsAction(String type)
    {
        super("os");
        this.type = type;
        addNextParameter(new Parameter<OsAction>(0, "(os)", this)
        {
            @Override
            public ActionResult doAction(OsAction sourceAction, List<String> arguments)
            {
                return performSendOsType();
            }
        });
    }

    private ActionResult performSendOsType()
    {
        return new ActionResult(type, true);
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        Pattern p = Pattern.compile("(ack)\\s\"(.+)\"\\s(.+)");
        Matcher m = p.matcher(message);
        if (m.find() && m.groupCount() == 3)
        {
            String osMsg = m.group(2);
            String res = m.group(3);
            String msg = agent.getIp() + ":" + agent.getPort() + ":" + osMsg;
            agent.storeMessage(msg + " " + res);
        }
    }

}
