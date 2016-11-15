/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Parameter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Honza
 */
public class ExecuteAction extends Action
{

    public ExecuteAction()
    {
        super("execute");
        addNextParameter(new Parameter<ExecuteAction>(1, "(execute)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(ExecuteAction sourceAction, List<String> arguments)
            {
                return performExecute(arguments);
            }
        });
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        System.out.println("not implemented perform ack from execute command");
    }

    private ActionResult performExecute(List<String> params)
    {
        String source = params.get(0);
        System.out.println("execute " + source);

        try
        {
            Process process = Runtime.getRuntime().exec(source);
            int result = process.waitFor();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    System.out.println(line);
                }
            }
            catch (IOException ex)
            {

            }

        }
        catch (IOException e)
        {

        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(ExecuteAction.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ActionResult("EXECUTE SUCCESS", true);
    }


}
