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
// Generated Apr 3, 2007 6:49:19 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class TandemScanMethod  *//**********************************************
*
*
*/ 
public class TandemScanMethod extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int tandemScanMethodId;
      
    private Analyser analyser;
      
    private String tandemScanMethod;
      
    private Set<AnalyserParameter> analyserParameters = new HashSet<AnalyserParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public TandemScanMethod() {}

    /** Minimal constructor */
    public TandemScanMethod( Analyser analyser, String tandemScanMethod ) 
    {
        this.analyser = analyser;
        this.tandemScanMethod = tandemScanMethod;
    }
    
    /** full constructor */
    public TandemScanMethod( Analyser analyser, String tandemScanMethod, Set<AnalyserParameter> analyserParameters ) 
    {
        this.analyser = analyser;
        this.tandemScanMethod = tandemScanMethod;
        this.analyserParameters = analyserParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getTandemScanMethodId  *//******************************** 
    *
    */ 
    public int getTandemScanMethodId() 
    {
        return this.tandemScanMethodId;
    }
    
    
    /*  setTandemScanMethodId  *//******************************** 
    *
    */
    public void setTandemScanMethodId( int tandemScanMethodId ) 
    {
        this.tandemScanMethodId = tandemScanMethodId;
    }
    

    /*  getAnalyser  *//******************************** 
    *
    */ 
    public Analyser getAnalyser() 
    {
        return this.analyser;
    }
    
    
    /*  setAnalyser  *//******************************** 
    *
    */
    public void setAnalyser( Analyser analyser ) 
    {
        this.analyser = analyser;
    }
    

    /*  getTandemScanMethod  *//******************************** 
    *
    */ 
    public String getTandemScanMethod() 
    {
        return this.tandemScanMethod;
    }
    
    
    /*  setTandemScanMethod  *//******************************** 
    *
    */
    public void setTandemScanMethod( String tandemScanMethod ) 
    {
        this.tandemScanMethod = tandemScanMethod;
    }
    

    /*  getAnalyserParameters  *//******************************** 
    *
    */ 
    public Set<AnalyserParameter> getAnalyserParameters() 
    {
        return this.analyserParameters;
    }
    
    
    /*  setAnalyserParameters  *//******************************** 
    *
    */
    public void setAnalyserParameters( Set<AnalyserParameter> analyserParameters ) 
    {
        this.analyserParameters = analyserParameters;
    }
    
       

    







} // end class
