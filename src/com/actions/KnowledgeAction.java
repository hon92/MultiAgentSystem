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

    public KnowledgeAction(String prefix, int paramsLength, String regex, boolean hasResult)
    {
        super(prefix, paramsLength, regex, hasResult);
    }

    @Override
    public ActionResult perform(String sourceIp, int sourcePort, String message) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
