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
* 
*/
package org.eurocarbdb.MolecularFramework.io.GlycoCT;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
* @author sherget
*
*/
public class LevelGZIPOutputStream extends GZIPOutputStream
{
/**
* Creates a new output stream with a default buffer size and
* sets the current compression level to the specified value.
*
* @param out the output stream.
* @param level the new compression level (0-9).
* @exception IOException If an I/O error has occurred.
* @exception IllegalArgumentException if the compression level is invalid.
*/
public LevelGZIPOutputStream( OutputStream out, int compressionLevel )
    throws IOException
{
  super( out );
  def.setLevel( compressionLevel );
}
}
