
----- indexes for table core.contributor -----

/** Index for core.contributor.contributor_id (primary key) */
CREATE UNIQUE INDEX index_con__contributor_id_name ON core.contributor ( contributor_id, contributor_name )
;

----- indexes for table core.glycan_sequence -----

/** Index for core.glycan_sequence.contributor_id (foreign key) */
CREATE UNIQUE INDEX index_gly_seq__glycan_sequence_id ON core.glycan_sequence ( glycan_sequence_id )
;

CREATE  INDEX index_gly_seq__contributor_id ON core.glycan_sequence ( contributor_id )
;


----- indexes for table core.composition -----


----- indexes for table core.taxonomy -----

/** Index for core.taxonomy.taxonomy_id (foreign key) */
CREATE UNIQUE INDEX index_tax__taxonomy_id ON core.taxonomy ( taxonomy_id, parent_taxonomy_id )
;

/** Index for core.taxonomy.taxon */
CREATE  INDEX index_tax__taxon ON core.taxonomy ( taxon )
;

/** Index for core.taxonomy.ncbi_id */
CREATE  INDEX index_tax__ncbi ON core.taxonomy ( ncbi_id )
;


----- indexes for table core.taxonomy_synonym -----

/** Index for core.taxonomy_synonym.taxonomy_synonym_id (primary key) */
CREATE UNIQUE INDEX index_tax_syn__taxonomy_synonym_id ON core.taxonomy_synonym ( taxonomy_synonym_id, taxonomy_id )
;

/** Index for core.taxonomy_synonym.synonym (unique column) */
CREATE INDEX index_tax_syn__synonym ON core.taxonomy_synonym ( synonym )
;

----- indexes for table core.tissue_taxonomy -----

/** Index for core.tissue_taxonomy.tissue_taxonomy_id (primary key) */
CREATE UNIQUE INDEX index_tis_tax__tissue_taxonomy_id ON core.tissue_taxonomy ( tissue_taxonomy_id, parent_tissue_taxonomy_id )
;

/** Index for core.tissue_taxonomy.tissue_taxon */
CREATE INDEX index_tis_tax__taxon ON core.tissue_taxonomy ( tissue_taxon )
;


----- indexes for table core.tissue_taxonomy_synonym -----

/** Index for core.tissue_taxonomy_synonym.tissue_taxonomy_synonym_id (primary key) */
CREATE UNIQUE INDEX index_tis_tax_syn__tissue_taxonomy_synonym_id ON core.tissue_taxonomy_synonym ( tissue_taxonomy_synonym_id, tissue_taxonomy_id )
;

/** Index for core.tissue_taxonomy_synonym.synonym (unique column) */
CREATE UNIQUE INDEX index_tis_tax_syn__synonym ON core.tissue_taxonomy_synonym ( synonym )
;

----- indexes for table core.biological_context -----

/** Index for taxonomy_id/tissue_taxonomy_id (foreign keys) */
CREATE  INDEX index_bio_con__taxonomy ON core.biological_context ( taxonomy_id, tissue_taxonomy_id )
;

/** Index for core.biological_context.tissue_taxonomy_id (foreign key) */
CREATE  INDEX index_bio_con__tissue_taxonomy_id ON core.biological_context ( tissue_taxonomy_id )
;

----- indexes for table core.perturbation -----

/** Index for core.perturbation.parent_perturbation_id (foreign key) */
CREATE UNIQUE INDEX index_per__parent_perturbation_id ON core.perturbation ( perturbation_id, parent_perturbation_id )
;

/** Index for core.perturbation.perturbation_name */
CREATE  INDEX index_per__perturbation_name ON core.perturbation ( perturbation_name )
;


----- indexes for table core.disease -----

/** Index for core.disease.disease_id (primary key) */
CREATE UNIQUE INDEX index_dis__disease_id ON core.disease ( disease_id, parent_disease_id )
;

/** Index for core.disease.disease_name */
CREATE  INDEX index_dis__disease_name ON core.disease ( disease_name )
;

----- indexes for table core.disease_synonym -----

/** Index for core.disease_synonym.disease_synonym_id (primary key) */
CREATE UNIQUE INDEX index_dis_syn__disease_synonym_id ON core.disease_synonym ( disease_synonym_id, disease_id )
;

/** Index for core.disease_synonym.synonym (unique column) */
CREATE UNIQUE INDEX index_dis_syn__synonym ON core.disease_synonym ( synonym )
;

----- indexes for table core.glycoconjugate -----

CREATE UNIQUE INDEX index_gly__glycoconjugate_id ON core.glycoconjugate ( glycoconjugate_id )
;

----- indexes for table core.glycoprotein -----

/** Index for core.glycoprotein.native_species (foreign key) */
CREATE  INDEX index_gly__native_species ON core.glycoprotein ( native_species )
;

/** Index for core.glycoprotein.expressed_species (foreign key) */
CREATE  INDEX index_gly__expressed_species ON core.glycoprotein ( expressed_species )
;

----- indexes for table core.glycoprotein_attachment -----

/** Index for core.glycoprotein_attachment.glycoprotein_id (foreign key) */
CREATE  INDEX index_gly_att__glycoprotein_id ON core.glycoprotein_attachment ( glycoprotein_id )
;

----- indexes for table core.technique -----

/** Index for core.technique.technique_id (primary key) */
CREATE UNIQUE INDEX index_tec__technique ON core.technique ( technique_id, technique_name, technique_abbrev )
;

----- indexes for table core.experiment -----


----- indexes for table core.evidence -----

/** Index for core.evidence.experiment_id (foreign key) */
CREATE  INDEX index_evi__experiment_id ON core.evidence ( experiment_id )
;

/** Index for core.evidence.technique_id (foreign key) */
CREATE  INDEX index_evi__technique_id ON core.evidence ( technique_id )
;

/** Index for core.evidence.contributor_id (foreign key) */
CREATE  INDEX index_evi__contributor_id ON core.evidence ( contributor_id )
;

----- indexes for table core.experiment_step -----

/** Index for core.experiment_step.parent_experiment_step_id (foreign key) */
CREATE  INDEX index_exp_ste__parent_experiment_step_id ON core.experiment_step ( parent_experiment_step_id )
;

/** Index for core.experiment_step.experiment_id (foreign key) */
CREATE  INDEX index_exp_ste__experiment_id ON core.experiment_step ( experiment_id )
;

/** Index for core.experiment_step.evidence_id (foreign key) */
CREATE  INDEX index_exp_ste__evidence_id ON core.experiment_step ( evidence_id )
;

/** Index for core.experiment_step.technique_id (foreign key) */
CREATE  INDEX index_exp_ste__technique_id ON core.experiment_step ( technique_id )
;

/** Index for core.experiment_step.contributor_id (foreign key) */
CREATE  INDEX index_exp_ste__contributor_id ON core.experiment_step ( contributor_id )
;

----- indexes for table core.journal -----


----- indexes for table core.reference -----

/** Index for core.reference.contributor_id (foreign key) */
CREATE  INDEX index_ref__contributor_id ON core.reference ( contributor_id )
;

CREATE INDEX index_ref__external_ref ON core.reference ( external_reference_name, external_reference_id )
;


----- indexes for table core.journal_reference -----

/** Index for core.journal_reference.reference_id (foreign key) */
CREATE  INDEX index_jou_ref__reference_id ON core.journal_reference ( reference_id )
;

/** Index for core.journal_reference.journal_id (foreign key) */
CREATE  INDEX index_jou_ref__journal_id ON core.journal_reference ( journal_id )
;

----- indexes for table core.glycan_sequence_to_biological_context -----

/** Index for core.glycan_sequence_to_biological_context.glycan_sequence_id (foreign key) */
CREATE  INDEX index_gly_seq_to_bio_con__glycan_sequence_id ON core.glycan_sequence_to_biological_context ( glycan_sequence_id )
;

/** Index for core.glycan_sequence_to_biological_context.biological_context_id (foreign key) */
CREATE  INDEX index_gly_seq_to_bio_con__biological_context_id ON core.glycan_sequence_to_biological_context ( biological_context_id )
;

----- indexes for table core.biological_context_to_perturbation -----

/** Index for core.biological_context_to_perturbation.biological_context_id (foreign key) */
CREATE  INDEX index_bio_con_to_per__biological_context_id ON core.biological_context_to_perturbation ( biological_context_id )
;

/** Index for core.biological_context_to_perturbation.perturbation_id (foreign key) */
CREATE  INDEX index_bio_con_to_per__perturbation_id ON core.biological_context_to_perturbation ( perturbation_id )
;

----- indexes for table core.biological_context_to_disease -----

/** Index for core.biological_context_to_disease.biological_context_id (foreign key) */
CREATE  INDEX index_bio_con_to_dis__biological_context_id ON core.biological_context_to_disease ( biological_context_id )
;

/** Index for core.biological_context_to_disease.disease_id (foreign key) */
CREATE  INDEX index_bio_con_to_dis__disease_id ON core.biological_context_to_disease ( disease_id )
;

----- indexes for table core.glycan_sequence_to_evidence -----

/** Index for core.glycan_sequence_to_evidence.glycan_sequence_id (foreign key) */
CREATE  INDEX index_gly_seq_to_evi__glycan_sequence_id ON core.glycan_sequence_to_evidence ( glycan_sequence_id )
;

/** Index for core.glycan_sequence_to_evidence.evidence_id (foreign key) */
CREATE  INDEX index_gly_seq_to_evi__evidence_id ON core.glycan_sequence_to_evidence ( evidence_id )
;

----- indexes for table core.evidence_to_reference -----

/** Index for core.evidence_to_reference.evidence_id (foreign key) */
CREATE  INDEX index_evi_to_ref__evidence_id ON core.evidence_to_reference ( evidence_id )
;

/** Index for core.evidence_to_reference.reference_id (foreign key) */
CREATE  INDEX index_evi_to_ref__reference_id ON core.evidence_to_reference ( reference_id )
;

----- indexes for table core.biological_context_to_glycoconjugate -----

/** Index for core.biological_context_to_glycoconjugate.biological_context_id (foreign key) */
CREATE  INDEX index_bio_con_to_gly__biological_context_id ON core.biological_context_to_glycoconjugate ( biological_context_id )
;

/** Index for core.biological_context_to_glycoconjugate.glycoconjugate_id (foreign key) */
CREATE  INDEX index_bio_con_to_gly__glycoconjugate_id ON core.biological_context_to_glycoconjugate ( glycoconjugate_id )
;

----- indexes for table core.glycan_sequence_to_reference -----

/** Index for core.glycan_sequence_to_reference.glycan_sequence_id (foreign key) */
CREATE  INDEX index_gly_seq_to_ref__glycan_sequence_id ON core.glycan_sequence_to_reference ( glycan_sequence_id )
;

/** Index for core.glycan_sequence_to_reference.reference_id (foreign key) */
CREATE  INDEX index_gly_seq_to_ref__reference_id ON core.glycan_sequence_to_reference ( reference_id )
;

----- indexes for table core.biological_context_to_experiment -----

/** Index for core.biological_context_to_experiment.biological_context_id (foreign key) */
CREATE  INDEX index_bio_con_to_exp__biological_context_id ON core.biological_context_to_experiment ( biological_context_id )
;

/** Index for core.biological_context_to_experiment.experiment_id (foreign key) */
CREATE  INDEX index_bio_con_to_exp__experiment_id ON core.biological_context_to_experiment ( experiment_id )
;

----- indexes for table core.tissue_taxonomy_relations -----

/** Index for core.tissue_taxonomy_relations.left_index (unique column) */
CREATE UNIQUE INDEX index_tis_tax_rel__left_index ON core.tissue_taxonomy_relations ( left_index )
;

/** Index for core.tissue_taxonomy_relations.right_index (unique column) */
CREATE UNIQUE INDEX index_tis_tax_rel__right_index ON core.tissue_taxonomy_relations ( right_index )
;

----- indexes for table core.taxonomy_relations -----

/** Index for core.taxonomy_relations.left_index (unique column) */
CREATE UNIQUE INDEX index_tax_rel__left_index ON core.taxonomy_relations ( left_index )
;

/** Index for core.taxonomy_relations.right_index (unique column) */
CREATE UNIQUE INDEX index_tax_rel__right_index ON core.taxonomy_relations ( right_index )
;

----- indexes for table core.disease_relations -----

/** Index for core.disease_relations.left_index (unique column) */
CREATE UNIQUE INDEX index_dis_rel__left_index ON core.disease_relations ( left_index )
;

/** Index for core.disease_relations.right_index (unique column) */
CREATE UNIQUE INDEX index_dis_rel__right_index ON core.disease_relations ( right_index )
;

----- indexes for table core.perturbation_relations -----

/** Index for core.perturbation_relations.left_index (unique column) */
CREATE UNIQUE INDEX index_per_rel__left_index ON core.perturbation_relations ( left_index )
;

/** Index for core.perturbation_relations.right_index (unique column) */
CREATE UNIQUE INDEX index_per_rel__right_index ON core.perturbation_relations ( right_index )
;

----- indexes for table core.glycoprotein_association -----

/** Index for core.glycoprotein_association.glycan_sequence_id (foreign key) */
CREATE  INDEX index_gly_ass__glycan_sequence_id ON core.glycoprotein_association ( glycan_sequence_id )
;

/** Index for core.glycoprotein_association.glycoconjugate_id (foreign key) */
CREATE  INDEX index_gly_ass__glycoconjugate_id ON core.glycoprotein_association ( glycoconjugate_id )
;

/** Index for core.glycoprotein_association.biological_context_id (foreign key) */
CREATE  INDEX index_gly_ass__biological_context_id ON core.glycoprotein_association ( biological_context_id )
;
