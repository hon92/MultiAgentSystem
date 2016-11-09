/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

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
        super("send", 3, "(send)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(.+)", false);
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
        String message = parameters.get(2);
        int port = Integer.parseInt(portStr);
        boolean sended = sendMessageToAddress(senderIp, senderPort, message, ip, port);
        return new ActionResult(sended);
    }

}
