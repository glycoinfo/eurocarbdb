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
package org.eurocarbdb.applications.ms.glycopeakfinder.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eurocarbdb.applications.ms.glycopeakfinder.glycosciences.DatabaseResult;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPAnnotation;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.GPOtherResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.MassResidue;
import org.eurocarbdb.applications.ms.glycopeakfinder.storage.ResidueCategory;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.DBInterface;
import org.eurocarbdb.applications.ms.glycopeakfinder.util.ErrorTextEnglish;

/**
* @author Logan
*
*/
public class ResultAction extends GlycoPeakfinderAction
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // Settings for the page navigation
    private String m_strPageFrom = "";
    private String m_strPageTo = "";
    // database request
    private DatabaseResult m_objDatabaseResult = null;
    // selected values
    private String[] m_aSearchNumber = new String[0];
    private String m_strDatabase = null;
    private String m_strDatabaseStartID = null;
    private String m_strSearchPage = null;

    public ResultAction()
    {
        this.m_strPageType = "calculation";
    }

    public void setSearchPage(String a_strDB)
    {
        this.m_strSearchPage = a_strDB;
    }

    public String getSearchPage()
    {
        return this.m_strSearchPage;
    }

    public void setStartId(String a_strDB)
    {
        this.m_strDatabaseStartID = a_strDB;
    }

    public String getStartId()
    {
        return this.m_strDatabaseStartID;
    }

    public void setDatabase(String a_strDB)
    {
        this.m_strDatabase = a_strDB;
    }

    public String getDatabase()
    {
        return this.m_strDatabase;
    }

    public void setDbResult(DatabaseResult a_objResult)
    {
        this.m_objDatabaseResult = a_objResult;
    }

    public DatabaseResult getDbResult()
    {
        return this.m_objDatabaseResult;
    }

    public void setSearchNumber(String[] a_strNumbers)
    {
        this.m_aSearchNumber = a_strNumbers;
    }

    public String[] getSearchNumber()
    {
        return this.m_aSearchNumber;
    }

    public void setPageFrom(String a_strPage)
    {
        this.m_strPageFrom = a_strPage;
    }

    public String getPageFrom()
    {
        return this.m_strPageFrom;
    }

    public void setPageTo(String a_strPage)
    {
        this.m_strPageTo = a_strPage;
    }

    public String getPageTo()
    {
        return this.m_strPageTo;
    }

    public ArrayList<MassResidue> getUsedResidue()
    {
        ArrayList<MassResidue> t_aResult = new ArrayList<MassResidue>();
        for (Iterator<ResidueCategory> t_iterCategory = this.m_objSettings.getCategorie().iterator(); t_iterCategory.hasNext();) 
        {
            for (Iterator<MassResidue> t_iterResidue = t_iterCategory.next().getResidues().iterator(); t_iterResidue.hasNext();) 
            {
                MassResidue t_objResidue = t_iterResidue.next();
                if ( t_objResidue.getMax() > 0 )
                {
                    t_aResult.add(t_objResidue);
                }
            }

        }
        GPOtherResidue t_objOther = this.m_objSettings.getOtherResidueOne();
        if ( t_objOther.getMax() > 0 )
        {
            MassResidue t_objResidue = new MassResidue();
            t_objResidue.setName(t_objOther.getName());
            t_objResidue.setAbbr(t_objOther.getName());
            t_objResidue.setMax(t_objOther.getMax());
            t_objResidue.setMin(t_objOther.getMin());
            t_aResult.add(t_objResidue);
        }
        t_objOther = this.m_objSettings.getOtherResidueTwo();
        if ( t_objOther.getMax() > 0 )
        {
            MassResidue t_objResidue = new MassResidue();
            t_objResidue.setName(t_objOther.getName());
            t_objResidue.setAbbr(t_objOther.getName());
            t_objResidue.setMax(t_objOther.getMax());
            t_objResidue.setMin(t_objOther.getMin());
            t_aResult.add(t_objResidue);
        }
        t_objOther = this.m_objSettings.getOtherResidueThree();
        if ( t_objOther.getMax() > 0 )
        {
            MassResidue t_objResidue = new MassResidue();
            t_objResidue.setName(t_objOther.getName());
            t_objResidue.setAbbr(t_objOther.getName());
            t_objResidue.setMax(t_objOther.getMax());
            t_objResidue.setMin(t_objOther.getMin());
            t_aResult.add(t_objResidue);
        }    
        return t_aResult;
    }

    public void setUsedResidue(ArrayList<MassResidue> a_aResidues)
    {}

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
        this.m_objSettings.resetErrors();
        if ( !this.m_objSettings.getInitialized() || !this.m_objResult.getInitialized() )
        {
            return "go_start_page";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("sett") )
        {
            this.m_objDatabaseResult = null;
            return "page_sett";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("dela") )
        {
            int t_iMinor = 0;
            int t_iMajor = 0;
            for (int t_iCounter = 0; t_iCounter < this.m_aSearchNumber.length; t_iCounter++) 
            {
                String[] t_aParts = this.m_aSearchNumber[t_iCounter].split("-");
                if ( t_aParts.length == 3 )
                {
                    if ( t_aParts[0].equals("pre") )
                    {
                        t_iMajor = Integer.parseInt(t_aParts[1]);
                        t_iMinor = Integer.parseInt(t_aParts[2]);
                        this.m_objResult.deletePrecursorAnnotation(t_iMajor,t_iMinor);    
                    }
                    else if ( t_aParts[0].equals("frag") )
                    {
                        t_iMajor = Integer.parseInt(t_aParts[1]);
                        t_iMinor = Integer.parseInt(t_aParts[2]);
                        this.m_objResult.deleteAnnotation(t_iMajor,t_iMinor);                    
                    }
                }                
            }
            this.m_objDatabaseResult = null;
            return "page_resu";
        }
        if ( this.m_strPageTo.equalsIgnoreCase("stru") )
        {
            if ( this.m_objDatabaseResult == null )
            {
                if ( this.m_aSearchNumber == null )
                {
                    this.m_objSettings.addError( ErrorTextEnglish.NO_SELECT );
                    return "page_error";
                }
                if ( this.m_aSearchNumber.length == 0 )
                {
                    this.m_objSettings.addError( ErrorTextEnglish.NO_SELECT );
                    return "page_error";
                }
                if ( this.m_strDatabase == null )
                {
                    this.m_objSettings.addError( ErrorTextEnglish.NO_DB );
                    return "page_error";
                }
                if ( this.m_aSearchNumber.length != 1 )
                {
                    this.m_objSettings.addError( ErrorTextEnglish.ONLY_ONE_FOR_DB );
                    return "page_error";
                }
            }
            else
            {
                this.m_strDatabase = this.m_objDatabaseResult.getDatabase();
            }
            // Databaserequest
            try
            {
                if ( this.m_strDatabase.equals("glycosciences") )
                {
                    DBInterface t_objDB = new DBInterface(this.m_objConfiguration);
                    if ( this.m_objDatabaseResult == null || this.m_strSearchPage == null )
                    {
                        int t_iMinor = 0;
                        int t_iMajor = 0;
                        try
                        {
                            for (int t_iCounter = 0; t_iCounter < this.m_aSearchNumber.length; t_iCounter++) 
                            {
                                String[] t_aParts = this.m_aSearchNumber[t_iCounter].split("-");
                                if ( t_aParts.length == 3 )
                                {
                                    GPAnnotation t_objAnno = null;
                                    if ( t_aParts[0].equals("pre") )
                                    {
                                        t_iMajor = Integer.parseInt(t_aParts[1]);
                                        t_iMinor = Integer.parseInt(t_aParts[2]);
                                        t_objAnno = this.m_objResult.findPrecursorAnnotation(t_iMajor,t_iMinor);
                                    }
                                    else if ( t_aParts[0].equals("frag") )
                                    {
                                        t_iMajor = Integer.parseInt(t_aParts[1]);
                                        t_iMinor = Integer.parseInt(t_aParts[2]);
                                        t_objAnno = this.m_objResult.findAnnotation(t_iMajor,t_iMinor);
                                    }
                                    if ( t_objAnno == null )
                                    {
                                        this.m_objSettings.addError( ErrorTextEnglish.NO_SELECT );
                                        return "page_error";
                                    }

                                    this.m_objDatabaseResult = t_objDB.performeQueryGlycosciences(t_objAnno,this.m_objSettings.getLimits().getMaxGlycosciencesResults());

                                    t_objDB.pageGlycosciences(this.m_objDatabaseResult,this.m_objSettings.getLimits().getMaxGlycosciencesResults(),1);
                                    if ( this.m_objDatabaseResult.getError() != null )
                                    {
                                        this.m_objSettings.addError( this.m_objDatabaseResult.getError() );
                                        return "page_error";
                                    }
                                }
                            }                    
                        } 
                        catch (Exception e)
                        {
                            this.handleExceptions("result","glycosciences request", e);
                        }                            
                    }
                    else
                    {
                        // paging
                        Integer t_iPage = Integer.parseInt(this.m_strSearchPage);
                        if ( t_iPage < 1 || t_iPage > this.m_objDatabaseResult.getLastPage() )
                        {
                            this.m_objSettings.addError( ErrorTextEnglish.INVALIDE_PAGE );
                            return "page_error";
                        }
                        t_objDB.pageGlycosciences(this.m_objDatabaseResult,this.m_objSettings.getLimits().getMaxGlycosciencesResults(),t_iPage);                            
                    }
                    return "page_glycosciences";
                }
                else
                {
                    this.m_objSettings.addError( ErrorTextEnglish.UNKNOWN_DB + this.m_strDatabase );
                    this.m_objDatabaseResult = null;
                    return "page_error";
                }

            }
            catch (ClassNotFoundException e)
            {
                this.handleExceptions("result","glycosciences request class", e);
                this.m_objSettings.addError( ErrorTextEnglish.CRITICAL_COMPOSITION_ERROR );
                return "page_error";
            } 
            catch (SQLException e)
            {
                this.handleExceptions("result","glycosciences request query", e);
                this.m_objSettings.addError( ErrorTextEnglish.CRITICAL_COMPOSITION_ERROR );
                return "page_error";
            }            
            catch (Exception e)
            {
                this.handleExceptions("result","glycosciences request", e);
                this.m_objSettings.addError( ErrorTextEnglish.CRITICAL_COMPOSITION_ERROR );
                return "page_error";
            }            
        }
        this.m_objDatabaseResult = null;
        return "page_resu";
    }
   
}