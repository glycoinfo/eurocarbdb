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

package org.eurocarbdb.action.devel;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormatException;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.DeveloperAction;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import org.eurocarbdb.util.ProgressWatchable;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*   Sequentially parses all {@link GlycanSequence}s in the data store.
*
*   This is a long running action. Most execution time is spent in the
*   {@link #execute()} method, though execution can be stopped by calling
*   {@link #stop()}.
*/
public class TestGlycanSequenceParsing extends EurocarbAction 
implements DeveloperAction, ProgressWatchable
{

    private final List<GlycanSequence> seq_fails = new ArrayList<GlycanSequence>(128);
        
    private final List<SequenceFormatException> seq_excepts = new ArrayList<SequenceFormatException>(128);
        
    private final Set<Exception> other_excepts = new HashSet<Exception>( 0 );
            
    private int successful = 0;
    
    private int total = 0;
            
    private int totalSequences = -1;            
    
    private boolean stop = false;
    
    private long startTimeMsec;
    
    
    /** Percent of sequences that have been parsed compared to total */
    public int getPercentComplete() 
    {
        if ( totalSequences <= 0 ) 
            return 0;
        
        return (int) (total * 100 / totalSequences);
    }
    
    
    /** Percent of sequences that have successfully parsed so far */
    public int getPercentSuccessful()
    {
        if ( total <= 0 ) 
            return 0;
        
        return (int) (successful * 100 / total);    
    }
    
    
    public int getMillisecsElapsed() 
    {
        return (int) (now() - startTimeMsec);
    }
    
    
    /** Number of sequences that have successfully parsed so far */
    public int getCountSuccessful() 
    {
        return successful;    
    }
    

    /** Number of total sequences parsed so far */    
    public int getCountTotal() 
    {
        return total;    
    }
    
    
    /** Number of total sequences in the data store */    
    public int getCountTotalSequences() 
    {
        return totalSequences;    
    }
    
    
    public void setStop( boolean b )
    {
        synchronized( this )
        {
            log.debug("execution manually stopped");
            stop = true;
        }
    }
    
    
    /** List of sequences that produced parse exceptions so far */
    public List<GlycanSequence> getFailedSequences()
    {
        return seq_fails;   
    }
    

    /** 
    *   The list of sequence format exceptions produced so far 
    *   (has same length/indexes as {@link #getFailedSequences}). 
    */
    public List<SequenceFormatException> getFailedSequenceExceptions()
    {
        return seq_excepts;   
    }
    

    /** List of any other exceptions that may have been thrown during parsing so far. */
    public Set<Exception> getOtherExceptions()
    {
        return other_excepts;   
    }
    
    
    /** Performs parsing. */
    public String execute()
    {
        getEntityManager().beginUnitOfWork();
        totalSequences = getEntityManager().countAll( GlycanSequence.class ); 
        
        Iterator<GlycanSequence> i 
            = (Iterator<GlycanSequence>) getEntityManager()
                .getQuery( GlycanSequence.class.getName() + ".ALL" )
                .setReadOnly( true )
                .iterate();
            
        if ( i == null )
            return "success";
        
        GlycanSequence gs = null;
        SugarSequence ss = null;
        Sugar sugar = null;
        
        this.startTimeMsec = now();
        
        while ( i.hasNext() && !stop )
        {
            try
            {
                gs = i.next();
                ss = gs.getSugarSequence(); 
                sugar = ss.getSugar();
                successful++;
            }
            catch ( SequenceFormatException ex )
            {
                seq_fails.add( gs );
                seq_excepts.add( ex );
                if ( seq_fails.size() > 512 ) break;
            }
            catch ( Exception other_ex )
            {
                other_excepts.add( other_ex );      
                if ( other_excepts.size() > 512 ) break;
            }
            
            total++;
        }
        
        getEntityManager().endUnitOfWork();
        
        return "success";
    }
    
    
    /** Stops parsing, forcing a return from {@link #execute()}. */
    public void stop()
    {
        synchronized( this )
        {
            log.debug("execution manually stopped");
            stop = true;
        }
    }
    
    
    private static final long now()
    {
        return System.currentTimeMillis(); 
    }

} // end class


