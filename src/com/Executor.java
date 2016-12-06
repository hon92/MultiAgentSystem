/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.IOException;

/**
 *
 * @author Honza
 */
public class Executor
{
    public Executor()
    {

    }

    public boolean execute(String command)
    {
        try
        {
            Runtime.getRuntime().exec(command);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
}
