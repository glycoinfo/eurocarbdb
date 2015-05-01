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
package org.eurocarbdb.resourcesdb.monosaccharide;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.resourcesdb.ResourcesDbException;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplate;
import org.eurocarbdb.resourcesdb.template.SubstituentTemplateContainer;

public class SubstituentSubpartTreeNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

    private String name = null;
    private SubstituentTemplate substTmpl = null;
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String nameStr) {
        this.name = nameStr;
    }
    
    public SubstituentTemplate getSubstTmpl() {
        return this.substTmpl;
    }
    
    public SubstituentTemplate getSubstTmpl(SubstituentTemplateContainer container) {
        if(this.substTmpl == null) {
            if(this.getName() != null) {
                try {
                    this.substTmpl = container.forName(GlycanNamescheme.MONOSACCHARIDEDB, this.getName());
                } catch(ResourcesDbException rEx) {
                    
                }
            }
        }
        return this.substTmpl;
    }
    
    public void setSubstTmpl(SubstituentTemplate substTmpl) {
        this.substTmpl = substTmpl;
    }

}
