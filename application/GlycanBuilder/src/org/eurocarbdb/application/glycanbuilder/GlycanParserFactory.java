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
*   Last commit: $Rev: 1886 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-03-09 #$  
*/

package org.eurocarbdb.application.glycanbuilder;

import java.util.*;

/**
   Factory class used to create instances of parsers for glycan
   structure encoding formats.
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class GlycanParserFactory {

    /**
       Return a map of the supported formats for importing glycan
       structures. The map contains the identifier and the description
       of each format.
     */
    public static Map<String,String> getImportFormats() {
    return getImportFormats(false);
    }

    /**
       Return a map of the supported formats for exporting glycan
       structures. The map contains the identifier and the description
       of each format.
       @param add_internal if <code>true</code> add the internal
       GlycoWorkbench formats to the map
     */
    public static Map<String,String> getImportFormats(boolean add_internal) {
    Map<String,String> ret = MolecularFrameworkParser.getImportFormats();
    if( add_internal )
        ret.put("GWS","GlycoWorkbench sequence");
    ret.put("glycominds","Glycominds");
    ret.put("gwlinucs","Linucs");
    return ret;
    }

    /**
       Return a map of the supported formats for exporting glycan
       structures. The map contains the identifier and the description
       of each format.
     */
    public static Map<String,String> getExportFormats() {
    Map<String,String> ret =  MolecularFrameworkParser.getExportFormats();
    ret.put("glycominds","Glycominds");
    return ret;
    }

    /**
       Return a map of all the supported formats for glycan
       structures. The map contains the identifier and the description
       of each format.
     */
    static public Map<String,String> getFormats() {
    Map<String,String> ret = MolecularFrameworkParser.getFormats();
    ret.put("GWS","GlycoWorkbench sequence");
    ret.put("glycominds","Glycominds");
    ret.put("gwlinucs","Linucs");
    return ret;
    }

    /**
       Return <code>true</code> if the string identifies a supported
       format.
     */
    static public boolean isSequenceFormat(String format) {
    return getFormats().containsKey(format);
    }
    
    public enum GlycanSequenceFormat {
    	GWS("gws"),
	GlycoMinds("Glycominds"),
	GwLinucs("Linucs");
	
    	String format;
    	GlycanSequenceFormat(String format){
    		this.format=format;
    	}
    	
    	public String toString(){
    		return this.format;
    	}
    	
    }
    
    /**
       Create a new instance of a glycan structure parser for a given
       format (Call getParser(GlycanSequenceFormat) instead.)
       @param format the identifier of the encoding format
       @throws Exception if the identifier does not represent a valid format
       @deprecated
     */
    static public GlycanParser getParser(String format) throws Exception{

    // molecular framework formats
    if( MolecularFrameworkParser.isSequenceFormat(format) )
        return new MolecularFrameworkParser(format);

    // internal formats
    if( format.compareToIgnoreCase("gws")==0 ) 
        return new GWSParser();
    else if( format.compareToIgnoreCase("gwlinucs")==0 ) 
        return new LinucsParser();
    else if( format.compareToIgnoreCase("glycominds")==0 ) 
        return new GlycoMindsParser();
    else if( format.compareToIgnoreCase("glycoct")==0 ) 
        return new GlycoCTParser(false);
    else if( format.compareToIgnoreCase("glycoct_condensed")==0 ) 
        return new GlycoCTParser(false);

    throw new Exception("Unsupported format " + format);
    }
    
    static public GlycanParser getParser(GlycanSequenceFormat glycanSequenceFormat) throws Exception{
    	return getParser(glycanSequenceFormat.toString());
    }
}