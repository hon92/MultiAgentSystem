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
public class StoreAction extends Action
{
    public StoreAction()
    {
        super("store");
        addNextParameter(new Parameter<StoreAction>(1, "(store)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(StoreAction sourceAction,
                    List<String> arguments)
            {
                return performStore(arguments);
            }
        });
    }

    private ActionResult performStore(List<String> params)
    {
        String message = params.get(0);
        agent.storeMessage(message);
        return new ActionResult(true);
    }


}
