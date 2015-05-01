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
// Generated Jun 21, 2007 2:07:01 PM by Hibernate Tools 3.1.0.beta4

package org.eurocarbdb.dataaccess.hplc;

//  stdlib imports
import java.util.HashSet;
import java.util.Set;

import java.io.Serializable;

//  eurocarb imports
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/*  class Digest  *//**********************************************
*
*
*/ 
public class Digest extends BasicEurocarbObject implements Serializable 
{


    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
      
    private int digestId;
      
    private String name;
      
    private String enzymeOne;
      
    private int targetOne;
      
    private String enzymeTwo;
      
    private int targetTwo;
      
    private String enzymeThree;
      
    private int targetThree;
      
    private String enzymeFour;
      
    private int targetFour;
      
    private String enzymeFive;
      
    private int targetFive;
      
    private Integer glycanId;
      
    private Set<ProfileData> profileDatas = new HashSet<ProfileData>(0);
      
    private Set<HplcPeaksIntegrated> hplcPeaksIntegrateds = new HashSet<HplcPeaksIntegrated>(0);
      
    private Set<IntegrationMethod> integrationMethods = new HashSet<IntegrationMethod>(0);


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Default constructor. */
    public Digest() {}

    /** Minimal constructor */
    public Digest( String enzymeOne, int targetOne, String enzymeTwo, int targetTwo, String enzymeThree, int targetThree, String enzymeFour, int targetFour, String enzymeFive, int targetFive ) 
    {
        this.enzymeOne = enzymeOne;
        this.targetOne = targetOne;
        this.enzymeTwo = enzymeTwo;
        this.targetTwo = targetTwo;
        this.enzymeThree = enzymeThree;
        this.targetThree = targetThree;
        this.enzymeFour = enzymeFour;
        this.targetFour = targetFour;
        this.enzymeFive = enzymeFive;
        this.targetFive = targetFive;
    }
    
    /** full constructor */
    public Digest( String name, String enzymeOne, int targetOne, String enzymeTwo, int targetTwo, String enzymeThree, int targetThree, String enzymeFour, int targetFour, String enzymeFive, int targetFive, Integer glycanId, Set<ProfileData> profileDatas, Set<HplcPeaksIntegrated> hplcPeaksIntegrateds, Set<IntegrationMethod> integrationMethods ) 
    {
        this.name = name;
        this.enzymeOne = enzymeOne;
        this.targetOne = targetOne;
        this.enzymeTwo = enzymeTwo;
        this.targetTwo = targetTwo;
        this.enzymeThree = enzymeThree;
        this.targetThree = targetThree;
        this.enzymeFour = enzymeFour;
        this.targetFour = targetFour;
        this.enzymeFive = enzymeFive;
        this.targetFive = targetFive;
        this.glycanId = glycanId;
        this.profileDatas = profileDatas;
        this.hplcPeaksIntegrateds = hplcPeaksIntegrateds;
        this.integrationMethods = integrationMethods;
    }
    

   
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    /*  getDigestId  *//******************************** 
    *
    */ 
    public int getDigestId() 
    {
        return this.digestId;
    }
    
    
    /*  setDigestId  *//******************************** 
    *
    */
    public void setDigestId( int digestId ) 
    {
        this.digestId = digestId;
    }
    

    /*  getName  *//******************************** 
    *
    */ 
    public String getName() 
    {
        return this.name;
    }
    
    
    /*  setName  *//******************************** 
    *
    */
    public void setName( String name ) 
    {
        this.name = name;
    }
    

    /*  getEnzymeOne  *//******************************** 
    *
    */ 
    public String getEnzymeOne() 
    {
        return this.enzymeOne;
    }
    
    
    /*  setEnzymeOne  *//******************************** 
    *
    */
    public void setEnzymeOne( String enzymeOne ) 
    {
        this.enzymeOne = enzymeOne;
    }
    

    /*  getTargetOne  *//******************************** 
    *
    */ 
    public int getTargetOne() 
    {
        return this.targetOne;
    }
    
    
    /*  setTargetOne  *//******************************** 
    *
    */
    public void setTargetOne( int targetOne ) 
    {
        this.targetOne = targetOne;
    }
    

    /*  getEnzymeTwo  *//******************************** 
    *
    */ 
    public String getEnzymeTwo() 
    {
        return this.enzymeTwo;
    }
    
    
    /*  setEnzymeTwo  *//******************************** 
    *
    */
    public void setEnzymeTwo( String enzymeTwo ) 
    {
        this.enzymeTwo = enzymeTwo;
    }
    

    /*  getTargetTwo  *//******************************** 
    *
    */ 
    public int getTargetTwo() 
    {
        return this.targetTwo;
    }
    
    
    /*  setTargetTwo  *//******************************** 
    *
    */
    public void setTargetTwo( int targetTwo ) 
    {
        this.targetTwo = targetTwo;
    }
    

    /*  getEnzymeThree  *//******************************** 
    *
    */ 
    public String getEnzymeThree() 
    {
        return this.enzymeThree;
    }
    
    
    /*  setEnzymeThree  *//******************************** 
    *
    */
    public void setEnzymeThree( String enzymeThree ) 
    {
        this.enzymeThree = enzymeThree;
    }
    

    /*  getTargetThree  *//******************************** 
    *
    */ 
    public int getTargetThree() 
    {
        return this.targetThree;
    }
    
    
    /*  setTargetThree  *//******************************** 
    *
    */
    public void setTargetThree( int targetThree ) 
    {
        this.targetThree = targetThree;
    }
    

    /*  getEnzymeFour  *//******************************** 
    *
    */ 
    public String getEnzymeFour() 
    {
        return this.enzymeFour;
    }
    
    
    /*  setEnzymeFour  *//******************************** 
    *
    */
    public void setEnzymeFour( String enzymeFour ) 
    {
        this.enzymeFour = enzymeFour;
    }
    

    /*  getTargetFour  *//******************************** 
    *
    */ 
    public int getTargetFour() 
    {
        return this.targetFour;
    }
    
    
    /*  setTargetFour  *//******************************** 
    *
    */
    public void setTargetFour( int targetFour ) 
    {
        this.targetFour = targetFour;
    }
    

    /*  getEnzymeFive  *//******************************** 
    *
    */ 
    public String getEnzymeFive() 
    {
        return this.enzymeFive;
    }
    
    
    /*  setEnzymeFive  *//******************************** 
    *
    */
    public void setEnzymeFive( String enzymeFive ) 
    {
        this.enzymeFive = enzymeFive;
    }
    

    /*  getTargetFive  *//******************************** 
    *
    */ 
    public int getTargetFive() 
    {
        return this.targetFive;
    }
    
    
    /*  setTargetFive  *//******************************** 
    *
    */
    public void setTargetFive( int targetFive ) 
    {
        this.targetFive = targetFive;
    }
    

    /*  getGlycanId  *//******************************** 
    *
    */ 
    public Integer getGlycanId() 
    {
        return this.glycanId;
    }
    
    
    /*  setGlycanId  *//******************************** 
    *
    */
    public void setGlycanId( Integer glycanId ) 
    {
        this.glycanId = glycanId;
    }
    

    /*  getProfileDatas  *//******************************** 
    *
    */ 
    public Set<ProfileData> getProfileDatas() 
    {
        return this.profileDatas;
    }
    
    
    /*  setProfileDatas  *//******************************** 
    *
    */
    public void setProfileDatas( Set<ProfileData> profileDatas ) 
    {
        this.profileDatas = profileDatas;
    }
    

    /*  getHplcPeaksIntegrateds  *//******************************** 
    *
    */ 
    public Set<HplcPeaksIntegrated> getHplcPeaksIntegrateds() 
    {
        return this.hplcPeaksIntegrateds;
    }
    
    
    /*  setHplcPeaksIntegrateds  *//******************************** 
    *
    */
    public void setHplcPeaksIntegrateds( Set<HplcPeaksIntegrated> hplcPeaksIntegrateds ) 
    {
        this.hplcPeaksIntegrateds = hplcPeaksIntegrateds;
    }
    

    /*  getIntegrationMethods  *//******************************** 
    *
    */ 
    public Set<IntegrationMethod> getIntegrationMethods() 
    {
        return this.integrationMethods;
    }
    
    
    /*  setIntegrationMethods  *//******************************** 
    *
    */
    public void setIntegrationMethods( Set<IntegrationMethod> integrationMethods ) 
    {
        this.integrationMethods = integrationMethods;
    }
    
} // end class
