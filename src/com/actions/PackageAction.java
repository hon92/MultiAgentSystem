/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.FileComposer;
import com.Parameter;
import com.Part;
import com.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Honza
 */
public class PackageAction extends Action
{

    private final String projectFile;
    private final Map<String, FileComposer> files;

    public PackageAction(String projectFile)
    {
        super("package");
        this.projectFile = projectFile;
        files = new HashMap<>();

        addNextParameter(new Parameter<PackageAction>(2, "(package)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3}):(\\d+)", this)
        {
            @Override
            public ActionResult doAction(PackageAction sourceAction, List<String> arguments)
            {
                return sourceAction.performSendProject();
            }

            @Override
            public boolean precondition(List<String> arguments)
            {
                String sourceIp = arguments.get(0);
                int sourcePort = Integer.parseInt(arguments.get(1));
                return sourceIp.equals(agent.getIp()) && sourcePort == agent.getPort();
            }
        });

        addNextParameter(new Parameter<PackageAction>(1, "(package)\\s(.+)", this)
        {
            @Override
            public ActionResult doAction(PackageAction sourceAction, List<String> arguments)
            {
                return sourceAction.performSendFiles(arguments);
            }
        });

    }
    
    private String getFilePrefix()
    {
        return agent.getIp() + "-" + agent.getPort();
    }

    private ActionResult performSendProject()
    {
        File zipFile = Util.createZipFile(new File(projectFile));
        if (zipFile == null)
        {
            return new ActionResult();
        }
        List<String> parts = prepareFileParts(zipFile);
        if (parts == null)
        {
            return new ActionResult();
        }
        return new ActionResult(parts, true);
    }

    private ActionResult performSendFiles(List<String> params)
    {
        String filesString = params.get(0);
        String[] filesNames = filesString.split(" ");
        List<String> allParts = new ArrayList<>();

        for (String filename : filesNames)
        {
            File zipFile = Util.createZipFile(new File(filename));
            if (zipFile != null)
            {
                String zipFilename = zipFile.getName();
                String nameWithoutZip = zipFilename.substring(0, zipFilename.length() - 4);
                List<String> parts = prepareFileParts(zipFile);
                if (parts == null)
                {
                    System.err.println(filename + " was skipped due to error");
                    continue;
                }
                allParts.addAll(parts);
            }
            else
            {
                System.err.println(filename + " cant be converted to zip");
            }
        }
        return new ActionResult(allParts, true);
    }

    private ActionResult performFilePart(List<String> params)
    {
        String sourceIp = params.get(0);
        int sourcePort = Integer.parseInt(params.get(1));
        int currentPart = Integer.parseInt(params.get(2));
        int parts = Integer.parseInt(params.get(3));
        String hash = params.get(4);
        String id = params.get(5);
        int size = Integer.parseInt(params.get(6));
        byte[] data = Util.decodeString(params.get(7));

        String k = sourceIp + ":" + sourcePort + ":" + hash;
        FileComposer myFile = files.get(k);

        if (myFile == null)
        {
            myFile = new FileComposer(sourceIp, sourcePort, parts, hash, id);
            files.put(k, myFile);
        }

        System.out.println("Receiving " + id + " " + currentPart + "/" + parts);
        myFile.addPart(new Part(currentPart, size, data));

        if (myFile.isComplete())
        {
            boolean success = myFile.writeAndCheck(agent.getFilesFolderPath());
            if (success)
            {
                System.out.println("File " + id + " was corretly received");
                files.remove(k);
            }
            else
            {
                System.err.println("File " + id + " is corrupted");
            }
        }
        return new ActionResult("RECEIVED", true);
    }

    @Override
    public void performAck(String ip, int port, String message)
    {
        final String filePartAckRegex = "ack\\s\"(.+)\"\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3}):(\\d+)\\s(\\d+)/(\\d+)\\s(.+)\\s(.+)\\s(\\d+)\\s(.+)";

        Pattern p = Pattern.compile(filePartAckRegex);
        Matcher m = p.matcher(message);
        if (m.matches() && m.groupCount() == 9)
        {
            List<String> arguments = new ArrayList<>();
            for (int i = 2; i <= m.groupCount(); i++)
            {
                arguments.add(m.group(i));
            }
            performFilePart(arguments);
        }
    }

    private List<String> prepareFileParts(File file)
    {
        if (!file.exists())
        {
            return null;
        }
        final int BUFFER_SIZE = 4096;
        long fileLegth = file.length();
        int parts = ((int) fileLegth / BUFFER_SIZE) + ((int) fileLegth % BUFFER_SIZE > 0 ? 1 : 0);
        String fileHash = Util.getHash(file);
        if (fileHash.equals(""))
        {
            return null;
        }

        String id = file.getName();
        List<String> allParts = new ArrayList<>(parts);

        try (FileInputStream fis = new FileInputStream(file))
        {
            int readed = 0;
            byte[] buffer = new byte[BUFFER_SIZE];

            int index = 1;
            while ((readed = fis.read(buffer)) != -1)
            {
                byte[] bytesToEncode = new byte[readed];
                System.arraycopy(buffer, 0, bytesToEncode, 0, readed);
                String encodedString = Util.encodeBytes(bytesToEncode);
                String part = String.format("%s:%d %d/%d %s %s %d %s",
                        agent.getIp(),
                        agent.getPort(),
                        index++,
                        parts,
                        fileHash,
                        id,
                        readed,
                        encodedString);
                allParts.add(part);
            }
            return allParts;
        }
        catch (IOException ex)
        {
            return null;
        }
    }

}
