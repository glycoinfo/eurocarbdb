SET search_path=core,pg_catalog;
SET default_tablespace='';
/**ALTER USER postgres set search_path core,ms */

/**From here http://mssql-to-postgresql.blogspot.com/2007/12/cool-groupconcat.html*/
create aggregate array_accum (
sfunc = array_append,
basetype = anyelement,
stype = anyarray,
initcond = '{}'
);

CREATE OR REPLACE FUNCTION _group_concat(text, text)
RETURNS text AS $$
SELECT CASE
WHEN $2 IS NULL THEN $1
WHEN $1 IS NULL THEN $2
ELSE $1 operator(pg_catalog.||) ',' operator(pg_catalog.||) $2
END
$$ IMMUTABLE LANGUAGE SQL;

CREATE AGGREGATE group_concat (
BASETYPE = text,
SFUNC = _group_concat,
STYPE = text
);

/**CREATE NEW BIOLOGICAL CONTEXT TABLE _NEW*/
CREATE TABLE core.biological_context_new(
    biological_context_id               SERIAL PRIMARY KEY NOT NULL,

    taxonomy_id                         INT NOT NULL
                                        REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,

    tissue_taxonomy_id                  INT NOT NULL
                                        REFERENCES core.tissue_taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
            
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

/**TRANSFER DATA FROM ORIGINAL TABLE - MINUS CONTRIBUTORS*/
INSERT INTO biological_context_new (taxonomy_id,tissue_taxonomy_id,date_entered) SELECT  taxonomy_id, tissue_taxonomy_id,min(date_entered) FROM biological_context GROUP BY taxonomy_id,tissue_taxonomy_id;

/** Index for taxonomy_id/tissue_taxonomy_id (foreign keys) */
CREATE  INDEX index_bio_con_new__taxonomy ON core.biological_context_new ( taxonomy_id, tissue_taxonomy_id )
;

/** Index for core.biological_context.tissue_taxonomy_id (foreign key) */
CREATE  INDEX index_bio_con_new__tissue_taxonomy_id ON core.biological_context_new ( tissue_taxonomy_id )
;

/** Index for core.biological_context.tissue_taxonomy_id (foreign key) */
CREATE  INDEX index_bio_con_new__taxonomy_id ON core.biological_context_new ( taxonomy_id )
;

/**CREATE TEMPORARY MAPPING TABLE (old biological_context_id to new biological_context_id*/
CREATE TEMPORARY TABLE biological_context_id_old_to_new  AS SELECT n.biological_context_id AS new_id,o.biological_context_id AS old_id FROM biological_context AS o, biological_context_new AS n WHERE o.taxonomy_id=n.taxonomy_id AND o.tissue_taxonomy_id=n.tissue_taxonomy_id;

CREATE INDEX temp_biological_context_id_old_to_new_new_id ON biological_context_id_old_to_new(new_id);
CREATE INDEX temp_biological_context_id_old_to_new_old_id ON biological_context_id_old_to_new(old_id);


/**CREATE biological_context_contributor table*/
CREATE TABLE core.biological_context_contributor (
   /** Auto-incrementing primary key.  */
    biological_context_contributor_id               SERIAL PRIMARY KEY NOT NULL,

    biological_context_id   INT NOT NULL,

     /** The contributor of this context */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, 

    /** User-specific comments relevant to this sample/biological_context. */
    comments                            TEXT DEFAULT NULL
);

/**TRANSFER contributor association to biological contexts to new table*/
INSERT INTO biological_context_contributor (biological_context_id,contributor_id,comments,date_entered)  SELECT DISTINCT b.new_id, contributor_id,group_concat(comments),min(date_entered) FROM biological_context a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id GROUP BY b.new_id, contributor_id;

/**CONVERT DATA, AND STORE IN TMP*/
CREATE TEMPORARY TABLE biological_context_to_disease_new AS SELECT DISTINCT disease_id,b.new_id AS biological_context_id FROM biological_context_to_disease a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id;
CREATE TEMPORARY TABLE biological_context_to_experiment_new AS SELECT DISTINCT experiment_id,contributor_id, min(date_entered) AS date_entered, b.new_id AS biological_context_id FROM biological_context_to_experiment a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id  GROUP BY experiment_id,contributor_id,b.new_id;
CREATE TEMPORARY TABLE biological_context_to_glycoconjugate_new AS SELECT DISTINCT glycoconjugate_id,b.new_id AS biological_context_id FROM biological_context_to_glycoconjugate a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id;
CREATE TEMPORARY TABLE biological_context_to_perturbation_new AS SELECT DISTINCT perturbation_id,b.new_id AS biological_context_id FROM biological_context_to_perturbation a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id;
CREATE TEMPORARY TABLE evidence_to_biological_context_new AS SELECT DISTINCT evidence_id,contributor_id,min(date_entered) AS date_entered, b.new_id AS biological_context_id FROM evidence_to_biological_context a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id GROUP BY evidence_id,contributor_id,b.new_id;
CREATE TEMPORARY TABLE glycan_sequence_to_biological_context_new AS SELECT DISTINCT glycan_sequence_id,contributor_id,min(date_entered) AS date_entered,b.new_id AS biological_context_id FROM glycan_sequence_to_biological_context a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id GROUP BY glycan_sequence_id,contributor_id,b.new_id;
CREATE TEMPORARY TABLE glycoprotein_association_new AS SELECT DISTINCT glycan_sequence_id,glycoconjugate_id,amino_acid,amino_acid_position,occupancy,b.new_id AS biological_context_id FROM glycoprotein_association a, biological_context_id_old_to_new b WHERE a.biological_context_id=b.old_id;

/**DROP CONSTRAINTS*/
ALTER TABLE biological_context_to_disease DROP CONSTRAINT biological_context_to_disease_biological_context_id_fkey;
ALTER TABLE biological_context_to_experiment DROP CONSTRAINT biological_context_to_experiment_biological_context_id_fkey;
ALTER TABLE biological_context_to_glycoconjugate DROP CONSTRAINT biological_context_to_glycoconjugate_biological_context_id_fkey;
ALTER TABLE biological_context_to_perturbation DROP CONSTRAINT biological_context_to_perturbation_biological_context_id_fkey;
ALTER TABLE evidence_to_biological_context DROP CONSTRAINT evidence_to_biological_context_biological_context_id_fkey;
ALTER TABLE glycan_sequence_to_biological_context DROP CONSTRAINT glycan_sequence_to_biological_contex_biological_context_id_fkey;
ALTER TABLE glycoprotein_association DROP CONSTRAINT glycoprotein_association_biological_context_id_fkey;

/**TRUNCATE TABLES*/
TRUNCATE TABLE biological_context_to_disease;
TRUNCATE TABLE biological_context_to_experiment;
TRUNCATE TABLE biological_context_to_glycoconjugate;
TRUNCATE TABLE biological_context_to_perturbation;
TRUNCATE TABLE evidence_to_biological_context;
TRUNCATE TABLE glycan_sequence_to_biological_context;
TRUNCATE TABLE glycoprotein_association;

/**INSTALL NEW BIOLOGICAL CONTEXT TABLES*/
DROP TABLE core.biological_context;
ALTER TABLE core.biological_context_new RENAME TO biological_context;
ALTER SEQUENCE biological_context_new_biological_context_id_seq RENAME TO  biological_context_biological_context_id_seq;


/**RESTORE CONSTRAINTS*/
ALTER TABLE biological_context_to_disease ADD CONSTRAINT biological_context_to_disease_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE biological_context_to_experiment ADD CONSTRAINT biological_context_to_experiment_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE biological_context_to_glycoconjugate ADD CONSTRAINT biological_context_to_glycoconjugate_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE biological_context_to_perturbation ADD CONSTRAINT biological_context_to_perturbation_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE evidence_to_biological_context ADD CONSTRAINT evidence_to_biological_context_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE glycan_sequence_to_biological_context ADD CONSTRAINT glycan_sequence_to_biological_contex_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE glycoprotein_association ADD CONSTRAINT glycoprotein_association_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE biological_context_contributor ADD CONSTRAINT biological_context_contributor_biological_context_id_fkey FOREIGN KEY (biological_context_id) REFERENCES core.biological_context ON UPDATE CASCADE ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED;

/**ALTER UNIQUE CONSTRAINT ON glycan_sequence_to_biological_context*/
ALTER TABLE glycan_sequence_to_biological_context DROP CONSTRAINT glycan_sequence_to_biological_context_glycan_sequence_id_key;
ALTER TABLE glycan_sequence_to_biological_context ADD CONSTRAINT glycan_sequence_to_biological_context_glycan_sequence_id_constr UNIQUE(biological_context_id,glycan_sequence_id,contributor_id);

/**RESTORE DATA*/
INSERT INTO biological_context_to_disease (biological_context_id,disease_id) SELECT biological_context_id,disease_id FROM biological_context_to_disease_new;
INSERT INTO biological_context_to_experiment (biological_context_id,experiment_id,contributor_id,date_entered) SELECT biological_context_id,experiment_id,contributor_id,date_entered FROM biological_context_to_experiment_new;
INSERT INTO biological_context_to_glycoconjugate (biological_context_id,glycoconjugate_id) SELECT biological_context_id,glycoconjugate_id FROM biological_context_to_glycoconjugate_new;
INSERT INTO biological_context_to_perturbation (biological_context_id,perturbation_id) SELECT biological_context_id, perturbation_id FROM biological_context_to_perturbation_new;
INSERT INTO evidence_to_biological_context (biological_context_id,evidence_id,contributor_id,date_entered) SELECT biological_context_id,evidence_id,contributor_id,date_entered FROM evidence_to_biological_context_new;
INSERT INTO glycan_sequence_to_biological_context (biological_context_id,glycan_sequence_id,contributor_id,date_entered) SELECT biological_context_id,glycan_sequence_id,contributor_id,date_entered FROM glycan_sequence_to_biological_context_new;
INSERT INTO glycoprotein_association (biological_context_id,glycan_sequence_id,glycoconjugate_id,amino_acid,amino_acid_position,occupancy) SELECT biological_context_id,glycan_sequence_id,glycoconjugate_id,amino_acid,amino_acid_position,occupancy FROM glycoprotein_association_new; 