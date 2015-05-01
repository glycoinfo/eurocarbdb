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
*   Last commit: $Rev: 1208 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/
package org.eurocarbdb.sugar;

import org.eurocarbdb.util.Visitor;

/**
*   Convenience implementation of the {@link http://en.wikipedia.org/wiki/Visitor_pattern 
*   Visitor pattern} for traversing sugar objects. The default implementations
*   of visit methods do nothing.
*/
public abstract class SugarVisitor extends Visitor
{    
    public void accept( Sugar s ) {}
    public void accept( Residue r ) {}
    public void accept( Linkage l ) {}
    public void accept( Monosaccharide m ) {}
    public void accept( GlycosidicLinkage l ) {} 
    
    // public void visit( RepeatResidue r ) {} 
    
}
