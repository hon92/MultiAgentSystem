/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.ExpressionSolver;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class SolveAction extends Action
{
    private final String PATTERN = "(solve)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(.+)";
    private final ExpressionSolver expressionSolver;

    public SolveAction()
    {
        super("solve", 3);
        expressionSolver = new ExpressionSolver();
    }

    @Override
    public void perform(String senderIp, int senderPort, String msg) throws Exception
    {
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(msg);
        if (m.find() && m.groupCount() == getParamsLength() + 1)
        {
            String ip = m.group(2);
            String portStr = m.group(3);
            String expression = m.group(4);
            int port = Integer.parseInt(portStr);
            String expressionResult = expressionSolver.evaluate(expression).toString();
            sendMessageToAddress(senderIp, senderPort, expressionResult, ip, port);
        }
        else
        {
            throw new Exception("Invalid arguments for " + getPrefix() + " action");
        }
    }

}
