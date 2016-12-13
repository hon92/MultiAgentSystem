/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

/**
 *
 * @author Honza
 */
public class AckAction extends Action
{

    private final String agentIp;
    private final int agentPort;
    private final String senderIp;
    private final int senderPort;

    public AckAction(String agentIp, int agentPort, String senderIp, int senderPort)
    {
        super("ack");
        this.agentIp = agentIp;
        this.agentPort = agentPort;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
    }

    @Override
    public ActionResult perform(String msg)
    {
        String ackMsg = String.format("ack %s", msg);
        boolean sended = sendMessageToAddress(agentIp,
                agentPort,
                ackMsg,
                senderIp,
                senderPort);
        return new ActionResult(sended);
    }

}
