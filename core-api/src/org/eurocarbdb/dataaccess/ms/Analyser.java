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
// Generated Apr 3, 2007 6:49:17 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.ms;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Analyser  *//**********************************************
*
*
*/ 
public class Analyser extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int analyserId;
      
    private Device device;
      
    private String model;
      
    private String analyserType;
      
    private double accuracy;
      
    private double scanRate;
      
    private double scanTime;
      
    private String scanDirection;
      
    private String scanLaw;
      
    private Double tofPathLength;
      
    private Double isolationWidth;
      
    private Double magneticFieldStrengh;
      
    private int finalMsExponent;
      
    private Set<TandemScanMethod> tandemScanMethods = new HashSet<TandemScanMethod>(0);
      
    private Set<AnalyserParameter> analyserParameters = new HashSet<AnalyserParameter>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Analyser() {}

    /** Minimal constructor */
    public Analyser( Device device, String analyserType, double accuracy, double scanRate, double scanTime, String scanDirection, String scanLaw, int finalMsExponent ) 
    {
        this.device = device;
        this.analyserType = analyserType;
        this.accuracy = accuracy;
        this.scanRate = scanRate;
        this.scanTime = scanTime;
        this.scanDirection = scanDirection;
        this.scanLaw = scanLaw;
        this.finalMsExponent = finalMsExponent;
    }
    
    /** full constructor */
    public Analyser( Device device, String model, String analyserType, double accuracy, double scanRate, double scanTime, String scanDirection, String scanLaw, Double tofPathLength, Double isolationWidth, Double magneticFieldStrengh, int finalMsExponent, Set<TandemScanMethod> tandemScanMethods, Set<AnalyserParameter> analyserParameters ) 
    {
        this.device = device;
        this.model = model;
        this.analyserType = analyserType;
        this.accuracy = accuracy;
        this.scanRate = scanRate;
        this.scanTime = scanTime;
        this.scanDirection = scanDirection;
        this.scanLaw = scanLaw;
        this.tofPathLength = tofPathLength;
        this.isolationWidth = isolationWidth;
        this.magneticFieldStrengh = magneticFieldStrengh;
        this.finalMsExponent = finalMsExponent;
        this.tandemScanMethods = tandemScanMethods;
        this.analyserParameters = analyserParameters;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getAnalyserId  *//******************************** 
    *
    */ 
    public int getAnalyserId() 
    {
        return this.analyserId;
    }
    
    
    /*  setAnalyserId  *//******************************** 
    *
    */
    public void setAnalyserId( int analyserId ) 
    {
        this.analyserId = analyserId;
    }
    

    /*  getDevice  *//******************************** 
    *
    */ 
    public Device getDevice() 
    {
        return this.device;
    }
    
    
    /*  setDevice  *//******************************** 
    *
    */
    public void setDevice( Device device ) 
    {
        this.device = device;
    }
    

    /*  getModel  *//******************************** 
    *
    */ 
    public String getModel() 
    {
        return this.model;
    }
    
    
    /*  setModel  *//******************************** 
    *
    */
    public void setModel( String model ) 
    {
        this.model = model;
    }
    

    /*  getAnalyserType  *//******************************** 
    *
    */ 
    public String getAnalyserType() 
    {
        return this.analyserType;
    }
    
    
    /*  setAnalyserType  *//******************************** 
    *
    */
    public void setAnalyserType( String analyserType ) 
    {
        this.analyserType = analyserType;
    }
    

    /*  getAccuracy  *//******************************** 
    *
    */ 
    public double getAccuracy() 
    {
        return this.accuracy;
    }
    
    
    /*  setAccuracy  *//******************************** 
    *
    */
    public void setAccuracy( double accuracy ) 
    {
        this.accuracy = accuracy;
    }
    

    /*  getScanRate  *//******************************** 
    *
    */ 
    public double getScanRate() 
    {
        return this.scanRate;
    }
    
    
    /*  setScanRate  *//******************************** 
    *
    */
    public void setScanRate( double scanRate ) 
    {
        this.scanRate = scanRate;
    }
    

    /*  getScanTime  *//******************************** 
    *
    */ 
    public double getScanTime() 
    {
        return this.scanTime;
    }
    
    
    /*  setScanTime  *//******************************** 
    *
    */
    public void setScanTime( double scanTime ) 
    {
        this.scanTime = scanTime;
    }
    

    /*  getScanDirection  *//******************************** 
    *
    */ 
    public String getScanDirection() 
    {
        return this.scanDirection;
    }
    
    
    /*  setScanDirection  *//******************************** 
    *
    */
    public void setScanDirection( String scanDirection ) 
    {
        this.scanDirection = scanDirection;
    }
    

    /*  getScanLaw  *//******************************** 
    *
    */ 
    public String getScanLaw() 
    {
        return this.scanLaw;
    }
    
    
    /*  setScanLaw  *//******************************** 
    *
    */
    public void setScanLaw( String scanLaw ) 
    {
        this.scanLaw = scanLaw;
    }
    

    /*  getTofPathLength  *//******************************** 
    *
    */ 
    public Double getTofPathLength() 
    {
        return this.tofPathLength;
    }
    
    
    /*  setTofPathLength  *//******************************** 
    *
    */
    public void setTofPathLength( Double tofPathLength ) 
    {
        this.tofPathLength = tofPathLength;
    }
    

    /*  getIsolationWidth  *//******************************** 
    *
    */ 
    public Double getIsolationWidth() 
    {
        return this.isolationWidth;
    }
    
    
    /*  setIsolationWidth  *//******************************** 
    *
    */
    public void setIsolationWidth( Double isolationWidth ) 
    {
        this.isolationWidth = isolationWidth;
    }
    

    /*  getMagneticFieldStrengh  *//******************************** 
    *
    */ 
    public Double getMagneticFieldStrengh() 
    {
        return this.magneticFieldStrengh;
    }
    
    
    /*  setMagneticFieldStrengh  *//******************************** 
    *
    */
    public void setMagneticFieldStrengh( Double magneticFieldStrengh ) 
    {
        this.magneticFieldStrengh = magneticFieldStrengh;
    }
    

    /*  getFinalMsExponent  *//******************************** 
    *
    */ 
    public int getFinalMsExponent() 
    {
        return this.finalMsExponent;
    }
    
    
    /*  setFinalMsExponent  *//******************************** 
    *
    */
    public void setFinalMsExponent( int finalMsExponent ) 
    {
        this.finalMsExponent = finalMsExponent;
    }
    

    /*  getTandemScanMethods  *//******************************** 
    *
    */ 
    public Set<TandemScanMethod> getTandemScanMethods() 
    {
        return this.tandemScanMethods;
    }
    
    
    /*  setTandemScanMethods  *//******************************** 
    *
    */
    public void setTandemScanMethods( Set<TandemScanMethod> tandemScanMethods ) 
    {
        this.tandemScanMethods = tandemScanMethods;
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
