/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Parameter;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.Timer;

/**
 *
 * @author Honza
 */
public class TerminateAction extends Action
{

    public TerminateAction()
    {
        super("terminate");
        addNextParameter(new Parameter<TerminateAction>(0, "(terminate)", this)
        {
            @Override
            public ActionResult doAction(TerminateAction sourceAction, List<String> arguments)
            {
                return performTerminate();
            }
        });
    }

    private ActionResult performTerminate()
    {
        Timer t = new Timer(1000, (ActionEvent e) ->
        {
            System.exit(0);
        });
        t.start();
        return new ActionResult("SUCCESS", true);
    }

}
