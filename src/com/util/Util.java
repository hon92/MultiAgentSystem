/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.util;

import com.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Honza
 */
public class Util
{

    public static String getHash(File file)
    {
        final String HASHING_ALGORITHM = "MD5";

        try
        {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
            md.update(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            byte[] hashBytes = md.digest();
            String digestInHex = DatatypeConverter.printHexBinary(hashBytes)
                    .toUpperCase();
            return digestInHex;
        }
        catch (IOException | NoSuchAlgorithmException ex)
        {
            return "";
        }
    }

    public static File createZipFile(File file)
    {
        if (!file.exists())
        {
            return null;
        }

        String zipFilename;
        if (file.isDirectory())
        {
            zipFilename = file.getName() + ".zip";
        }
        else
        {
            String absPath = file.getAbsolutePath();
            int dotIndex = absPath.indexOf(".");
            zipFilename = absPath.substring(0, dotIndex) + ".zip";
        }

        File zipFile = new File(zipFilename);

        try
        {
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)))
            {
                if (file.isFile())
                {
                    zipFile(zos, file, file);
                }
                else
                {
                    zipDirectory(zos, file, file);
                }
            }
            return zipFile;
        }
        catch (IOException ex)
        {
            Logger.getInstance().log(Logger.Level.Error, ex.getMessage());
            return null;
        }
    }

    public static boolean unzipFile(File zipFile)
    {
        if (!zipFile.exists() || !zipFile.getName().endsWith(".zip"))
        {
            return false;
        }

        String filename = zipFile.getName();
        String nameWithouExtension = filename.substring(0,
                filename.length() - 4);
        String outputFolderName
                = zipFile.getParent() + File.separator + nameWithouExtension;
        File outputFolder = new File(outputFolderName);
        if (!outputFolder.exists())
        {
            if (!outputFolder.mkdir())
            {
                return false;
            }
        }

        byte[] buffer = new byte[2048];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
        {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null)
            {
                String name = zipEntry.getName();
                File f = new File(outputFolderName + File.separator + name);
                new File(f.getParent()).mkdir();
                try (FileOutputStream fos = new FileOutputStream(f))
                {
                    int readed = 0;
                    while ((readed = zis.read(buffer)) != -1)
                    {
                        fos.write(buffer, 0, readed);
                    }
                }
            }
            zis.closeEntry();
            return true;
        }
        catch (IOException ex)
        {
            Logger.getInstance().log(Logger.Level.Error, ex.getMessage());
            return false;
        }
    }

    public static byte[] decodeString(String binaryString)
    {
        return Base64.getDecoder().decode(binaryString);
    }

    public static String encodeBytes(byte[] bytes)
    {
        return new String(Base64.getEncoder().encode(bytes));
    }

    public static File findFile(File directory, String filename)
    {
        File file = null;
        if (!directory.exists() || !directory.isDirectory())
        {
            return null;
        }
        for (File f : directory.listFiles())
        {
            if (f.isDirectory())
            {
                file = findFile(f, filename);
                if (file != null)
                {
                    return file;
                }
            }
            String[] s = f.getName().split("\\.(?=[^\\.]+$)");
            if (s[0].equals(filename))
            {
                file = f;
                break;
            }
        }
        return file;
    }

    private static void zipDirectory(ZipOutputStream zos,
            File directory,
            File originDirectory) throws IOException
    {
        if (!directory.exists() || !directory.isDirectory())
        {
            return;
        }

        File[] files = directory.listFiles();
        for (File f : files)
        {
            if (f.isDirectory())
            {
                zipDirectory(zos, f, originDirectory);
            }
            else
            {
                zipFile(zos, f, originDirectory);
            }
        }
    }

    private static void zipFile(ZipOutputStream zos,
            File file, File originDirectory) throws IOException
    {
        if (!file.exists() || !file.isFile())
        {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file))
        {
            int len = originDirectory.getPath().length() + 1;
            String sub = file.getPath().substring(len);
            ZipEntry zipEntry = new ZipEntry(sub);
            zos.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int readBytes = 0;
            while ((readBytes = fis.read(buffer)) > 0)
            {
                zos.write(buffer, 0, readBytes);
                zos.flush();
            }
            zos.closeEntry();
        }
    }

}
