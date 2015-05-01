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
/**
* $Id: CreateTechnique.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.core;

import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Technique;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.Preparable;

/**
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class CreateTechnique implements EntityManaged, Action, Preparable {

    private EntityManager entityManager;
    
    private Technique technique;
    
    /* (non-Javadoc)
     * @see org.eurocarbdb.action.core.EntityManaged#getEntityManager()
     */
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    /* (non-Javadoc)
     * @see org.eurocarbdb.action.core.EntityManaged#setEntityManager(org.eurocarbdb.dataaccess.EntityManager)
     */
    public void setEntityManager(EntityManager manager) {
        this.entityManager = manager;
    }

    /* (non-Javadoc)
     * @see com.opensymphony.xwork.Action#execute()
     */
    public String execute() throws Exception {
        if (this.getTechnique() == null ) {
            return INPUT;
        }
        this.getEntityManager().store(this.getTechnique());
        return SUCCESS;
    }

    /* (non-Javadoc)
     * @see com.opensymphony.xwork.Preparable#prepare()
     */
    public void prepare() throws Exception {
        if (this.getTechnique() == null ) {
            return;
        }
        Technique technique = this.getEntityManager().lookup(Technique.class, this.getTechnique().getTechniqueId());
        if ( technique != null ) {
            this.setTechnique(technique);
        }
    }

    public Technique getTechnique() {
        return technique;
    }

    public void setTechnique(Technique technique) {
        this.technique = technique;
    }

}
