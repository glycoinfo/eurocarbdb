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
/**
* 
*/
package org.eurocarbdb.MolecularFramework.sugar;

/**
* @author rene
* complex means, only linkage position 1 is allowed for non complex substitutents
*/
public enum SubstituentType
{
    ACETYL("acetyl",1,1,false),
    ACYL("acyl",1,0, false),
    AMINO("amino",1,2, false),
    ANHYDRO("anhydro",2,2, false),
    BROMO("bromo",1,1, false),
    CHLORO("chloro",1,1, false),
    EPOXY("epoxy",2,0, false),
    ETHANOLAMINE("ethanolamine",1,0, true),
    ETHYL("ethyl",1,1, false),
    FLOURO("fluoro",1,1, false),
    FORMYL("formyl",1,1, false),
    GLYCOLYL("glycolyl",1,1, true),
    HYDROXYMETHYL("hydroxymethyl",1,1, true),
    IMINO("imino",1,2, false),
    IODO("iodo",1,1, false),
    LACTONE("lactone",2,0, false),
    METHYL("methyl",1,1, false),
    N_ACETYL("n-acetyl",1,1, false),
    N_ALANINE("n-alanine",1,1, true),
    N_DIMETHYL("n-dimethyl",1,1, false),
    N_FORMYL("n-formyl",1,1, false),
    N_GLYCOLYL("n-glycolyl",1,1, true),
    N_METHYL("n-methyl",1,1, false),
    N_SUCCINATE("n-succinate",1,0, true),
    SUCCINATE("succinate",1,0, true),
    N_SULFATE("n-sulfate",1,2, true),
    N_TRIFLOUROACETYL("n-triflouroacetyl",1,1, false),
    NITRATE("nitrate",1,1, false),
    PHOSPHATE("phosphate",1,3, false),
    PYRUVATE("pyruvate",2,0, true),
    PYROPHOSPHATE("pyrophosphate",1,3, true),
    TRIPHOSPHATE("triphosphate",1,3, true),
    R_LACTATE("(r)-lactate",1,2, true),
    R_PYRUVATE("(r)-pyruvate",2,2, true),
    S_LACTATE("(s)-lactate",1,2, true),
    S_PYRUVATE("(s)-pyruvate",2,2, true),
    SULFATE("sulfate",1,2, false),
    THIO("thio",1,1, false),
    AMIDINO("amidino",1,1, false),
    N_AMIDINO("n-amidino",1,1, false),
    R_CARBOXYMETHYL("(r)-carboxymethyl",1,1, false),
    S_CARBOXYMETHYL("(s)-carboxymethyl",1,1, false),
    R_CARBOXYETHYL("(r)-carboxyethyl",1,1, false),
    S_CARBOXYETHYL("(s)-carboxyethyl",1,1, false),
    N_METHYLCARBAMOYL("n-methyl-carbamoyl",1,1, true),
    PHOSPHO_ETHANOLAMINE("phospho-ethanolamine",1,2, true),    
    DIPHOSPHO_ETHANOLAMINE("diphospho-ethanolamine",1,2, true),
    PHOSPHO_CHOLINE("phospho-choline",1,1, true),
    X_LACTATE("(x)-lactate",1,2, true),
    X_PYRUVATE("(x)-pyruvate",2,2, true);  
    
    private String m_strName;
    private Integer minValence;
    private Integer maxValence;
    private Boolean complex;
    
    private SubstituentType( String a_strName, Integer a_minValence, Integer a_maxValence, Boolean a_complex )
    {
        this.m_strName = a_strName;
        this.minValence = a_minValence;
        this.complex = a_complex;
        this.maxValence = a_maxValence;
        
    }

    public static SubstituentType forName( String a_strName )
    {
        String t_strName = a_strName.toUpperCase();
        for ( SubstituentType t_objType : SubstituentType.values() )
        {
            if ( t_objType.m_strName.equalsIgnoreCase(t_strName) )
            {
                return t_objType;
            }
        }
        return null;
    }       
    
    public String getName()
    {
        return this.m_strName;
    }
    
    public Integer getMinValence()
    {
        return this.minValence;
    }
    
    public Integer getMaxValence()
    {
        return this.maxValence;
    }
    
    public Boolean getComplexType (){
        return this.complex;
    }
}



