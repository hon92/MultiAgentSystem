/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.ExpressionSolver;
import java.util.List;

/**
 *
 * @author Honza
 */
public class SolveAction extends Action
{

    private final ExpressionSolver expressionSolver;

    public SolveAction()
    {
        super("solve", 1, "(solve)\\s(.+)", true);
        expressionSolver = new ExpressionSolver();
    }

    @Override
    public ActionResult perform(String senderIp, int senderPort, String msg) throws Exception
    {
        List<String> parameters = getMessageParameters(msg);
        if (parameters == null)
        {
            return new ActionResult();
        }
        String expression = parameters.get(0);
        String expressionResult = expressionSolver.evaluate(expression).toString();
        boolean sended = sendMessageToAddress(senderIp, senderPort, expressionResult, senderIp, senderPort);
        return new ActionResult(expressionResult, sended);
    }

}
