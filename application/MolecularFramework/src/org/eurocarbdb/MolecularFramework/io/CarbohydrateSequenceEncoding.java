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
package org.eurocarbdb.MolecularFramework.io;

public enum CarbohydrateSequenceEncoding
{
    linucs("linucs", "LINUCS Encoding", "LInear Notation for Unique description of Carbohydrate Structures developed by GLYCOSCIENCES.de."),
    bcsdb("bcsdb", "BCSDB sequences encoding", "Sequences encoding used in the bacterial carbohydrate structure database."),
    cfg("cfg","GlycoMindes encoding","Sequence encoding developed by GlycoMinds and used in CFG."),
    glycoct_xml("glycoct_xml","GlycoCT XML","GlycoCT XML encoding for carbohydrate sequences."),
    glycoct_condensed("glycoct_condensed","GlycoCT condensed","GlycoCT condensed encoding for carbohydrate sequences."),
    glyde("glyde","Glyde II","XML based exchange format for carbohydrate sequences."),
    kcf("kcf","KCF encoding","Sequence encoding used by Kegg."),
    ogbi("ogbi","OGBI motif encoding","Motif based encoding for carbohydrate sequences."),
    iupac_condenced("iupac_condenced","IUPAC condenced","Condenced version of the IUPAC encoding."),
    iupac_short_v1("iupac_short_v1","IUPAC short ver.1","short version of the IUPAC encoding (version 1)."),
    iupac_short_v2("iupac_short_v2","IUPAC short ver.2","Short version of the IUPAC encoding (version 2)."),
    carbbank("carbbank", "Carbbank encoding","ASCII 2D graph encoding used in Carbbank.");


    private String m_strId;
    private String m_strName;
    private String m_strComment;
    
    private CarbohydrateSequenceEncoding( String a_strId, String a_strName, String a_strComment )
    {
        this.m_strComment = a_strComment;
        this.m_strId  = a_strId;
        this.m_strName = a_strName;
    }

    public String getId() 
    {  
        return this.m_strId;  
    }

    public String getName() 
    {  
        return this.m_strName;  
    }

    public String getComment() 
    {  
        return this.m_strComment;  
    }

    public static CarbohydrateSequenceEncoding forId( String a_strId ) throws Exception
    {
        for ( CarbohydrateSequenceEncoding a : CarbohydrateSequenceEncoding.values() )
        {
            if ( a_strId.equalsIgnoreCase(a.m_strId ) ) 
            {
                return a;
            }
        }
        throw new Exception("Invalide ID for CarbohydrateSequenceEncoding.");
    }
}
