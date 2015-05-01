/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
/**
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MMFCreator {
    
    private long last_position;
    private File theFile;
    private RandomAccessFile theRandomAccessFile;
    private FileOutputStream theStream;
    private DataOutputStream theDataStream;

    public MMFCreator() throws Exception {
    last_position = 0;
    theFile = File.createTempFile("gwb",null);
    theFile.deleteOnExit();    
    theRandomAccessFile = new RandomAccessFile(theFile,"rw");
    theStream = new FileOutputStream(theFile);
    theDataStream = new DataOutputStream(new BufferedOutputStream(theStream));
    }


    public void addByte(byte b) throws Exception {
    theDataStream.writeByte(b);
    }

    public void addDouble(double v) throws Exception {
    theDataStream.writeDouble(v);
    }

    public long getLastPosition() {
    return last_position;
    }

    public long getCurrentPosition() throws Exception {
    return theStream.getChannel().position();
    }
   
    public Pointer getPointerFromBeginning() throws Exception {
    theDataStream.flush();
    FileChannel fc = theStream.getChannel();
    long size = fc.position();    
    Pointer ret = new Pointer(this,0,size);
    last_position = size;
    return ret;
    }

    public Pointer getPointerFromLast() throws Exception {
    theDataStream.flush();
    FileChannel fc = theStream.getChannel();
    long size = fc.position();    
    Pointer ret = new Pointer(this,last_position,size-last_position);
    last_position = size;
    return ret;
    }

    public static class Pointer {

    private MMFCreator mmfc;
    private long position;
    private long size;

    public Pointer(MMFCreator _mmfc, long _position, long _size) {
        mmfc = _mmfc;
        position = _position;
        size = _size;
    }
   
    public MappedByteBuffer getBuffer(boolean read_only) throws Exception {
        FileChannel fc = mmfc.theRandomAccessFile.getChannel();
        if( read_only ) 
        return fc.map(FileChannel.MapMode.READ_ONLY,position,size);
        return fc.map(FileChannel.MapMode.READ_WRITE,position,size);
    }

    public MMFCreator getCreator() {
        return mmfc;
    }

    public long getPosition() {
        return position;
    }

    public long getSize() {
        return size;
    }
    }
}