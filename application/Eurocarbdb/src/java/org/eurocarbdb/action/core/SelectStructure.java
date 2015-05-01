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
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.*;
import java.io.*;
import java.net.*;

//  3rd party imports 
import org.apache.log4j.Logger;
import org.hibernate.*; 
import org.hibernate.criterion.*; 

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;

import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;

import org.eurocarbdb.application.glycoworkbench.GlycanWorkspace;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.FileUtils;
import org.eurocarbdb.application.glycanbuilder.LogUtils;
import org.eurocarbdb.application.glycanbuilder.TextUtils;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.hibernate.*;

public class SelectStructure extends EurocarbAction implements RequiresLogin
{
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    static 
    {
        GlycanWorkspace gw = new GlycanWorkspace(); // init dictionaries
    }

    /** Logging handle. */
    private static final Logger log 
        = Logger.getLogger( ContributeStructure.class );
    
    private String sequenceGWS = null;
    private GlycanSequence glycanSequence = null;         

    // sequence file upload
    private File   sequenceFile            = null;
    private String sequenceFileContentType = null;
    private String sequenceFileFileName    = null;
    
    private String submitAction = null;
        
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public File getSequenceFile()
    {
        return sequenceFile;
    }
    
    public void setSequenceFile(File file)
    {
        sequenceFile = file;
    }
    
    public String getSequenceFileContentType()
    {
        return sequenceFileContentType;
    }
    
    public void setSequenceFileContentType(String strContentType)
    {
        sequenceFileContentType = strContentType;
    }
    
    public String getSequenceFileFileName()
    {
        return sequenceFileFileName;
    }
    
    public void setSequenceFileFileName(String strFileName)
    {
        sequenceFileFileName = strFileName;
    }

    public void setSubmitAction(String s) 
    {
        submitAction = s;
    }

    public String getSequenceGWS() 
    {
        return sequenceGWS;
    }

    public void setSequenceGWS(String str) 
    {
        sequenceGWS = str;
    }

    public void setGlycanSequence(GlycanSequence s) 
    {
        glycanSequence = s;
    }

    public GlycanSequence getGlycanSequence() 
    {
        return glycanSequence;
    }

    public String getGlycanSequenceAsGWS() 
    {
        if( glycanSequence==null ) {
            return "";
        }
        return glycanSequence.getSequenceGWS();
    }

   

    public String execute() throws Exception 
    {

    
        if( sequenceFile!=null ) 
        {
            // upload glycoct XML file
            String content = FileUtils.content(sequenceFile);
            Glycan glycan = Glycan.fromGlycoCT(content);
            if( glycan!=null ) 
                sequenceGWS = glycan.toString();
            else 
                addActionError("Error while parsing the GlycoCT file");
                
            return "input";
        }
    
        //*************************    
        // create a GlycanSequence from the sequenceGWS and go to the set context page
            
        if( sequenceGWS!=null && sequenceGWS.length()>0 ) {
            // create glycan from string
            Glycan g = Glycan.fromString(sequenceGWS);
            g.removeReducingEndModification();
        
            SugarSequence seq = new SugarSequence( g.toGlycoCTCondensed() );
            glycanSequence = GlycanSequence.lookupOrCreateNew( seq );

            if( glycanSequence.getGlycanSequenceId() == 0 )
                getEntityManager().store(glycanSequence);

            return "success";
        }

        addActionError("No glycan sequence given");
    
        // init the sequence
        glycanSequence = null;
        return "input";
    }

}
   
