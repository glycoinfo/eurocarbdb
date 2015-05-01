/******************************************************************************

*/

SET search_path TO core;

/*===  VIEWS  ===*/


CREATE VIEW view_biological_context 
 AS SELECT bc.biological_context_id
		,	tax.parent_taxonomy_id
		,	tax.taxonomy_id
		,	tax.taxon
		,	ttax.parent_tissue_taxonomy_id
		,	ttax.tissue_taxonomy_id
		,	ttax.tissue_taxon
		,	COUNT( bcd.disease_id ) AS count_diseases
		,	COUNT( bcp.peturbation_id ) AS count_peturbations
--		, 	CONCAT( tax_syn.synonym, ", " ) AS taxonomy_synonyms,
--		,	CONCAT( ttax_syn.synonym, ", " ) AS tissue_taxonomy_synonyms
	FROM 	biological_context bc
			LEFT JOIN taxonomy tax 
				USING ( taxonomy_id )
			LEFT JOIN tissue_taxonomy ttax 
				USING ( tissue_taxonomy_id )
--			LEFT OUTER JOIN taxonomy_synonym tax_syn
--				USING ( taxonomy_id )
--			LEFT OUTER JOIN tissue_taxonomy_synonym ttax_syn
--				USING ( tissue_taxonomy_id )
			LEFT OUTER JOIN	biological_context_to_disease bcd
				USING ( biological_context_id )
			LEFT OUTER JOIN biological_context_to_peturbation bcp
				USING ( biological_context_id )
	GROUP BY bc.biological_context_id
		,	ttax.tissue_taxonomy_id
		,	ttax.parent_tissue_taxonomy_id
		,	ttax.tissue_taxon
		,	tax.parent_taxonomy_id
		,	tax.taxonomy_id
		, 	tax.taxon
;
