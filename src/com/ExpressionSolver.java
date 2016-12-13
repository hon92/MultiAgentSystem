/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author Honza
 */
public class ExpressionSolver
{
    private final ScriptEngine engine;

    public ExpressionSolver()
    {
        engine = new ScriptEngineManager().getEngineByName("JavaScript");
    }

    public Integer evaluate(String expression)
    {
        try
        {
            Object result = engine.eval(expression);
            if (result instanceof Integer)
            {
                int intResult = (Integer) result;
                return intResult;
            }
            return null;
        }
        catch (ScriptException ex)
        {
            Logger.getInstance().log(Logger.Level.Error, ex.getMessage());
            return null;
        }
    }

}
