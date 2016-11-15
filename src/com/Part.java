/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

/**
 *
 * @author Honza
 */
public class Part
{
    private final int partNumber;
    private final int size;
    private final byte[] data;

    public Part(int partNumber, int size, byte[] data)
    {
        this.partNumber = partNumber;
        this.size = size;
        this.data = data;
    }

    public int getPartNumber()
    {
        return partNumber;
    }

    public int getSize()
    {
        return size;
    }

    public byte[] getData()
    {
        return data;
    }

}
