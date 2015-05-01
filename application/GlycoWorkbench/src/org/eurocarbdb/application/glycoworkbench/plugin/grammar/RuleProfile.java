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
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

package org.eurocarbdb.application.glycoworkbench.plugin.grammar;

import org.eurocarbdb.application.glycoworkbench.*;
import org.eurocarbdb.application.glycanbuilder.*;
import java.util.*;

public class RuleProfile {
        
    private TreeMap<Rule,Double> data = new TreeMap<Rule,Double>();

    public RuleProfile() {
    }

    public RuleProfile(Collection<Rule> rules) {
    addAll(rules);
    }

    public void clear() {
    data.clear();
    }    

    public void addAll(Collection<Rule> rules) {
    if( rules!=null ) 
        for(Rule r : rules) 
        add(r);        
    }

    public boolean add(Rule r) {
    return add(r,1.);
    }

    public boolean add(Rule r, double value) {
    if( r==null )
        return false;

    Double oldvalue = data.get(r);
    if( oldvalue==null )
        data.put(r,value);
    else
        data.put(r,oldvalue+value);
    return true;
    }

    public Double get(Rule r) {
    return data.get(r);
    }

    public Set<Map.Entry<Rule,Double>> getEntries() {
    return data.entrySet();
    }
   
    public RuleProfile clone() {
    RuleProfile ret = new RuleProfile();
    ret.data.putAll(this.data);
    return ret;
    }

    public RuleProfile intersection(RuleProfile other) {
    RuleProfile ret = this.clone();
    if( other!=null ) {
        for( Map.Entry<Rule,Double> e : other.getEntries() ) 
        ret.add(e.getKey(),-e.getValue());        
    }
    return ret;
    }

    public double absSum() {
    double ret = 0.;
    for( Map.Entry<Rule,Double> e : this.getEntries() ) 
        ret += Math.abs(e.getValue());
    return ret;
    }
 
    public double distance(RuleProfile other) {
    if( other==null )
        return absSum();

    double ret = 0.;

    Map.Entry<Rule,Double>[] arr1 = (Map.Entry<Rule,Double>[])this.getEntries().toArray();
    Map.Entry<Rule,Double>[] arr2 = (Map.Entry<Rule,Double>[])other.getEntries().toArray();
            
    int i=0,l=0;
    while( i<arr1.length || l<arr2.length ) {
        if( l==arr2.length )
        ret += Math.abs(arr1[i++].getValue());
        else if( i==arr1.length ) 
        ret += Math.abs(arr2[l++].getValue());
        else {
        int compare = arr1[i].getKey().compareTo(arr2[l].getKey());
        if( compare==0 )
            ret += Math.abs(arr1[i++].getValue()-arr2[l++].getValue());
        else if( compare == -1 ) 
            ret += Math.abs(arr1[i++].getValue());
        else 
            ret += Math.abs(arr2[l++].getValue());
        }
    }

    return ret;
    }
 
}
          
