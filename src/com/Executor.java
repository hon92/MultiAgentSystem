/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.File;
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
            Logger.getInstance().log(Logger.Level.Error, e.getMessage());
            return false;
        }
    }

    public boolean executeFile(File file)
    {
        if (!file.exists() || !file.isFile())
        {
            return false;
        }

        String filename = file.getName();
        String absPath = file.getAbsolutePath();

        if (filename.endsWith("py"))
        {
            return runPythonFile(absPath);
        }
        else if (filename.endsWith("jar"))
        {
            return runJavaFile(absPath);
        }
        else if (filename.endsWith("exe"))
        {
            return runExeFile(absPath);
        }
        else
        {
            Logger.getInstance().log(Logger.Level.Error,
                    "Unsupported extension for execute program");
            return false;
        }
    }

    private boolean runPythonFile(String filePath)
    {
        return execute("python " + filePath);
    }

    private boolean runJavaFile(String filePath)
    {
        return execute("java -jar " + filePath);
    }

    private boolean runExeFile(String filePath)
    {
        return execute("\"" + filePath + "\"");
    }
}
