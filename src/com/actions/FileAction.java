/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Honza
 */
public class FileAction extends Action
{

    public FileAction()
    {
        super("file", 2, "(file)\\s(.+)\\s(.+)", false);
    }

    @Override
    public ActionResult perform(String sourceIp, int sourcePort, String message) throws Exception
    {
        List<String> params = getMessageParameters(message);
        if (params == null)
        {
            return new ActionResult();
        }

        String data = params.get(1);
        System.err.println(data);
        File f = createFile(data.getBytes());
        System.err.println(data);
        return new ActionResult(true);
    }

    public File createFile(byte[] bytes)
    {
        try
        {
            File file = new File("test.zip");
            ZipInputStream zis = new ZipInputStream(new ByteInputStream(bytes, bytes.length));
            ZipEntry zipEntry = null;
            while ((zipEntry = zis.getNextEntry()) != null)
            {
                String entryName = zipEntry.getName();
                FileOutputStream out = new FileOutputStream(entryName);
                byte[] byteBuff = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = zis.read(byteBuff)) != -1)
                {
                    out.write(byteBuff, 0, bytesRead);
                }

                out.close();
                zis.closeEntry();
            }
            zis.close();
            return file;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

}
