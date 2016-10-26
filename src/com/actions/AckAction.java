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

    public AckAction(String agentIp, int agentPort)
    {
        super("ack", 0);
        this.agentIp = agentIp;
        this.agentPort = agentPort;
    }

    @Override
    public void perform(String senderIp, int senderPort, String msg) throws Exception
    {
        String ackMsg = String.format("ack %s", msg);
        System.out.println("ACK:" + ackMsg);
        sendMessageToAddress(agentIp, agentPort, ackMsg, senderIp, senderPort);
    }

}