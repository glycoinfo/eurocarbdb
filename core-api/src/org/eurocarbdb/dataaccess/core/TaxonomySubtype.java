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
package org.eurocarbdb.dataaccess.core;
// Generated 3/08/2006 16:48:24 by Hibernate Tools 3.1.0.beta4

import java.io.Serializable;

import org.eurocarbdb.dataaccess.BasicEurocarbObject;

/**
* TaxonomySubtype
*/

public class TaxonomySubtype extends BasicEurocarbObject implements Serializable 
{

    // Fields    

    private Taxonomy taxonomy;
    private Taxonomy subTaxonomy;
    private String taxon;

    // Constructors

    /** default constructor */
    public TaxonomySubtype() 
    {
    }    
   
    // Property accessors

    public Taxonomy getTaxonomy() 
    {
        return this.taxonomy;
    }
    
    public void setTaxonomy(Taxonomy taxonomy) 
    {
        this.taxonomy = taxonomy;
    }

    public Taxonomy getSubTaxonomy() 
    {
        return this.subTaxonomy;
    }
    
    public void setSubTaxonomy(Taxonomy subTaxonomy) 
    {
        this.subTaxonomy = subTaxonomy;
    }   

    /*  getTaxon  *//************************************************
    *
    *   Returns the name of this taxon.
    */
    public String getTaxon() 
    {
        if ( this.taxon == "root" ) return "Taxonomy";
        return this.taxon;
    }
    
    /*  setTaxon  *//************************************************
    *
    *   Sets the name of this taxon.
    */
    public void setTaxon( String taxon ) 
    {
        if ( taxon == "Taxonomy" ) taxon = "root";
        this.taxon = taxon;
    }

    public boolean equals(Object other) {
    if( other==null )
        return false;
    if( !(other instanceof TaxonomySubtype) )
        return false;

    TaxonomySubtype ts = (TaxonomySubtype)other;
    return (this.taxonomy.getTaxonomyId()==ts.taxonomy.getTaxonomyId() && 
        this.subTaxonomy.getTaxonomyId()==ts.subTaxonomy.getTaxonomyId());
    }

    public int hashCode() {
    return taxonomy.hashCode() + subTaxonomy.hashCode();
    }
   
}
