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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.action;

/*  class NoopAction  *//****************************************
*
*   This class does nothing; it's a no-operation (Noop) action
*   used for providing basic action support/methods to actions
*   that don't require any business logic.
*
*   @author mjh [glycoslave@gmail.com]
*/
public final class NoopAction extends EurocarbAction
{
    /*  execute  *//*************************************************
    *
    *   Does nothing; returns unconditional {@link ActionSupport#SUCCESS}.
    */
    public String execute()
    {
        return SUCCESS;   
    }

} 

