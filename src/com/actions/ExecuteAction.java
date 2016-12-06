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
        addNextParameter(new Parameter<ExecuteAction>(0, "(execute)\\sMain", this)
        {
            @Override
            public ActionResult doAction(ExecuteAction sourceAction, List<String> arguments)
            {
                return performExecuteMain();
            }
        });

        addNextParameter(new Parameter<ExecuteAction>(1, "(execute)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(ExecuteAction sourceAction, List<String> arguments)
            {
                return performExecute(arguments);
            }
        });
    }

    private ActionResult performExecute(List<String> params)
    {
        String source = params.get(0);
        System.out.println("execute " + source);

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

    private ActionResult performExecuteMain()
    {
        String folderPath = getAgent().getFilesFolderPath();
        File folder = new File(folderPath);

        File f = Util.findFile(folder, "Main");
        if (f != null)
        {
            if (f.isFile())
            {
                String name = f.getName();
                boolean executed = false;

                if (name.endsWith("py"))
                {
                    executed = runPythonFile(f.getAbsolutePath());
                }
                else if (name.endsWith("jar"))
                {
                    executed = runJavaFile(f.getAbsolutePath());
                }
                else if (name.endsWith("exe"))
                {
                    executed = runExeFile(f.getAbsolutePath());
                }
                else
                {
                    System.err.println("Unsupported extension for execute program");
                    return new ActionResult(FAIL, true);
                }
                if (executed)
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
        else
        {
            return new ActionResult(FAIL, true);
        }
    }

    private boolean runPythonFile(String filePath)
    {
        return new Executor().execute("python " + filePath);
    }

    private boolean runJavaFile(String filePath)
    {
        return new Executor().execute("java -jar " + filePath);
    }

    private boolean runExeFile(String filePath)
    {
        return new Executor().execute("\"" + filePath + "\"");
    }
}
