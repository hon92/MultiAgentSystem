/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.actions;

import com.Agent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Honza
 */
public class PackageAction extends Action
{

    private final Agent agent;
    private final String filesRegex = "(package)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(.+)";
    private final String projectPath;
    private final String HASHING_ALGORITHM = "MD5";

    public PackageAction(Agent agent, String projectPath)
    {
        super("package", 4, "(package)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)\\s(\\d{0,3}.\\d{0,3}.\\d{0,3}.\\d{0,3})\\s(\\d+)", true);
        this.agent = agent;
        this.projectPath = projectPath;
    }

    @Override
    public ActionResult perform(String senderIp, int senderPort, String msg) throws Exception
    {
        List<String> params = getMessageParameters(msg);
        if (params == null)
        {
            params = getFilesMessageParameters(msg);
            if (params == null)
            {
                return new ActionResult();
            }
            else
            {
                return performSendFiles(senderIp, senderPort, params);
            }
        }
        return performSendProject(senderIp, senderPort, params);
    }

    private List<String> getFilesMessageParameters(String message)
    {
        Pattern p = Pattern.compile(filesRegex);
        Matcher m = p.matcher(message);
        final int paramsLength = 3;

        if (m.find() && m.groupCount() == paramsLength + 1)
        {
            List<String> parameters = new ArrayList<>();
            for (int i = 2; i < paramsLength + 2; i++)
            {
                parameters.add(m.group(i));
            }
            return parameters;
        }
        return null;
    }

    private ActionResult performSendProject(String senderIp, int senderPort, List<String> params)
    {
        boolean sended = false;
        return new ActionResult(sended);
    }

    private ActionResult performSendFiles(String senderIp, int senderPort, List<String> params) throws IOException
    {
        String filesString = params.get(2);
        String[] filesNames = filesString.split(" ");
        String targetIp = params.get(0);
        int targetPort = Integer.parseInt(params.get(1));
        for (String filename : filesNames)
        {
            File zipFile = createZipFile(new File(filename));
            if (zipFile != null)
            {
                String hashZipFile = getHash(zipFile);
                if (!hashZipFile.equals(""))
                {
                    System.out.println("zip file " + filename + " " + zipFile.length() + " " + hashZipFile);
                    String dataMsg = new String(Files.readAllBytes(Paths.get(zipFile.getAbsolutePath())));
                    String fileMessage = "file " + hashZipFile + " " + dataMsg;
                    boolean sended = sendMessageToAddress(senderIp, senderPort, fileMessage, targetIp, targetPort);
                    return new ActionResult(sended);
                }
            }
            else
            {
                System.err.println("zip file null " + filename);
            }
        }
        return new ActionResult();
    }

    private File createZipFile(File file)
    {
        if (file.exists() && file.isFile())
        {
            int dotIndex = file.getAbsolutePath().indexOf(".");
            String zipFilename = file.getAbsolutePath().substring(0, dotIndex) + ".zip";
            File resultZipFile = new File(zipFilename);
            try
            {
                try (FileInputStream fis = new FileInputStream(file))
                {
                    try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(resultZipFile)))
                    {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);
                        byte[] buffer = new byte[1024];
                        int readBytes = 0;
                        while ((readBytes = fis.read(buffer)) > 0)
                        {
                            zos.write(buffer, 0, readBytes);
                            zos.flush();
                        }
                        zos.closeEntry();
                        return resultZipFile;
                    }
                }
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(PackageAction.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            catch (IOException ex)
            {
                Logger.getLogger(PackageAction.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }

    private String getHash(File file)
    {
        try (FileInputStream fis = new FileInputStream(file))
        {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
            md.update(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            byte[] hashBytes = md.digest();
            String digestInHex = DatatypeConverter.printHexBinary(hashBytes).toUpperCase();
            return digestInHex;//System.out.println(digestInHex);
            //return new BigInteger(1, hashBytes).toString(16);
        }
        catch (Exception ex)
        {
            return "";
        }
    }

    private byte[] getZipFileBytes(String filename)
    {
        try
        {
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ZipInputStream zis = new ZipInputStream(bais);
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(new File("result.txt"));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null)
            {
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory())
                {
                    while ((bytesRead = zis.read(buffer)) != -1)
                    {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }

            zis.close();
            fos.close();
//            while ((entry = zis.getNextEntry()) != null)
//            {
//                entry.getName();

//                String entryName = zipEntry.getName();
//                FileOutputStream out = new FileOutputStream(entryName);
//                byte[] byteBuff = new byte[1024];
//                int bytesRead = 0;
//                while ((bytesRead = zis.read(byteBuff)) != -1)
//                {
//                    out.write(byteBuff, 0, bytesRead);
//                }
//
//                out.close();
//                zis.closeEntry();
//            }
            zis.close();
            return bytes;
        }
        catch (IOException ex)
        {
            Logger.getLogger(PackageAction.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
