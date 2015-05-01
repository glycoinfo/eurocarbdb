-- schema comment --
COMMENT ON SCHEMA core
IS '=head1  EuroCarbDB core schema - table creation

This SQL script creates all core EuroCarbDB tables. It\'s important to note 
that only the tables and their associated constraints are created here 
creating the initial database and schema, dropping pre-existing tables, etc
is the responsibility of other scripts. 

=head2  Overview 

This schema design can be conceptually reduced down to the relationships between
4 basic data types/tables - 

    -   glycan sequence - all information pertaining to structural aspects of glycans.
        This includes all physical/invariant data about component monosaccharides,
        monosaccharide composition, mass, sequence, etc. 
    -   biological context - all information pertaining to where structures are found
        in biological systems, including taxonomic and tissue information, associated
        diseases and chemical treatments "perturbations" (if any).
    -   evidence - the actual scientific data for glycan structures found in 
        particular biological contexts, including which technique was used, all associated
        experimental data, etc. The term \'evidence\' here is also analogous to 
        \'experiment\', since 1 piece of evidence refers to the results of a single 
        experimental technique.
    -   reference - information relating to the publication of experimental results -
        normally a journal article.

=head3  Example 1

A study comparing glycans from healthy human brain and ischaemic 
(oxygen-starved) brain finds a total of 4 new glycan structures from a combination 
of mass spec, lectins and sequential enzymatic degradation would create the following
database entries:

    -   4 rows in table #glycan_sequence# for each new glycan sequence.
    -   2 rows in table #biological_context#, one for human brain, and one for 
        ischaemic human brain; with one row in the #biological_context_to_perturbation#
        table to point to the row corresponding to \'ischaemia\' in table #perturbation#.
    -   1 row in the #evidence# table for each mass spec run, with rows also introduced
        in table #glycan_sequence_to_evidence# to point to the glycan structures 
        identified in the mass spec, and rows introduced into table 
        #evidence_to_biological_context#. Rows would also be entered into the relevant 
        mass spec data tables to hold the actual experimental data, and rows placed into
        the #glycan_sequence_to_evidence# table to map which structures were found in
        which mass spec experiment.
    -   Likewise, 1 row in the #evidence# table for each lectin and enzyme degradation
        experiment, with associated rows in #glycan_sequence_to_evidence#, 
        #evidence_to_biological_context#, as well as the relevant method-specific
        experimental data tables for lectins and enzymatic methods.
    -   If the work were published, then 1 row would be entered into the #reference#
        table, and rows entered into the #evidence_to_reference# table for each piece#
        of evidence published.

=head3  Example 2



=head2  Conventions

Markup conventions:

    ->  words enclosed by \'_\' mean that that text is _italicised_.
    ->  words enclosed by \'*\' mean that that text is *bold*.
    ->  words enclosed by \'#\' mean that that text is #literal#. 

Database naming conventions:

    ->  all identifiers are lower case, words separated by underscores #like_this#.
    ->  SQL text is upper case, eg: #CREATE TABLE my_table (...);#
    ->  primary key column names are formed from the table name + the suffix \'_id\',
        eg: the primary key column for the table #person# is always #person_id#.
    ->  column names that end in \'_id\' that are not primary keys of the table
        in which they are found are foreign keys.
    ->  join tables, that is, tables whose main purpose for being is to encapsulate
        relations between other tables, are named as \'<table1>_to_<table2>\', where
        table1 and table2 are the tables so related.
    ->  views are prefixed with \'view_\'.

Programming conventions:

    ->  Wherever possible, write SQL statement using views, not tables. It is 
        perfectly acceptable to use the \'SELECT * FROM\' syntax with views.
    ->  All columns in an insert must be named 
        form \'INSERT INTO evidence VALUES (...)\', rather the following 
        should be used: 
        
            INSERT INTO evidence ( column1, column2, ... ) 
                          VALUES ( value1, value2, ... )
    
        or the alternative syntax:
        
            INSERT INTO evidence SET column1 = value1, column2 = value2, ...

    ->  More to come!

        
=head2  Issues to grapple with in the future
    
    ->  indefinite structures (sequences/structures that have unknown elements).
    ->  what exactly constitutes a "monosaccharide residue"?
    ->  issues around structures identified from recombinant sources.
    ->  do we need a composition table?
    ->  glycoconjugates
    ->  where/how best to store heterogeneity info.

=head2 Authors

Matt Harrison <matt@ebi.ac.uk>

Version: $Id:$ 

Changelog:
    
	mjh 2006-06-14		
	- 	4 new tables have also been added for the tree relationships of taxonomy, 
		tissue_taxonomy,  perturbation, and disease. These tables are named \'*_relations\', 
		and eliminate the need to use recursive SQL (which Postgres doesn\'t support) to 
		perform child/parent queries.
	- 	Tables \'experiment\' and \'biological_context_to_experiment\' have been added.
	- 	Foreign key \'experiment_id\' added to evidence table & experiment_step table.
	- 	Table \'glycoprotein\' added. Glycoconjugate handling is still crappy, and needs 
		revision at a later date.
	- 	Some elements of documentation improved/clarified.
	

';


-- table core.contributor --
COMMENT ON TABLE core.contributor
IS 'This table represents EITHER a single individual, or a research group/institution.';

-- columns for table core.contributor --
COMMENT ON COLUMN core.contributor.contributor_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.contributor.contributor_name
IS '';
COMMENT ON COLUMN core.contributor.date_entered
IS '';


-- table core.glycan_sequence --
COMMENT ON TABLE core.glycan_sequence
IS 'Glycan sequences. One row in this table refers to a single carbohydrate sequence, which may be found in one or more _biological contexts_, each supported by one or more pieces of _evidence_.';

-- columns for table core.glycan_sequence --
COMMENT ON COLUMN core.glycan_sequence.glycan_sequence_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.glycan_sequence.sequence_iupac
IS 'Glycan sequence in IUPAC format. Residue (monosaccharide) names must be unique identifiers and have a definition in the associated monosaccharide database/dictionary.';
COMMENT ON COLUMN core.glycan_sequence.sequence_ct
IS 'Glycan sequence in connection table format.';
COMMENT ON COLUMN core.glycan_sequence.sequence_ct_condensed
IS 'sequence_ct condensed';
COMMENT ON COLUMN core.glycan_sequence.residue_count
IS 'Number of residues in sequence, where _residue_ refers to individual monosaccharides (as determined by the associated monosaccharide database). This field is derived from the sequence. Some indefinite structures will still have a defined monosaccharide count.';
COMMENT ON COLUMN core.glycan_sequence.mass_monoisotopic
IS 'Monoisotopic mass in Daltons (Da). This should be the full, unionised (ie: all acid groups protonated) molecular mass (as distinct from the residue mass). _Indefinite structures_ (ie: structures that cannot have a discrete mass due to repeats, etc) should have the value NULL.';
COMMENT ON COLUMN core.glycan_sequence.mass_average
IS 'Average mass in Daltons (Da); otherwise the same as for mass_monoisotopic.';
COMMENT ON COLUMN core.glycan_sequence.composition
IS 'A pure text string encoding monosaccharide/residue composition, eg: \'Glc:2;GlcNAc:1;Man:2\'. The names of monosaccharides in the composition must be found in the monosaccharide database. The order of monosaccharides in the string should be alphabetically sorted. This field is derived from the sequence. If the given structure is _indefinite_, this field will should be NULL to indicate that composition is indefinite.';
COMMENT ON COLUMN core.glycan_sequence.date_entered
IS 'Date this structure was first entered.';
COMMENT ON COLUMN core.glycan_sequence.date_contributed
IS 'Date this structure was accepted into master DB.';
COMMENT ON COLUMN core.glycan_sequence.contributor_id
IS 'The first contributor of this structure/sequence. Foreign key to contributor table.';


-- table core.taxonomy --
COMMENT ON TABLE core.taxonomy
IS 'Composition of a glycan sequence. Stored as simple "name(string)" = "value(integer)" assignment e.g: (\'Glc\', 2) (\'GlcNAc\', 1) (\'Hex\', 5) ... / /******************************************************** Reference table of species/taxonomy, largely derived from the NCBI taxonomy database/MeSH. Species taxonomies essentially form a tree data structure (ie: a single-rooted, directed, acyclic graph), where top-level taxa (animals, plants, bacteria, fungi, etc) are the children of some arbitrary root node (for convenience). References: NCBI taxonomy home page: http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/ MeSH taxonomy tree: http://www.nlm.nih.gov/mesh/2005/MeSHtree.B.html';

-- columns for table core.taxonomy --
COMMENT ON COLUMN core.taxonomy.taxonomy_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.taxonomy.parent_taxonomy_id
IS 'The parent taxon of this taxon';
COMMENT ON COLUMN core.taxonomy.ncbi_id
IS 'Taxon identifier from the NCBI Taxonomy online DB at http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/';
COMMENT ON COLUMN core.taxonomy.rank
IS 'Taxon rank - kingdom > order > family > species etc.';
COMMENT ON COLUMN core.taxonomy.taxon
IS '"Official" taxon name, eg: Escherichia coli.';


-- table core.taxonomy_synonym --
COMMENT ON TABLE core.taxonomy_synonym
IS 'Taxonomy synonyms.';

-- columns for table core.taxonomy_synonym --
COMMENT ON COLUMN core.taxonomy_synonym.taxonomy_synonym_id
IS '';
COMMENT ON COLUMN core.taxonomy_synonym.taxonomy_id
IS 'The parent taxon of this taxon';
COMMENT ON COLUMN core.taxonomy_synonym.synonym
IS 'Synonym for this taxon';


-- table core.tissue_taxonomy --
COMMENT ON TABLE core.tissue_taxonomy
IS 'Reference table of organs/tissues/cells. Basically everything smaller than whole organisms should go here. The suggested working vocabulary/taxonomy for sub-organismal biology is the "Anatomy" sub-tree from MeSH, see: http://www.nlm.nih.gov/mesh/2005/MeSHtree.A.html .';

-- columns for table core.tissue_taxonomy --
COMMENT ON COLUMN core.tissue_taxonomy.tissue_taxonomy_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.tissue_taxonomy.parent_tissue_taxonomy_id
IS 'The parent taxon of this taxon';
COMMENT ON COLUMN core.tissue_taxonomy.tissue_taxon
IS '"Official" taxon name, egs: liver, skin, fibroblast. For MeSH this is the value of the MeSH \'MH\' (main heading) field.';
COMMENT ON COLUMN core.tissue_taxonomy.mesh_id
IS 'MeSH id. This is the value of the MeSH \'MN\' field.';
COMMENT ON COLUMN core.tissue_taxonomy.description
IS 'MeSH description. This is the value of the MeSH \'MS\' field.';
COMMENT ON COLUMN core.tissue_taxonomy.date_last_modified
IS 'The date on which this entry was last modified (most likely by MeSH).';


-- table core.tissue_taxonomy_synonym --
COMMENT ON TABLE core.tissue_taxonomy_synonym
IS 'Tissue taxonomy synonyms. See also the tissue_taxonomy table.';

-- columns for table core.tissue_taxonomy_synonym --
COMMENT ON COLUMN core.tissue_taxonomy_synonym.tissue_taxonomy_synonym_id
IS '';
COMMENT ON COLUMN core.tissue_taxonomy_synonym.tissue_taxonomy_id
IS 'The parent taxon of this taxon';
COMMENT ON COLUMN core.tissue_taxonomy_synonym.synonym
IS 'Synonym for this tissue taxon';


-- table core.biological_context --
COMMENT ON TABLE core.biological_context
IS 'This table represents the collection of _biological contexts_ in which glycan structures/sequences are found. The term _biological context_ refers to a specific _tissue taxonomy_ (organ/tissue/subcellular location) of a specific _taxonomy_ (species/family) in which one or more _glycan structures_ are found. A biological context also comprises zero or more _diseases_ and/or _chemical perturbations_; for example, human liver is a different biological context than human liver with cirrhosis, which is a different biological context than human liver with hepatoma and cirrhosis. Chemical perturbations, such as treatment with an antibiotic, also define their own distinct biological context, eg: human liver with hepatoma is a different biological context than human liver with hepatoma treated with some anti-cancer drug. Chemically-induced perturbations associated with this biological context are found in the #perturbation# table, and connected to #biological_context# by the #biological_context_to_perturbation table#. Diseases applicable to a biological context are found in the #disease# and #biological_context_to_disease# tables, and glycoconjugates in the #glycoconjugate# table in the same manner. New biological context entries are created each time a new piece of _evidence_ is entered.';

-- columns for table core.biological_context --
COMMENT ON COLUMN core.biological_context.biological_context_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.biological_context.taxonomy_id
IS 'The organism from which a structure is identified. Foreign key to taxonomy table.';
COMMENT ON COLUMN core.biological_context.tissue_taxonomy_id
IS 'A taxonomic identifier below the level of organism, and orthogonal to taxonomy_id, eg: brain, lung, alveolus, erythrocyte.';
COMMENT ON COLUMN core.biological_context.contributor_id
IS 'The contributor of this context';
COMMENT ON COLUMN core.biological_context.date_entered
IS '';
COMMENT ON COLUMN core.biological_context.comments
IS 'User-specific comments relevant to this sample/biological_context.';


-- table core.perturbation --
COMMENT ON TABLE core.perturbation
IS 'Reference table encapsulating all non-disease perturbations of biological contexts. It includes all chemical perturbations of natural state, such as treatment with antibiotics, or high salt conditions. Depending on how much information we store about the exact detail of the chemical treatment, this table could get very complex. We should probably at least store references to pertinent chemical compounds used. This table could use some extra information to further characterise perturbations more comprehensively.';

-- columns for table core.perturbation --
COMMENT ON COLUMN core.perturbation.perturbation_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.perturbation.parent_perturbation_id
IS 'Parent perturbation id';
COMMENT ON COLUMN core.perturbation.perturbation_name
IS 'Name/summary of this perturbation.';
COMMENT ON COLUMN core.perturbation.mesh_id
IS 'MeSH id. This is the value of the MeSH \'MN\' field.';
COMMENT ON COLUMN core.perturbation.description
IS 'MeSH description. This is the value of the MeSH \'MS\' field.';
COMMENT ON COLUMN core.perturbation.date_last_modified
IS 'The date on which this entry was last modified (most likely by MeSH).';


-- table core.disease --
COMMENT ON TABLE core.disease
IS 'Reference table of diseases, compiled from the NCBI MeSH disease vocabulary (MeSH category C, see http://www.nlm.nih.gov/mesh/).';

-- columns for table core.disease --
COMMENT ON COLUMN core.disease.disease_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.disease.parent_disease_id
IS 'Parent disease id';
COMMENT ON COLUMN core.disease.disease_name
IS 'Canonical disease name';
COMMENT ON COLUMN core.disease.mesh_id
IS 'MESH disease id';
COMMENT ON COLUMN core.disease.description
IS 'MeSH description. This is the value of the MeSH \'MS\' field.';
COMMENT ON COLUMN core.disease.date_last_modified
IS 'The date on which this entry was last modified (most likely by MeSH).';


-- table core.disease_synonym --
COMMENT ON TABLE core.disease_synonym
IS 'Disease synonyms for the reference table of diseases in the disease table.';

-- columns for table core.disease_synonym --
COMMENT ON COLUMN core.disease_synonym.disease_synonym_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.disease_synonym.disease_id
IS 'The disease for which this is a synonym';
COMMENT ON COLUMN core.disease_synonym.synonym
IS 'Synonym for this disease';


-- table core.glycoconjugate --
COMMENT ON TABLE core.glycoconjugate
IS 'Table for storing references to glycoconjugates. For the purposes of the DB schema, "glycoconjugate" may be thought of as a base type of all specific glycoconjugate types.';

-- columns for table core.glycoconjugate --
COMMENT ON COLUMN core.glycoconjugate.glycoconjugate_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.glycoconjugate.glycoconjugate_type
IS 'Type of glycoconjugate (enumeration). This is not an exhaustive list.';
COMMENT ON COLUMN core.glycoconjugate.glycoconjugate_table
IS 'Table in which reference information for this glyconjugate may be found.';
COMMENT ON COLUMN core.glycoconjugate.glycoconjugate_table_id
IS 'Key corresponding to the correct row in #glycoconjugate_table#';


-- table core.glycoprotein --
COMMENT ON TABLE core.glycoprotein
IS 'Table for storing information about glycoproteins.';

-- columns for table core.glycoprotein --
COMMENT ON COLUMN core.glycoprotein.glycoprotein_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.glycoprotein.uniprot_id
IS 'Uniprot accession id. "Foreign key" to Uniprot. Can be NULL, meaning that the protein is not found in uniprot.';
COMMENT ON COLUMN core.glycoprotein.variant
IS 'Name of glycoprotein sequence variant, if applicable.';
COMMENT ON COLUMN core.glycoprotein.glycoprotein_name
IS 'Canonical glycoprotein name, cached from uniprot.';
COMMENT ON COLUMN core.glycoprotein.glycoprotein_sequence
IS 'Full protein or peptide sequence.';
COMMENT ON COLUMN core.glycoprotein.sequence_offset
IS 'If only a *portion* of a full sequence is expressed, then this value indicates the offset into the full sequence as reported by uniprot.';
COMMENT ON COLUMN core.glycoprotein.native_species
IS 'This sequence\'s native species. NULL means that this sequence is artificial.';
COMMENT ON COLUMN core.glycoprotein.expressed_species
IS 'If expressed in a non-native species, this is the species in which this sequence was expressed. NULL means that this sequence is native. Note that by this definition, native_species and recombinant species can never be the same.';


-- table core.glycoprotein_attachment --
COMMENT ON TABLE core.glycoprotein_attachment
IS 'Table for storing references to glycoproteins.';

-- columns for table core.glycoprotein_attachment --
COMMENT ON COLUMN core.glycoprotein_attachment.glycoprotein_attachment_id
IS 'Primary key, generated from glycoconjugate table. This means that these ids will not necessarily be contiguous.';
COMMENT ON COLUMN core.glycoprotein_attachment.glycoprotein_id
IS 'The glycoprotein sequence';
COMMENT ON COLUMN core.glycoprotein_attachment.aminoacid_position
IS 'Position of attached amino-acid in the sequence given by uniprot_id above. Note that this amino-acid must be the same as that given by aminoacid_attached.';
COMMENT ON COLUMN core.glycoprotein_attachment.attachment_type
IS 'Classification of attachement type, eg: O-linked, N-linked, C-linked';
COMMENT ON COLUMN core.glycoprotein_attachment.aminoacid_attached
IS '1-letter abbreviation of amino-acid to which this glycan is attached.';


-- table core.technique --
COMMENT ON TABLE core.technique
IS 'Experimental technique reference table.';

-- columns for table core.technique --
COMMENT ON COLUMN core.technique.technique_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.technique.technique_abbrev
IS 'Short/abbreviated name of this technique, eg: ESI-MS.';
COMMENT ON COLUMN core.technique.technique_name
IS 'Name of technique, eg: electrospray mass spectrometry';


-- table core.experiment --
COMMENT ON TABLE core.experiment
IS '';

-- columns for table core.experiment --
COMMENT ON COLUMN core.experiment.experiment_id
IS 'Primary key';
COMMENT ON COLUMN core.experiment.contributor_id
IS 'Link to the group/lab/remote installation that contributed this piece of evidence.';
COMMENT ON COLUMN core.experiment.experiment_comments
IS 'Additional user-specified comments relating to the objective/purpose of this experiment.';
COMMENT ON COLUMN core.experiment.date_entered
IS 'The date when this experiment was first created.';


-- table core.evidence --
COMMENT ON TABLE core.evidence
IS 'This is the central lookup table for experimental data. One row in this table refers to one result obtained from one technique (which may provide evidence for multiple structures).';

-- columns for table core.evidence --
COMMENT ON COLUMN core.evidence.evidence_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.evidence.experiment_id
IS 'The experiment during which this piece of evidence was acquired.';
COMMENT ON COLUMN core.evidence.technique_id
IS 'The technique/method that was used to obtain this piece of evidence. Foreign key to technique table.';
COMMENT ON COLUMN core.evidence.contributor_id
IS 'Link to the group/lab/remote installation that contributed this piece of evidence.';
COMMENT ON COLUMN core.evidence.contributor_evidence_id
IS 'The evidence_id at the remote site.';
COMMENT ON COLUMN core.evidence.evidence_type
IS 'Type discriminator of the type of this Evidence, eg: ms, hplc, nmr.';
COMMENT ON COLUMN core.evidence.date_entered
IS 'This is the date that the data was entered into the DB.';


-- table core.experiment_step --
COMMENT ON TABLE core.experiment_step
IS 'This table\'s purpose is to record the sequence of techniques used to generate evidence, since not all techniques generate recordable data (evidence). This table comprises a recursive (tree) relationship between steps as it is conceivable that samples & the techniques applied to them may diverge during an experimental workflow. Question: should we allow "free text" description of the techniques used in experiment steps? The only alternative is to exhaustively populate the technique table with as many techniques as are likely to be used by most people, and to provide the capability to add new techniques on demand.';

-- columns for table core.experiment_step --
COMMENT ON COLUMN core.experiment_step.experiment_step_id
IS 'Primary key';
COMMENT ON COLUMN core.experiment_step.parent_experiment_step_id
IS 'Link to the experiment step that preceded this step. Recursive secondary key to this table.';
COMMENT ON COLUMN core.experiment_step.experiment_id
IS 'Link to the experiment in which this experimental step was performed.';
COMMENT ON COLUMN core.experiment_step.evidence_id
IS 'Link to any actual recorded _evidence_ (that is, data) arising from this experimental step. Not all steps will have associated data (evidence); this is indicated by a NULL in this column. Foreign key to evidence table.';
COMMENT ON COLUMN core.experiment_step.technique_id
IS 'The technique used in this experiment step. Foreign key to the technique table.';
COMMENT ON COLUMN core.experiment_step.contributor_id
IS 'The usergroup that performed this experiment step. Foreign key to the contributor table.';
COMMENT ON COLUMN core.experiment_step.date_entered
IS 'This is the date that the data was entered into the DB.';
COMMENT ON COLUMN core.experiment_step.date_obtained
IS 'This is the date the data was considered to have been collected.';
COMMENT ON COLUMN core.experiment_step.experiment_step_parameters
IS 'XML column to store any/all additional experimental step parameters that are not able to be stored in the current schema but are deemed to be "worth" storing. XML schema for validating/checking this XML will ideally be found in the \'parameter_schema\' column of the technique table.';
COMMENT ON COLUMN core.experiment_step.experiment_step_comments
IS 'Optional additional user-specified comments relating to the execution of this experiment step.';


-- table core.journal --
COMMENT ON TABLE core.journal
IS 'Publication/reference journal catalog.';

-- columns for table core.journal --
COMMENT ON COLUMN core.journal.journal_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.journal.journal_title
IS 'Full title of journal.';
COMMENT ON COLUMN core.journal.journal_abbrev
IS 'Abbreviated title of journal.';


-- table core.reference --
COMMENT ON TABLE core.reference
IS 'Publication/reference information. This includes: journal articles, external database entries relevant to a Eurocarb entry, and/or an external website. That is, in addition to containing numerous journal articles, this table also contains rows for each structure contributed by Carbbank, as well as a Reference for Carbbank itself.';

-- columns for table core.reference --
COMMENT ON COLUMN core.reference.reference_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.reference.contributor_id
IS 'The contributor who created "owns" this reference';
COMMENT ON COLUMN core.reference.reference_type
IS 'Type of reference Typical values: \'journal\', \'database\', \'website\'';
COMMENT ON COLUMN core.reference.date_entered
IS '';
COMMENT ON COLUMN core.reference.external_reference_name
IS 'The canonical name of the external reference source, eg: for journal references this would usually be \'pubmed\', for a carbbank reference \'carbbank\', for a KEGG reference \'kegg\', etc.';
COMMENT ON COLUMN core.reference.external_reference_id
IS 'The ID of this reference as given by the external entity/organisation to which this reference is linked.';
COMMENT ON COLUMN core.reference.external_reference_name
IS '';
COMMENT ON COLUMN core.reference.url
IS 'A direct URL for this reference, may be NULL if not relevant or the URL can be entirely dynamically generated from the external_reference_id, eg: Pubmed';
COMMENT ON COLUMN core.reference.reference_comments
IS 'Any extra information pertaining to this reference that may be useful.';


-- table core.journal_reference --
COMMENT ON TABLE core.journal_reference
IS 'Publication/reference information specifically for journal articles.';

-- columns for table core.journal_reference --
COMMENT ON COLUMN core.journal_reference.journal_reference_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.journal_reference.reference_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.journal_reference.journal_id
IS 'Scientific journal id; foreign key to journal table.';
COMMENT ON COLUMN core.journal_reference.pubmed_id
IS 'Pubmed id, from http://www.ncbi.nlm.nih.gov/entrez/query.fcgi This should be equal to core.reference.external_reference_id.';
COMMENT ON COLUMN core.journal_reference.authors
IS 'Authorlist of publication.';
COMMENT ON COLUMN core.journal_reference.title
IS 'title of journal article';
COMMENT ON COLUMN core.journal_reference.publication_year
IS 'Year of publication.';
COMMENT ON COLUMN core.journal_reference.journal_volume
IS 'Volume number of publication.';
COMMENT ON COLUMN core.journal_reference.journal_start_page
IS 'Starting page number of publication.';
COMMENT ON COLUMN core.journal_reference.journal_end_page
IS 'Ending page number of publication.';


-- table core.glycan_sequence_to_biological_context --
COMMENT ON TABLE core.glycan_sequence_to_biological_context
IS 'Join table to map glycan sequence to the one or more biological contexts in which it has been found.';

-- columns for table core.glycan_sequence_to_biological_context --
COMMENT ON COLUMN core.glycan_sequence_to_biological_context.glycan_sequence_to_biological_context_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.glycan_sequence_to_biological_context.glycan_sequence_id
IS 'A glycan sequence. Foreign key to glycan_sequence table.';
COMMENT ON COLUMN core.glycan_sequence_to_biological_context.biological_context_id
IS 'The biological context for the glycan structure given by #glycan_sequence_id#. Foreign key to #biological_context# table.';


-- table core.evidence_to_biological_context --
COMMENT ON TABLE core.evidence_to_biological_context
IS 'Join table to map evidence to the biological contexts from which said evidence was found';

-- columns for table core.evidence_to_biological_context --
COMMENT ON COLUMN core.evidence_to_biological_context.evidence_to_biological_context_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.evidence_to_biological_context.evidence_id
IS 'Foreign key to evidence table.';
COMMENT ON COLUMN core.evidence_to_biological_context.biological_context_id
IS 'The biological context(s) from which the Evidence was obtained.';


-- table core.biological_context_to_perturbation --
COMMENT ON TABLE core.biological_context_to_perturbation
IS 'Join table for the many-many relationship between biological_context and perturbation. Biological contexts can have zero or more associated perturbations. These perturbations may refer to multiple independent biological contexts.';

-- columns for table core.biological_context_to_perturbation --
COMMENT ON COLUMN core.biological_context_to_perturbation.biological_context_to_perturbation_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.biological_context_to_perturbation.biological_context_id
IS 'Foreign key to biological context table.';
COMMENT ON COLUMN core.biological_context_to_perturbation.perturbation_id
IS 'Link to all disease-related and chemically-induced perturbations associated with this biological context.';


-- table core.biological_context_to_disease --
COMMENT ON TABLE core.biological_context_to_disease
IS 'Join table for the many-many relationship between biological_context and disease. Biological contexts can have zero or more associated diseases. Diseases in turn may be found in multiple independent biological contexts.';

-- columns for table core.biological_context_to_disease --
COMMENT ON COLUMN core.biological_context_to_disease.biological_context_to_disease_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.biological_context_to_disease.biological_context_id
IS 'Foreign key to biological_context table.';
COMMENT ON COLUMN core.biological_context_to_disease.disease_id
IS 'Foreign key to disease table.';


-- table core.glycan_sequence_to_evidence --
COMMENT ON TABLE core.glycan_sequence_to_evidence
IS 'Join table to provide the mapping between glycan structures and the their associated experimental evidence.';

-- columns for table core.glycan_sequence_to_evidence --
COMMENT ON COLUMN core.glycan_sequence_to_evidence.glycan_sequence_to_evidence_id
IS 'Auto-incrementing primary key.';
COMMENT ON COLUMN core.glycan_sequence_to_evidence.glycan_sequence_id
IS 'Foreign key to glycan_sequence table.';
COMMENT ON COLUMN core.glycan_sequence_to_evidence.evidence_id
IS 'Foreign key to evidence table.';
COMMENT ON COLUMN core.glycan_sequence_to_evidence.quantitation_by_percent
IS 'Heterogeneity quantitation.';


-- table core.evidence_to_reference --
COMMENT ON TABLE core.evidence_to_reference
IS 'Maps the many-many relationship between the evidence and reference tables. One piece of evidence may refer to several publications; one publication may (will often) provide multiple pieces of evidence.';

-- columns for table core.evidence_to_reference --
COMMENT ON COLUMN core.evidence_to_reference.evidence_to_reference_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.evidence_to_reference.evidence_id
IS 'Foreign key to evidence table.';
COMMENT ON COLUMN core.evidence_to_reference.reference_id
IS 'Foreign key to reference table.';


-- table core.biological_context_to_glycoconjugate --
COMMENT ON TABLE core.biological_context_to_glycoconjugate
IS 'Maps the many-many relationship between the evidence and reference tables. One piece of evidence may refer to several publications; one publication may (will often) provide multiple pieces of evidence.';

-- columns for table core.biological_context_to_glycoconjugate --
COMMENT ON COLUMN core.biological_context_to_glycoconjugate.biological_context_to_glycoconjugate_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.biological_context_to_glycoconjugate.biological_context_id
IS 'Foreign key to biological_context table.';
COMMENT ON COLUMN core.biological_context_to_glycoconjugate.glycoconjugate_id
IS 'Foreign key to glycoconjugate table.';


-- table core.glycan_sequence_to_reference --
COMMENT ON TABLE core.glycan_sequence_to_reference
IS 'Maps the many-many relationship between the glycan_sequence and reference tables. One sequence may be reported in several publications; likewise, one publication may (will often) report multiple sequences.';

-- columns for table core.glycan_sequence_to_reference --
COMMENT ON COLUMN core.glycan_sequence_to_reference.glycan_sequence_to_reference_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.glycan_sequence_to_reference.glycan_sequence_id
IS 'Foreign key to glycan_sequence table.';
COMMENT ON COLUMN core.glycan_sequence_to_reference.reference_id
IS 'Foreign key to reference table.';


-- table core.biological_context_to_experiment --
COMMENT ON TABLE core.biological_context_to_experiment
IS '';

-- columns for table core.biological_context_to_experiment --
COMMENT ON COLUMN core.biological_context_to_experiment.biological_context_to_experiment_id
IS 'Auto-incrementing primary key';
COMMENT ON COLUMN core.biological_context_to_experiment.biological_context_id
IS 'Foreign key to biological_context table.';
COMMENT ON COLUMN core.biological_context_to_experiment.experiment_id
IS 'Foreign key to experiment table.';


-- table core.tissue_taxonomy_relations --
COMMENT ON TABLE core.tissue_taxonomy_relations
IS 'Implements the nested set algorithm for performing parent/child queries on the tissue_taxonomy table. See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295';

-- columns for table core.tissue_taxonomy_relations --
COMMENT ON COLUMN core.tissue_taxonomy_relations.tissue_taxonomy_id
IS 'Foreign key to tissue_taxonomy table';
COMMENT ON COLUMN core.tissue_taxonomy_relations.left_index
IS 'Lower (left) index for this node\'s position in the tree.';
COMMENT ON COLUMN core.tissue_taxonomy_relations.right_index
IS 'Upper (right) index for this node\'s position in the tree.';


-- table core.taxonomy_relations --
COMMENT ON TABLE core.taxonomy_relations
IS 'Implements the nested set algorithm for performing parent/child queries on the taxonomy table. See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295';

-- columns for table core.taxonomy_relations --
COMMENT ON COLUMN core.taxonomy_relations.taxonomy_id
IS 'Foreign key to taxonomy table';
COMMENT ON COLUMN core.taxonomy_relations.left_index
IS 'Lower (left) index for this node\'s position in the tree.';
COMMENT ON COLUMN core.taxonomy_relations.right_index
IS 'Upper (right) index for this node\'s position in the tree.';


-- table core.disease_relations --
COMMENT ON TABLE core.disease_relations
IS 'Implements the nested set algorithm for performing parent/child queries on the disease table. See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295';

-- columns for table core.disease_relations --
COMMENT ON COLUMN core.disease_relations.disease_id
IS 'Foreign key to disease table';
COMMENT ON COLUMN core.disease_relations.left_index
IS 'Lower (left) index for this node\'s position in the tree.';
COMMENT ON COLUMN core.disease_relations.right_index
IS 'Upper (right) index for this node\'s position in the tree.';


-- table core.perturbation_relations --
COMMENT ON TABLE core.perturbation_relations
IS 'Implements the nested set algorithm for performing parent/child queries on the perturbation table. See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295';

-- columns for table core.perturbation_relations --
COMMENT ON COLUMN core.perturbation_relations.perturbation_id
IS 'Foreign key to perturbation table';
COMMENT ON COLUMN core.perturbation_relations.left_index
IS 'Lower (left) index for this node\'s position in the tree.';
COMMENT ON COLUMN core.perturbation_relations.right_index
IS 'Upper (right) index for this node\'s position in the tree.';


-- table core.glycoprotein_association --
COMMENT ON TABLE core.glycoprotein_association
IS 'Captures the association between a specific glycan sequence and a specific glycoprotein at an individual amino-acid level.';

-- columns for table core.glycoprotein_association --
COMMENT ON COLUMN core.glycoprotein_association.glycoprotein_association_id
IS 'Primary key';
COMMENT ON COLUMN core.glycoprotein_association.glycan_sequence_id
IS 'Foreign key to glycan_sequence table';
COMMENT ON COLUMN core.glycoprotein_association.glycoconjugate_id
IS 'Foreign key to glycoconjugate table';
COMMENT ON COLUMN core.glycoprotein_association.biological_context_id
IS 'Foreign key to biological context table';
COMMENT ON COLUMN core.glycoprotein_association.amino_acid
IS 'The identity of the amino-acid, 3 letter code';
COMMENT ON COLUMN core.glycoprotein_association.amino_acid_position
IS 'Amino-acid position in the glycoprotein sequence.';
COMMENT ON COLUMN core.glycoprotein_association.occupancy
IS 'Proportion of this amino-acid that is glycosylated in this biological context. 1.0 means this site is 100% glycosylated with the given glycan; 0.1 means that 10% of the glycoprotein population is glycosylated with this glycan.';

