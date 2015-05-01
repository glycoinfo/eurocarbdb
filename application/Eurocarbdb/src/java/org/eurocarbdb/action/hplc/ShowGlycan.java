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
 *   Last commit: $Rev: 1879 $ by $Author: matthew.campbell1980 $ on $Date:: 2010-03-09 #$  
 */
/**
 * $Id: ShowGlycan.java 1879 2010-03-08 15:34:54Z matthew.campbell1980 $
 * Last changed $Author: matthew.campbell1980 $
 * EUROCarbDB Project
 */

package org.eurocarbdb.action.hplc;

import java.lang.*;
import java.util.*;


import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.hplc.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;
import org.eurocarbdb.dataaccess.EntityManager;

import org.apache.log4j.Logger;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

//eurocarb party imports
import org.eurocarbdb.action.BrowseAction;
import org.eurocarbdb.dataaccess.indexes.Index;
import org.eurocarbdb.dataaccess.indexes.Indexable;
import org.eurocarbdb.dataaccess.indexes.IndexByMostEvidence;
import org.eurocarbdb.dataaccess.indexes.IndexByContributedDate;
import org.eurocarbdb.dataaccess.indexes.IndexByContributorName;
import org.eurocarbdb.dataaccess.core.Contributor;


import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.Sugar;
import org.eurocarbdb.sugar.Monosaccharide;
import org.eurocarbdb.sugar.SugarSequence;
// import org.eurocarbdb.sugar.Substituent;
import org.eurocarbdb.sugar.seq.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;

/**
 * @author 		 	matthew	
 * @rev				$Rev: 1879 $
 */
public class ShowGlycan extends BrowseAction<Glycan> {
	

	
	

	protected static final Logger logger = Logger.getLogger( ShowGlycan.class.getName() );

	private Glycan glycan= null;
	private int searchGlycanId = 0;
	private String imageStyle;
	private int reportId;
	private double reportValue;
	private List<RefLink> stats;
	private List<RefLink> displayStats;
	private List<RefLink> refs;
	private List<RefLink> displayRefs;
	private List<DigestSingle> displayDigestSingle;
	private List<DigestSingle> digestSingleMulti;
	private List<DigestSingle> displayDigestSingleMulti;
	private List<MultistructuresGlycoct> displayMultipleCt;
	private List<MultistructuresGlycoct> multiDigests;
	private List<MultistructuresGlycoct> displayMultiDigests;
	private List<Multipleglycoct> multiDigestsProduct;
	private List<Multipleglycoct> displayMultiDigestsProduct;


	public Glycan getGlycan() {
		return glycan;
	}
	
	GlycanSequence gwseq = null;
	
	public GlycanSequence getGlycanSequence(){
		
		return gwseq;
	}
	
	
	
	GlycanSequence displaygwseq;
	public GlycanSequence getDisplaygwseq() { return displaygwseq;} 

	@Override
	public int getTotalResults()
	{
		if ( totalResults <= 0 )
		{
			totalResults = getEntityManager().countAll( Glycan.class );
			logger.debug("calculated totalResults = " + totalResults );
		}

		return totalResults;
	}

	public int getGlycanId() { return this.searchGlycanId; }
	public void setGlycanId( int search_glycan_id) {this.searchGlycanId = search_glycan_id; }

	public String getImageStyle() { return this.imageStyle;}
	public void setImageStyle( String pic_image_style) {this.imageStyle = pic_image_style;}


	public List getDisplayStats(){return displayStats;}

	public List<RefLink> getQuery() {
		return refs;
	}

	public List getDisplayRefs()
	{
		return displayRefs;
	}

	public List getDisplayDigestSingle()
	{
		return displayDigestSingle;
	}

	public List getDigestSingleMulti()
	{
		return digestSingleMulti;
	}

	public List getMultiDigests()
	{
		return multiDigests;
	}

	public List getDisplayMultiDigests()
	{
		return displayMultiDigests;
	}


	public List getMultiDigestsProduct()
	{
		return multiDigestsProduct;
	}

	public List getDisplayMultiDigestsProduct()
	{
		return displayMultiDigestsProduct;
	}

	public List getDisplayMultipleCt()
	{
		return displayMultipleCt;
	}

	public final Class<Glycan> getIndexableType() 
	{
		return Glycan.class;
	}
	
	public List<GlycanSequence> displaySuperStructures;
	public List getDisplaySuperStructures(){
		return displaySuperStructures;
	}
	
	public List<GlycanSequence> displayLinkage;
	public List getDisplayLinkage(){
		return displayLinkage;
	}
	
	public List<GlycanSequence> displayStereo;
	public List getDisplayStereo(){
		return displayStereo;
	}
	
	public List<GlycanSequence> displayEquivalents;
	public List getDisplayEquivalents(){
		return displayEquivalents;
	}
	public String execute() {

		EntityManager em = getEntityManager();
		Contributor currentContributor = Eurocarb.getCurrentContributor();


		if( searchGlycanId == 0 ) {
			this.addActionError( "Invalid");
			return INPUT;
		}


		logger.info("show stats");
		//get average and td values for glycanId
		stats = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.STATS")
		.setParameter("glycanId", searchGlycanId)
		.list();

		displayStats = stats;

		glycan = Eurocarb.getEntityManager().lookup( Glycan.class, searchGlycanId );
		
		int searchGlycanIdCore = glycan.getOgbitranslation();
		GlycanSequence gwseq = Eurocarb.getEntityManager().lookup( GlycanSequence.class, searchGlycanIdCore );
		
		//temp start
		logger.debug("saving new glycan_sequence");
        SugarSequence sseq = gwseq.getSugarSequence(); 
        GlycanSequence glycanSequence = GlycanSequence.lookupOrCreateNew( sseq );
        String sseqtest = glycanSequence.getSequenceIupac();
        logger.info("here it is: " + sseqtest);
		//temp end
		
		displaygwseq = gwseq;
		
	
		refs = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.RefLink.PUBS")
		.setParameter("glycan_id", searchGlycanId)
		.list();

		displayRefs = refs;

		List<DigestSingle> digestSingle = (List<DigestSingle>)
		Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestSingle.GLYCAN_ENTRY_DIGESTS")
		.setParameter("glycan_id", searchGlycanId)
		.setParameter("glycan_id", searchGlycanId)
		.list();

		displayDigestSingle = digestSingle; 

		List digestSingleMulti =Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.DigestSingle.GLYCAN_ENTRY_DIGESTS_MULTIPLE")
		//  .setParameter("glycan_id", searchGlycanId)
		.setParameter("glycan_id", searchGlycanId)
		.setParameter("glycan_id", searchGlycanId)
		.list();

		displayDigestSingleMulti = digestSingleMulti; 

		List<MultistructuresGlycoct> multipleCt = (List<MultistructuresGlycoct>) Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.SELECT_ALL_MULTICT")
		.setParameter("glycan_id", searchGlycanId)
		.list();

		displayMultipleCt = multipleCt;


		//List multiDigests = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Multipleglycoct.SELECT_ALL_MULTICT_DIGESTS")
		List<MultistructuresGlycoct> multiDigests = (List<MultistructuresGlycoct>) Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.SELECT_ALL_MULTICT_DIGESTS")
		.setParameter("glycan_id", searchGlycanId)
		.list();
		int metest = multiDigests.size();
		logger.info("listsizetest" + metest);
		if ( metest > 0) {
			displayMultiDigests = multiDigests;
		}
		if (metest <= 0) {
			//List multiDigestsProduct = Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.Multipleglycoct.SELECT_ALL_MULTICT_DIGESTS_PRODUCT")
			List<MultistructuresGlycoct> multiDigestsProduct = (List<MultistructuresGlycoct>) Eurocarb.getEntityManager().getQuery("org.eurocarbdb.dataaccess.hplc.MultistructuresGlycoct.SELECT_ALL_MULTICT_DIGESTS_PRODUCT")
			.setParameter("glycan_id", searchGlycanId)
			.list();
			displayMultiDigests = multiDigestsProduct;
		}

		return SUCCESS;
	}

}


