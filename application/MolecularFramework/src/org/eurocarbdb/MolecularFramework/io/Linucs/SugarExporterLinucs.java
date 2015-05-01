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
package org.eurocarbdb.MolecularFramework.io.Linucs;

import org.eurocarbdb.MolecularFramework.io.SugarExporter;
import org.eurocarbdb.MolecularFramework.io.SugarExporterException;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;

/**
* @author Logan
*
*/
public class SugarExporterLinucs extends SugarExporter 
{

    /**
     * @see org.eurocarbdb.MolecularFramework.io.SugarExporter#export(org.eurocarbdb.MolecularFramework.sugar.Sugar)
     */
    @Override
    public String export(Sugar a_objSugar) throws SugarExporterException 
    {
        try 
        {
            GlycoVisitorExport t_objVisitor = new GlycoVisitorExport();
            t_objVisitor.start(a_objSugar);
            return t_objVisitor.getCode();
        } 
        catch (Exception e) 
        {
            throw new SugarExporterException(e.getMessage(),e);
        }
    }

}
