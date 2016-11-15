/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Honza
 */
public class ActionResult
{
    private final List<String> resultMessages;
    private final boolean performed;
    private final boolean hasResult;


    private ActionResult(List<String> resultMessages, boolean performed, boolean hasResult)
    {
        this.resultMessages = resultMessages;
        this.performed = performed;
        this.hasResult = hasResult;
    }

    public ActionResult(boolean performed)
    {
        this(new ArrayList<String>(), performed, false);
    }

    public ActionResult()
    {
        this(new ArrayList<String>(), false, false);
    }

    public ActionResult(List<String> resultMessages, boolean performed)
    {
        this(resultMessages, performed, true);
    }

    public ActionResult(String resultMessage, boolean performed)
    {
        this(Arrays.asList(resultMessage), performed);
    }

    public List<String> getResultMessages()
    {
        return resultMessages;
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
