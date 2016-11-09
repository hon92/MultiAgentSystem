/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;
/**
 *
 * @author Honza
 */
public class AckMessage
{
    public String messageToResend;
    public String senderIp;
    public int senderPort;
    public String resultMessage;
    public long time;

    public AckMessage(String messageToResend, String senderIp, int senderPort, String resultMessage, long time)
    {
        this.messageToResend = messageToResend;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.resultMessage = resultMessage;
        this.time = time;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof AckMessage)
        {
            AckMessage am = (AckMessage) obj;
            return am.messageToResend.equals(messageToResend)
                    && am.resultMessage.equals(resultMessage)
                    && am.senderIp.equals(senderIp)
                    && am.senderPort == senderPort;
        }
        return false;
    }


}
