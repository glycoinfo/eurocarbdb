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
package org.eurocarbdb.applications.ms.glycopeakfinder.interceptor;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.interceptor.AroundInterceptor;
import com.opensymphony.xwork.interceptor.NoParameters;
import com.opensymphony.xwork.util.OgnlContextState;
import com.opensymphony.xwork.util.OgnlValueStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
* HashParameterInterceptor
*  
* use in xwork.xml :
* <pre>
* &lt;interceptor name="hashparam" class="org.eurocarbdb.applications.ms.glycopeakfinder.interceptor.HashParameterInterceptor"/&gt;
*  
* &lt;interceptor-ref name="hashparam"&gt;
*   &lt;param name="executeMethode"&gt;myHashMethode&lt;/param&gt;
*   &lt;param name="hashTag"&gt;myHash&lt;/param&gt;
* &lt;/interceptor-ref&gt;
* </pre> 
* 
* <p>takes all inputfields of type :
* <pre>
* &lt;input type="text" name="myHash.key" value="value"&gt;
* </pre>
* 
* <p>generates a Hashmap&lt;String,String[]&gt; :
* <pre>
* Hashmap 
* [
*     "key"  ==> value
* ]
* </pre>
* 
* <p> and give this Hashmap to the methode of the action in executeMethode
* 
* @author Rene Ranzinger
*/
public class HashParameterInterceptor extends AroundInterceptor 
{
    private static final long serialVersionUID = 1L;
    
    private String m_strHashTag = "myHash";
    private String m_strExecuteMethode = "myHashMethode";
    
    public void setHashTag(String a_strTag)
    {
        this.m_strHashTag = a_strTag;
    }
    
    public String getHashTag()
    {
        return this.m_strHashTag;
    }
    
    public void setExecuteMethode(String a_strMethode)
    {
        this.m_strExecuteMethode = a_strMethode;
    }
    
    public String getExecuteMethode()
    {
        return this.m_strExecuteMethode;
    }
    
    /**
     * not used
     */
    protected void after(ActionInvocation dispatcher, String result) throws Exception 
    {
    }
    
    protected void before(ActionInvocation invocation) throws Exception 
    {
        // have params?
        if (!(invocation.getAction() instanceof NoParameters)) 
        {
            // yes
            ActionContext t_objContext = invocation.getInvocationContext();
            final Map t_mapParameter = t_objContext.getParameters();
            
            if (t_mapParameter != null) 
            {
                Map t_objContextMap = t_objContext.getContextMap();
                try 
                {
                    OgnlContextState.setCreatingNullObjects(t_objContextMap, true);
                    OgnlContextState.setDenyMethodExecution(t_objContextMap, true);
                    OgnlContextState.setReportingConversionErrors(t_objContextMap, true);
                    
                    OgnlValueStack t_objStack = t_objContext.getValueStack();
                    boolean t_bFoundOne = false;
                    Set t_objSet = t_mapParameter.keySet();
                    HashMap<String,String[]> t_hashParameter = new HashMap<String,String[]>();
                    String t_strHashKey = "";
                    if ( t_objSet != null )
                    {
                        for (Iterator t_iterKey = t_objSet.iterator(); t_iterKey.hasNext();)
                        {
                            String t_objKey = (String) t_iterKey.next();
                            if ( t_objKey.startsWith(this.m_strHashTag) )
                            {
                                String[] t_objValue = (String[]) t_mapParameter.get(t_objKey);
                                t_strHashKey = t_objKey.substring(this.m_strHashTag.length()+1);
                                t_hashParameter.put(t_strHashKey,t_objValue);
                                t_bFoundOne = true;
                            }
                        }
                    }
                    if ( t_bFoundOne )
                    {
                        t_objStack.setValue(this.m_strExecuteMethode,t_hashParameter);
                    }
                } 
                finally 
                {
                    OgnlContextState.setCreatingNullObjects(t_objContextMap, false);
                    OgnlContextState.setDenyMethodExecution(t_objContextMap, false);
                    OgnlContextState.setReportingConversionErrors(t_objContextMap, false);
                }
            }
        }
    }
}