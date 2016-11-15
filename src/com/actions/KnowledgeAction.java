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
public class KnowledgeAction extends Action
{

    public KnowledgeAction(String prefix)
    {
        super(prefix);
    }

    @Override
    public ActionResult perform(String message)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        System.out.println("not implemented perform ack from knowledge command");
    }


}
