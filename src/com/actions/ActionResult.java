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
public class ActionResult
{
    private final String resultMessage;
    private final boolean performed;
    private final boolean hasResult;

    private ActionResult(String resultMessage, boolean performed, boolean hasResult)
    {
        this.resultMessage = resultMessage;
        this.performed = performed;
        this.hasResult = hasResult;
    }

    public ActionResult(boolean performed)
    {
        this(null, performed, false);
    }

    public ActionResult()
    {
        this(null, false, false);
    }

    public ActionResult(String resultMessage, boolean performed)
    {
        this(resultMessage, performed, true);
    }

    public String getResultMessage()
    {
        return resultMessage;
    }

    public boolean isPerformed()
    {
        return performed;
    }

    public boolean hasResult()
    {
        return hasResult;
    }
}
