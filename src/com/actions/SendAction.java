/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
//command
//send ip port message
public class SendAction extends Action
{
    private final String PATTERN = "(send)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(.+)";

    public SendAction()
    {
        super("send", 3);
    }

    @Override
    public void perform(String senderIp, int senderPort, String msg) throws Exception
    {
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(msg);
        if (m.find() && m.groupCount() == getParamsLength() + 1)
        {
            String ip = m.group(2);
            String portStr = m.group(3);
            String message = m.group(4);
            int port = 0;
            try
            {
                port = Integer.parseInt(portStr);
            }
            catch (NumberFormatException e)
            {
                throw new Exception("Port number must be integer");
            }
            boolean sended = sendMessageToAddress(senderIp, senderPort, message, ip, port);
            if (!sended)
            {
                throw new Exception("Cant send socket to address " + ip + ":" + portStr);
            }
        }
        else
        {
            throw new Exception("Invalid arguments for " + getPrefix() + " action");
        }
    }

}