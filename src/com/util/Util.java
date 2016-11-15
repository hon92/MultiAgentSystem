/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.util;

import com.actions.PackageAction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
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
            String digestInHex = DatatypeConverter.printHexBinary(hashBytes).toUpperCase();
            return digestInHex;
        }
        catch (IOException | NoSuchAlgorithmException ex)
        {
            return "";
        }
    }

    public static File createZipFile(File file)
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
        else
        {
            System.err.println("File " + file.getAbsolutePath() + " cant be converted to zip because dont exists");
        }
        return null;
    }

    public static File unzipFile(File zipFile)
    {
        return null;
    }

    public static byte[] decodeString(String binaryString)
    {
        return Base64.getDecoder().decode(binaryString);
    }

    public static String encodeBytes(byte[] bytes)
    {
        return new String(Base64.getEncoder().encode(bytes));
    }

}
