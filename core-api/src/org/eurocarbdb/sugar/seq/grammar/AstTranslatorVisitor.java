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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.sugar.seq.grammar;

//  eurocarb imports
import org.eurocarbdb.util.graph.Graph;
import org.eurocarbdb.util.graph.Vertex;
import org.eurocarbdb.util.graph.Edge;
import org.eurocarbdb.util.graph.DepthFirstGraphVisitor;

import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Residue;
import org.eurocarbdb.sugar.Linkage;


/**
*   Traverses an abstract syntax tree of a {@link Graph} of 
*   {@link LinkageToken}s and {@link ResidueToken}s and transforming
*   it into a {@link Sugar} object. The visitor is started by
*   calling {@link #visit(Graph)}.
*   @author mjh
*/
public class AstTranslatorVisitor 
extends DepthFirstGraphVisitor<LinkageToken,ResidueToken>
{
    /** The Sugar to populate */
    private final Sugar sugar;

    
    /** Creates an AST walker to populate the given {@link Sugar} object */
    public AstTranslatorVisitor( Sugar s ) 
    {  
        this.sugar = s;  
    }
    
    
    /**
    *   As we visit each edge in the AST, obtain the appropriate
    *   parent {@link Residue}, child {@link Residue}, and 
    *   {@link Linkage} from the given edge, and add these into
    *   the nascent {@link Sugar}.
    */
    public void accept( Edge<LinkageToken,ResidueToken> edge )
    {
        Linkage linkage = edge.getValue().getLinkage();
        
        Vertex<LinkageToken,ResidueToken> ptok = edge.getParent();
        Vertex<LinkageToken,ResidueToken> ctok = edge.getChild();
        
        Residue parent = ptok.getValue().getResidue();
        Residue child  = ctok.getValue().getResidue();
        
        if ( ! sugar.contains( parent ) )
        {
            sugar.addResidue( parent );
            
            //  optimisation only: pre-allocate exact number of edges for vertex
            sugar.getGraph().lastVertex().ensureCapacity( ptok.countAttachedEdges() );
        }
            
        sugar.addResidue( parent, linkage, child );
        
        //  optimisation only: pre-allocate exact number of edges for vertex
        sugar.getGraph().lastVertex().ensureCapacity( ctok.countAttachedEdges() );
        
        super.accept( edge );
    }
    
    
    /** 
    *   Although the traversal of {@link Edge}s is the primary means of 
    *   adding residues/linkages to the {@link Sugar}, we still have 
    *   to check {@link Vertex}es to make sure their {@link Residue}s
    *   also get added to the Sugar, since they may not have an {@link Edge}
    *   attached to them. A small optimisation is to make this check only for 
    *   {@link Vertex}es that <em>don't</em> have edges, since vertices with
    *   edges will be added in {@link #visit(Edge)}.
    */
    public void accept( Vertex<LinkageToken,ResidueToken> vertex )
    {
        if ( vertex.countAttachedEdges() == 0 )
        {
            Residue r = vertex.getValue().getResidue();
            sugar.addResidue( r );
        }
        
        super.accept( vertex );
    }
    
} // end class AstTranslatorVisitor


