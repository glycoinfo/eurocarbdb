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

package org.eurocarbdb.application.glycanbuilder;

import java.io.*;
import java.util.*;

/**
   Container for a collection of fragments with the same mass/charge
   value grouped by structure
   
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/


class FragmentGroup {


    private Vector<Vector<FragmentEntry>> fragments;

    /**
       Empty constructor
     */
    public FragmentGroup() {
    fragments = new Vector<Vector<FragmentEntry>>();
    }

    /**
       Return the collection of all fragment entries
     */
    public Vector<Vector<FragmentEntry>> getFragmentEntries() {
    return fragments;
    }
    
    /**
       Return the collection of all fragment entries for a specific
       group
       @param s_ind the index of the group
     */
    public Vector<FragmentEntry> getFragmentEntries(int s_ind) {
    return fragments.elementAt(s_ind);
    }

    /**
       Return the collection of all fragment structures for a specific group
       @param s_ind the index of the group
     */
    public Vector<Glycan> getFragments(int s_ind) {
    Vector<Glycan> ret = new Vector<Glycan>();
    for( FragmentEntry fe : fragments.elementAt(s_ind) )
        ret.add(fe.fragment);
    return ret;
    }

    /**
       Return <code>true</code> if there are no fragment entries in
       the container
     */
    public boolean isEmpty() {
    for( Vector<FragmentEntry> vfe : fragments ) 
        if( vfe.size()>0 )
        return false;
    return true;
    }

    public void assertSize(int s_ind) {
    // make space
    while( fragments.size()<=s_ind )
        fragments.add(new Vector<FragmentEntry>());
    }

    public void addFragment(int s_ind, FragmentEntry fe) {
    assertSize(s_ind);
    fragments.elementAt(s_ind).add(fe);
    }

    public void removeFragments(int s_ind) {
    fragments.removeElementAt(s_ind);    
    }

    public void removeFragment(int s_ind, FragmentEntry fe) {
    if( s_ind>=fragments.size() )
        return;
    
    Vector<FragmentEntry> vfe = fragments.elementAt(s_ind);
    for( int i=0; i<vfe.size(); i++ ) {
        if( vfe.elementAt(i).equals(fe) ) {
        vfe.removeElementAt(i);
        return;
        }
    }
    }

}