/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Executor;
import com.Parameter;
import com.util.Util;
import java.io.File;
import java.util.List;

/**
 *
 * @author Honza
 */
public class ExecuteAction extends Action
{

    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";

    public ExecuteAction()
    {
        super("execute");
        addNextParameter(new Parameter<ExecuteAction>(2,
                "(execute)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3}):(\\d+)",
                this)
        {
            @Override
            public ActionResult doAction(ExecuteAction sourceAction,
                    List<String> arguments)
            {
                return performExecuteAgent(arguments);
            }
        });

        addNextParameter(new Parameter<ExecuteAction>(1, "(execute)\\s(.+)",
                this)
        {
            @Override
            public ActionResult doAction(ExecuteAction sourceAction,
                    List<String> arguments)
            {
                return performExecute(arguments);
            }
        });
    }

    private ActionResult performExecute(List<String> params)
    {
        String source = params.get(0);

        boolean executed = new Executor().execute(source);
        if (executed)
        {
            return new ActionResult(SUCCESS, true);
        }
        else
        {
            return new ActionResult(FAIL, true);
        }
    }

    private ActionResult performExecuteAgent(List<String> params)
    {
        final String MAIN_FILE_FILENAME = "Main";
        String ip = params.get(0);
        int port = Integer.parseInt(params.get(1));

        ip = ip.replace(".", "-");

        String agentFolderPath = getAgent().getFilesFolderPath();

        File agentFolder = new File(agentFolderPath
                + File.separator
                + ip
                + "-"
                + port);

        if (agentFolder.exists() && agentFolder.isDirectory())
        {
            File mainFile = Util.findFile(agentFolder, MAIN_FILE_FILENAME);
            if (mainFile != null && new Executor().executeFile(mainFile))
            {
                return new ActionResult(SUCCESS, true);
            }
            else
            {
                return new ActionResult(FAIL, true);
            }
        }
        else
        {
            return new ActionResult(FAIL, true);
        }
    }
}
