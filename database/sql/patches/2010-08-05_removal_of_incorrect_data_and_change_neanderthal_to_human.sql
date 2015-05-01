
/*  removal of alessio's test data  */

--  remove evidence contributed by alessio
delete from 
    core.evidence 
where 
    contributor_id = (
        select contributor_id 
        from core.contributor 
        where contributor_name = 'aceroni' limit 1 )
;

--  restrictions of deletion of journal_reference unecesaarily tight, prevents cascading delete of sequence
ALTER TABLE core.journal_reference DROP CONSTRAINT journal_reference_reference_id_fkey;
ALTER TABLE core.journal_reference ADD CONSTRAINT journal_reference_reference_id_fkey FOREIGN KEY (reference_id) REFERENCES core.reference (reference_id)    ON UPDATE CASCADE ON DELETE CASCADE    DEFERRABLE INITIALLY DEFERRED;


--  remove structures contributed by alessio
delete from 
    core.glycan_sequence 
where 
    contributor_id = (
        select contributor_id 
        from core.contributor 
        where contributor_name = 'aceroni' limit 1 )
;

--  finally remove alessio contributor. note: cascades to remove all contexts/etc contributed by alessio
delete from core.contributor where contributor_name = 'aceroni';


/*  change all contexts that refer to neanderthals.  */

--  update any remaining refs to neanderthals to homo sap.
update core.biological_context 
set taxonomy_id = (
				select taxonomy_id 
				from core.taxonomy 
				where taxon = 'homo sapiens' limit 1 )  
where taxonomy_id = (
				select taxonomy_id 
				from core.taxonomy 
				where taxon = 'homo sapiens neanderthalensis' limit 1 )
;

				
				
