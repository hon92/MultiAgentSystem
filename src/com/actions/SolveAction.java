/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.ExpressionSolver;
import com.Parameter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class SolveAction extends Action
{

    private final ExpressionSolver expressionSolver;

    public SolveAction()
    {
        super("solve");
        expressionSolver = new ExpressionSolver();
        addNextParameter(new Parameter<SolveAction>(1, "(solve)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(SolveAction sourceAction,
                    List<String> arguments)
            {
                return performSolve(arguments);
            }
        });
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        Pattern p = Pattern.compile("(ack)\\s\"(.+)\"\\s(.+)");
        Matcher m = p.matcher(message);
        if (m.find() && m.groupCount() == 3)
        {
            String solveMsg = m.group(2);
            String res = m.group(3);
            agent.storeMessage(solveMsg + " " + res);
        }
    }

    private ActionResult performSolve(List<String> params)
    {
        String expression = params.get(0);
        String expressionResult = expressionSolver.evaluate(expression)
                .toString();
        return new ActionResult(expressionResult, true);
    }

}
