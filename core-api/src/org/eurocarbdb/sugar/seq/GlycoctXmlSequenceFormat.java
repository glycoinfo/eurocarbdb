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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar.seq;

import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.Modification;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.SequenceFormatException;


/*  class GlycoctXmlSequenceFormat  *//*********************************
*
*   Implements parsing and generation of carbohydrate sequences in 
*   GlycoCT format.
*/
public class GlycoctXmlSequenceFormat implements SequenceFormat
{   
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( GlycoctXmlSequenceFormat.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~//

    
    //  (no constructors)
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    

    /*  getName  *//*************************************************  
    *
    *   Returns "GlycoCT-XML", the name of this format.
    */
    public String getName() {  return "GlycoCT-XML";  }


    /*  getSugar  *//******************************************* 
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public Sugar getSugar( String sequence ) throws SequenceFormatException
    {
        //  rene's code here
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }

    
    /*  getMonosaccharide  *//*************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException
    */
    public Monosaccharide getMonosaccharide( String monosac_seq ) 
    throws SequenceFormatException
    {
        //  rene's code here
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }


    /*  getSubstituent  *//******************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException
    */
    public Substituent getSubstituent( String seq ) throws SequenceFormatException
    {
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }
    
    
    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public String getSequence( Sugar s )
    {
        //  rene's code here
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }
    
    
    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException
    */
    public String getSequence( Monosaccharide m )    
    {
        //  rene's code here
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }
   
    
    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException
    */
    public String getSequence( Substituent s )    
    {
        //  rene's code here
        throw new UnsupportedOperationException("need to add rene/stephan code here");
    }
 
    
    /** {@inheritDoc}  @see SequenceFormat#getSequence(Residue)  */
    public String getSequence( Residue r )
    {
        return r.getName();
    }
    
} // end class


