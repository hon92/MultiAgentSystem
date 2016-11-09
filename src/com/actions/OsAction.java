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
public class OsAction extends Action
{

    private final String type;

    public OsAction(String type)
    {
        super("os", 0, "", true);
        this.type = type;
    }

    @Override
    public ActionResult perform(String senderIp, int senderPort, String msg) throws Exception
    {
        boolean sended = sendMessageToAddress(senderIp, senderPort, type, senderIp, senderPort);
        return new ActionResult(type, sended);
    }

}
