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


package org.eurocarbdb.gui;

import java.util.List;

public class Navigation
{
    /** The Url that */
    private String url;
    
    /** The label of this navigation link */
    private String label;
    
    /** List of user roles that are able to follow this navigation link */
    private String[] roles;
    


    public String getUrl() {  return url;  }
    
    public void setUrl( String url ) {  this.url = url;  }
    
    public String getLabel() {  return this.label;  }
    
    public void setLabel( String label ) {  this.label = label;  }
    
    public String[] getRoles() {  return roles;  }
    
    public void setRoles( String[] roles ) {  this.roles = roles;  }
    
    public String toString()
    {
        return getUrl();   
    }
}
