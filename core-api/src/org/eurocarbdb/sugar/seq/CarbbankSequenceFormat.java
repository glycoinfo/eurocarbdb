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

/*
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCT;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
*/

/*  class CarbbankSequenceFormat  *//*********************************
*
*   Implements parsing and generation of carbohydrate sequences in 
*   Carbbank format.
*/
public class CarbbankSequenceFormat implements SequenceFormat
{   
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( CarbbankSequenceFormat.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~//

    

    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    

    /*  getName  *//*************************************************  
    *
    *   Returns "Carbbank", the name of this format.
    *   @see SequenceFormat#getName()  
    */
    public String getName() {  return "Carbbank";  }


    /*  getSugar  *//******************************************* 
    *
    *   Not yet implemented. @throws UnsupportedOperationException
    */
    public Sugar getSugar( String sequence ) throws SequenceFormatException
    {
        //  rene's code here
        /*
        SugarImporterCarbbank carbbank_importer = new SugarImporterCarbbank();
        
        GlycoVisitorToGlycoCT glycoct_visitor 
            = new GlycoVisitorToGlycoCT(  
                    new MonosaccharideConverter( 
                            new Config() ) );
            
        Sugar sugar = carbbank_importer.parse( sequence );
        
        glycoct_visitor.start(sugar);
        sugar = glycoct_visitor.getNormalizedSugar();
        
        SugarExporterGlycoCT glycoct_exporter = new SugarExporterGlycoCT();
        glycoct_exporter.start(sugar);
        String result = glycoct_exporter.getXMLCode();
        */
        
        throw new UnsupportedOperationException("Not yet implemented");
        
    }

    
    /*  getMonosaccharide  *//*************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public Monosaccharide getMonosaccharide( String monosac_seq ) 
    throws SequenceFormatException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    
    /*  parseSubstituent  *//****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public Substituent getSubstituent( String seq ) 
    throws SequenceFormatException
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    

    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public String getSequence( Sugar s )
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    
    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public String getSequence( Monosaccharide m )    
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    
    /*  getSequence  *//*****************************************
    *
    *   Not yet implemented. @throws UnsupportedOperationException 
    */
    public String getSequence( Substituent s )    
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /** {@inheritDoc}  @see SequenceFormat#getSequence(Residue)  */
    public String getSequence( Residue r )
    {
        return r.getName();
    }
    
} // end class


