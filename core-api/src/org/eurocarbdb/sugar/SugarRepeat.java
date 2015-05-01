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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;

import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.Vertex;

// import org.eurocarbdb.sugar.SequenceFormat;
//import static org.eurocarbdb.util.graph.Graphs.unmodifiableGraph;

import static org.eurocarbdb.util.StringUtils.join;
// import static org.eurocarbdb.util.JavaUtils.checkNotNull;

/**
*   Subclass of {@link Sugar} designed to handle structures with internal
*   repeats.
*
*
*    
*    
*/
public class SugarRepeat extends Sugar
{
    /** Logging instance. */
    static final Logger log = Logger.getLogger( SugarRepeat.class );

    protected List<SugarRepeatAnnotation> repeats = new ArrayList<SugarRepeatAnnotation>( 2 );    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public SugarRepeat() { super(); }
    
    public SugarRepeat( int size ) { super( size ); }
    
    public SugarRepeat( Residue first ) { super( first ); }
    
    /**
    *   Pseudo-copy constructor that adds support for repeats to the 
    *   passed {@link Sugar}. Independent changes made to the passed
    *   Sugar *will be* reflected in the returned SugarRepeat.
    */
    public SugarRepeat( Sugar s )
    {
        this.graph = s.graph;
        this.sequence = s.sequence;
        this.rootResidue = s.rootResidue;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public void addRepeatAnnotation( SugarRepeatAnnotation r )
    {
        repeats.add( r );   
    }
    
    
    public List<SugarRepeatAnnotation> getRepeatAnnotations()
    {
        return repeats;
    }
    

    /** */
    @Override
    public int countResidues()
    {
        return 0;
    }


    @Override
    public boolean isDefinite()
    {
        for ( SugarRepeatAnnotation r : repeats )
        {
            if ( ! r.isDefinite() )
                return false;
        }
        
        return super.isDefinite();
    }
    
    
    @Override
    public String toString()
    {
        return super.toString()
            + "\nRepeats:"
            + join("\n    ", repeats )
        ;
    }
    
    
} // end class
