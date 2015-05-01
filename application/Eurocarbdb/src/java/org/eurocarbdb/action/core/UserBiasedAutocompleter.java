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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date: 2010-02-23 01:50:46 +0900 (火, 23  2月 2010) $  
*/

package org.eurocarbdb.action.core;

//  3rd party imports 
import org.apache.log4j.Logger;

import java.util.List;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.*;

import org.eurocarbdb.action.RequiresLogin;
import org.eurocarbdb.action.UserAware;

import static org.eurocarbdb.util.StringUtils.join;

public class UserBiasedAutocompleter extends Autocompleter implements RequiresLogin
{
    private static final Logger log 
        = Logger.getLogger( UserBiasedAutocompleter.class.getName() );

    private Contributor contributor = null;

    public Contributor getContributor()
    {
        Contributor c = Eurocarb.getCurrentContributor();
        assert c != null;
        return c;
    }

    /**
     * Bias the results to taxa that the logged in user has contributed data for
     * before.
     */
    protected void findTaxonomiesMatchingString(String query) {
        for ( Taxonomy tax : getContributor().getMyTaxonomies() ) {
            List<String> synonyms = tax.getSynonyms();
            String resultString = synonyms.size() > 0 ? "("+join(",",synonyms)+")" : "";
            String searchString = tax.getName()+resultString;
            if (searchString.toLowerCase().contains(query.toLowerCase())) {
                addResult(tax.getName(),resultString,"user",tax);              
            }
        }
        super.findTaxonomiesMatchingString(query);
    }
    
    protected void findDiseasesMatchingString(String query) {
        for ( Disease disease : getContributor().getMyDiseases() ) {
            String resultString = disease.getDiseaseName();
            if (resultString.toLowerCase().contains(query.toLowerCase())) {
                log.debug("Adding user result "+resultString);
                AutocompleteResult res = addResult(resultString,"","user",disease);
                res.supplemental = join(" > ", disease.getAllParentDiseases());                
            }
        }
        super.findDiseasesMatchingString(query);        
    }

    protected void findTissuesMatchingString(String query) {
        for ( TissueTaxonomy tissue : getContributor().getMyTissueTaxonomies() ) {
            String resultString = tissue.getName();
            if (resultString.toLowerCase().contains(query.toLowerCase())) {
                log.debug("Adding user result "+resultString);
                AutocompleteResult res = addResult(resultString,"","user",tissue);
                res.supplemental = join( " > ", tissue.getAllParentTissueTaxonomies());
            }
        }
        super.findTissuesMatchingString(query);        
    }
    
}