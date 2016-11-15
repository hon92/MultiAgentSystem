/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import com.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Honza
 */
public class FileComposer
{
    private final String sourceIp;
    private final int sourcePort;
    private final int partsCount;
    private final String hash;
    private final String id;
    private final List<Part> parts;

    public FileComposer(String sourceIp, int sourcePort, int partsCount, String hash, String id)
    {
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.partsCount = partsCount;
        this.hash = hash;
        this.id = id;
        parts = new ArrayList<>();
    }

    public boolean isComplete()
    {
        return partsCount == parts.size();
    }

    public void addPart(Part part)
    {
        parts.add(part);
    }

    public boolean writeAndCheck(String path)
    {
        if (!isComplete())
        {
            return false;
        }
        File destFolder = new File(path);
        if (!destFolder.exists())
        {
            if (!destFolder.mkdir())
            {
                System.err.println("cannot create directory");
                return false;
            }
        }
        File f = new File(id);
        File destFile = new File(path + File.separator + f.getName());
        try (FileOutputStream fos = new FileOutputStream(destFile))
        {
            for (Part p : parts)
            {
                fos.write(p.getData());
                fos.flush();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
        return Util.getHash(destFile).equals(hash);
    }



}
