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
//command
//send ip port message
public class SendAction extends Action
{
    public SendAction()
    {
        super("send");
        addNextParameter(new Parameter<SendAction>(3, "(send)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(SendAction sourceAction, List<String> arguments)
            {
                return performSend(arguments);
            }
        });
    }

    private ActionResult performSend(List<String> params)
    {
        String ip = params.get(0);
        String portStr = params.get(1);
        String message = params.get(2);
        int port = Integer.parseInt(portStr);
        boolean sended = sendMessageToAddress(agent.getIp(), agent.getPort(), message, ip, port);
        return new ActionResult(sended);
    }

}
