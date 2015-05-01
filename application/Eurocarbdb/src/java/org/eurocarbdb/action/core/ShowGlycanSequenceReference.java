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

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.AbstractShowDetailAction;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.core.JournalReference;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.GlycanSequenceReference;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class ShowGlycanSequenceReference  *//****************************************
*
*   Shows a detail page for a {@link GlycanSequenceReference}.
*
*   @author mjh
*/

public class ShowGlycanSequenceReference 
extends AbstractShowDetailAction<GlycanSequenceReference>
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    static final Logger log = Logger.getLogger( ShowGlycanSequenceReference.class );

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public Class<GlycanSequenceReference> getGenericType()
    {
        return GlycanSequenceReference.class;           
    }
    
    
    public void setGlycanSequenceReferenceId( int id )
    {
        super.setId( id );   
    }
    
} // end class
