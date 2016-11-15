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
public class StoreAction extends Action
{
    public StoreAction()
    {
        super("store");
        addNextParameter(new Parameter<StoreAction>(1, "(store)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(StoreAction sourceAction, List<String> arguments)
            {
                return performStore(arguments);
            }
        });
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        Pattern p = Pattern.compile("(ack)\\s(store)\\s(.+)");
        Matcher m = p.matcher(message);
        if (m.find() && m.groupCount() == 3)
        {
            String res = m.group(3);
            agent.storeMessage(res);
        }
    }

    private ActionResult performStore(List<String> params)
    {
        String message = params.get(0);
        String ip = agent.getIp();
        int port = agent.getPort();

        boolean sended = sendMessageToAddress(ip, port, message, ip, port);
        return new ActionResult(sended);
    }


}
