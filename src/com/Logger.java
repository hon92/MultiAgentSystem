/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.CodeSource;
import java.util.Calendar;

/**
 *
 * @author Honza
 */
public class Logger
{

    public static enum Level
    {
        Warning, Error, Info
    };
    private static Logger LOGGER = null;
    private final String LOG_FILENAME = "log.log";
    private BufferedWriter bw;
    private File logFile;

    private Logger()
    {
        try
        {
            CodeSource cs = Window.class.getProtectionDomain().getCodeSource();
            File f = new File(cs.getLocation().getPath());
            String directory = f.getParentFile().getPath();
            logFile = new File(directory + File.separator + LOG_FILENAME);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
        }
        catch (IOException ex)
        {
            System.exit(0);
        }
    }

    public static Logger getInstance()
    {
        if (LOGGER == null)
        {
            LOGGER = new Logger();
        }
        return LOGGER;
    }

    public File getLoggerFile()
    {
        return getInstance().logFile;
    }

    public synchronized void log(Level level, String text)
    {
        writeToLog(formatText(text, level));
        writeToLog("\n");
    }

    private String formatText(String text, Level level)
    {
        return String.format("%s %s:%s", Calendar.getInstance().getTime().toString(),
                level.toString(),
                text);
    }

    private synchronized void writeToLog(String text)
    {
        try
        {
            bw.write(text);
            bw.flush();
        }
        catch (IOException ex)
        {
            System.exit(0);
        }
    }
}
