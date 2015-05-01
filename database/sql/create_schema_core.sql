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
*   Last commit: $Rev$ by $Author$ on $Date$  
*/



/******************************************************************************

=head1  EuroCarbDB core schema - table creation

This SQL script creates all core EuroCarbDB tables. It's important to note 
that only the tables and their associated constraints are created here -- 
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
        experimental data, etc. The term 'evidence' here is also analogous to 
        'experiment', since 1 piece of evidence refers to the results of a single 
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
        table to point to the row corresponding to 'ischaemia' in table #perturbation#.
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

    ->  words enclosed by '_' mean that that text is _italicised_.
    ->  words enclosed by '*' mean that that text is *bold*.
    ->  words enclosed by '#' mean that that text is #literal#. 

Database naming conventions:

    ->  all identifiers are lower case, words separated by underscores #like_this#.
    ->  SQL text is upper case, eg: #CREATE TABLE my_table (...);#
    ->  primary key column names are formed from the table name + the suffix '_id',
        eg: the primary key column for the table #person# is always #person_id#.
    ->  column names that end in '_id' that are not primary keys of the table
        in which they are found are foreign keys.
    ->  join tables, that is, tables whose main purpose for being is to encapsulate
        relations between other tables, are named as '<table1>_to_<table2>', where
        table1 and table2 are the tables so related.
    ->  views are prefixed with 'view_'.

Programming conventions:

    ->  Wherever possible, write SQL statement using views, not tables. It is 
        perfectly acceptable to use the 'SELECT * FROM' syntax with views.
    ->  All columns in an insert must be named -- that is, no statements of
        form 'INSERT INTO evidence VALUES (...)', rather the following 
        should be used: 
        
            INSERT INTO evidence ( column1, column2, ... ) 
                          VALUES ( value1, value2, ... )
    
        or the alternative syntax:
        
            INSERT INTO evidence SET column1 = value1, column2 = value2, ...

        
=head2 Authors

mjh, various contributions from various other eurocarb developers.

*/

CREATE SCHEMA core;

/*===  TABLES  ===*/


/*  table contributor  *//*****************************************************
*
*   This table represents EITHER a single individual, or a research 
*   group/institution.
*/
CREATE TABLE core.contributor 
(
    /** Auto-incrementing primary key.  */
    contributor_id                      SERIAL PRIMARY KEY NOT NULL,
    
    contributor_name                    VARCHAR(128) NOT NULL,

    password                            VARCHAR(128),

    full_name                           VARCHAR(128),

    institution                         VARCHAR(128),

    is_admin                            BOOLEAN NOT NULL,    

    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    /** OpenID id, see http://openid.net */
    open_id                             VARCHAR(128)
    
    /* TODO - user system not specified at this point */
)
;


/*  table glycan_sequence  *//************************************************* 
*
*   Glycan sequences. One row in this table refers to a single carbohydrate
*   sequence, which may be found in one or more _biological contexts_, each supported
*   by one or more pieces of _evidence_.
*
*/
CREATE TABLE core.glycan_sequence 
(
    /** Auto-incrementing primary key. */
    glycan_sequence_id                  SERIAL PRIMARY KEY NOT NULL,

    /** Glycan sequence in IUPAC format. Residue (monosaccharide)
    *   names must be unique identifiers and have a definition in the associated
    *   monosaccharide database/dictionary.  
    */
    sequence_iupac                      VARCHAR(65535) UNIQUE NOT NULL,

    /** Glycan sequence in connection table format. */
    sequence_ct                         VARCHAR(65535) UNIQUE NOT NULL,
    
    /** sequence_ct condensed */
    /**sequence_ct_condensed               VARCHAR(65535) NOT NULL, */

    /* Glycan sequence in GlycoWorkbench format, used for drawing */
    sequence_gws                      VARCHAR(65535) NOT NULL,


    /** Number of residues in sequence, where _residue_ refers to
    *   individual monosaccharides (as determined by the associated
    *   monosaccharide database). This field is derived from the sequence.
    *   Some indefinite structures will still have a defined monosaccharide count.  
    */
    residue_count                       SMALLINT DEFAULT NULL,

    /** Monoisotopic mass in Daltons (Da). This should be the full, unionised 
    *   (ie: all acid groups protonated) molecular mass (as distinct from the 
    *   residue mass). _Indefinite structures_ (ie: structures that cannot have
    *   a discrete mass due to repeats, etc) should have the value NULL.  
    */
    mass_monoisotopic                   NUMERIC,

    /** Average mass in Daltons (Da); otherwise the same as for mass_monoisotopic. */
    mass_average                        NUMERIC
                                        CONSTRAINT check_avgmass_gt_monoiso_mass
                                        CHECK( mass_average >= mass_monoisotopic ),

    /** A pure text string encoding monosaccharide/residue composition,
    *   eg: 'Glc:2;GlcNAc:1;Man:2'. The names of monosaccharides in the composition
    *   must be found in the monosaccharide database. The order of monosaccharides
    *   in the string should be alphabetically sorted. This field is derived from
    *   the sequence. If the given structure is _indefinite_, this field will should be
    *   NULL to indicate that composition is indefinite.  
    */
    composition                         VARCHAR(64),

    --/** Foreign key to composition table. NULL means that this structure's composition
    --*    cannot be determined (ie: an _indefinite_ structure).  
    --*/
    --mjh: next line commented out until we resolve whether we need a composition table or not
    --composition_id                    INT,
    --FOREIGN KEY( composition_id ) REFERENCES core.composition ON UPDATE CASCADE, 

    /** Date this structure was first entered.  */
    date_entered                        TIMESTAMP WITH TIME ZONE 
                                        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    /** Date this structure was accepted into master DB.  */
    date_contributed                    TIMESTAMP WITH TIME ZONE 
                                        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        CONSTRAINT check_sensible_dates
                                        CHECK( date_contributed >= date_entered ),
    
    /** The first contributor of this structure/sequence. Foreign key to contributor table.  */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor 
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED
                                    
)
;


/*  table taxonomy  *//******************************************************** 
*
*   Reference table of species/taxonomy, largely derived from the NCBI taxonomy
*   database/MeSH. Species taxonomies essentially form a tree data structure 
*   (ie: a single-rooted, directed, acyclic graph), where top-level taxa 
*   (animals, plants, bacteria, fungi, etc) are the children of some arbitrary
*   root node (for convenience). 
*
*   References:
*
*   NCBI taxonomy home page:
*   http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/
*    
*   MeSH taxonomy tree:
*   http://www.nlm.nih.gov/mesh/2005/MeSHtree.B.html
*
*/
CREATE TABLE core.taxonomy 
(
    /** Auto-incrementing primary key  */
    taxonomy_id                         SERIAL PRIMARY KEY NOT NULL,
    
    /** The parent taxon of this taxon */
    parent_taxonomy_id                  INT NOT NULL
                                        REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Taxon identifier from the NCBI Taxonomy online DB 
    *   at http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/  */
    ncbi_id                             INT NOT NULL UNIQUE,

    /** Taxon rank - kingdom > order > family > species etc.  */    
    rank                                VARCHAR(16) DEFAULT 'species' NOT NULL,
                                        --CHECK( rank IN ( 'kingdom', 'phylum', 'class', 
                                         --                'order', 'family', 'genus', 'species' ) ),
    
    /** "Official" taxon name, eg: Escherichia coli.  */
    taxon                               VARCHAR(128) NOT NULL
                                        CHECK( taxon <> '' )
    
)
;


/*  table taxonomy_synonym  *//************************************************
*
*   Taxonomy synonyms.
*/
CREATE TABLE core.taxonomy_synonym
(
    taxonomy_synonym_id                 SERIAL PRIMARY KEY NOT NULL,
    
    /** The parent taxon of this taxon */
    taxonomy_id                         INT NOT NULL
                                        REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Synonym for this taxon  */
    synonym                             VARCHAR(200) NOT NULL
)
;


/*  table tissue_taxonomy  *//*************************************************
*
*   Reference table of organs/tissues/cells. Basically everything smaller than
*   whole organisms should go here. The suggested working vocabulary/taxonomy for 
*   sub-organismal biology is the "Anatomy" sub-tree from MeSH, see:
*   http://www.nlm.nih.gov/mesh/2005/MeSHtree.A.html .
*/
CREATE TABLE core.tissue_taxonomy 
(
    /** Auto-incrementing primary key  */
    tissue_taxonomy_id                  SERIAL PRIMARY KEY NOT NULL,

    /** The parent taxon of this taxon  */
    parent_tissue_taxonomy_id           INT NOT NULL
                                        REFERENCES core.tissue_taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** "Official" taxon name, egs: liver, skin, fibroblast. For MeSH this 
    *    is the value of the MeSH 'MH' (main heading) field. 
    */
    tissue_taxon                        VARCHAR(128) NOT NULL,
    
    /** MeSH id. This is the value of the MeSH 'MN' field.  */
    mesh_id                             VARCHAR(64) NOT NULL
                                        CHECK( mesh_id <> '' ),
                                        
    /** MeSH description. This is the value of the MeSH 'MS' field. */
    description                         TEXT DEFAULT NULL,     
       
    /** The date on which this entry was last modified (most likely by MeSH). */
    date_last_modified                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP    
                               
)
;


/*  table tissue_taxonomy_synonym  *//*****************************************
*
*   Tissue taxonomy synonyms. See also the tissue_taxonomy table.
*/
CREATE TABLE core.tissue_taxonomy_synonym
(
    tissue_taxonomy_synonym_id          SERIAL PRIMARY KEY NOT NULL,
    
    /** The parent taxon of this taxon  */
    tissue_taxonomy_id                  INT NOT NULL
                                        REFERENCES core.tissue_taxonomy
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Synonym for this tissue taxon  */
    synonym                             VARCHAR(64) UNIQUE NOT NULL
)
;

CREATE TABLE core.relationships_taxonomy (
  ncbi_id INT NOT NULL REFERENCES core.taxonomy(ncbi_id), 
  rank VARCHAR(200) NOT NULL, 
  ncbi_id_taxa INT NOT NULL REFERENCES core.taxonomy(ncbi_id), 
  relationship VARCHAR(200) NOT NULL, 
  position INT NOT NULL,
  PRIMARY KEY(ncbi_id,ncbi_id_taxa)
);


/*  table biological_context  *//********************************************** 
*
*   This table represents the collection of _biological contexts_ in which glycan 
*   structures/sequences are found. 
*
*   The term _biological context_ refers to a specific _tissue taxonomy_ 
*   (organ/tissue/subcellular location) of a specific _taxonomy_ (species/family) 
*   in which one or more _glycan structures_ are found. A biological context 
*   also comprises zero or more _diseases_ and/or _chemical perturbations_; for
*   example, human liver is a different biological context than human liver with
*   cirrhosis, which is a different biological context than human liver with hepatoma
*   and cirrhosis. Chemical perturbations, such as treatment with an antibiotic, also
*   define their own distinct biological context, eg: human liver with hepatoma is
*   a different biological context than human liver with hepatoma treated with 
*   some anti-cancer drug.
*
*   Chemically-induced perturbations associated with this biological context are 
*   found in the #perturbation# table, and connected to #biological_context#
*   by the #biological_context_to_perturbation table#. Diseases applicable to 
*   a biological context are found in the #disease# and #biological_context_to_disease#
*   tables, and glycoconjugates in the #glycoconjugate# table in the same manner. 
*
*   New biological context entries are created each time a new piece of _evidence_ 
*   is entered. 
*/
CREATE TABLE core.biological_context 
(
    /** Auto-incrementing primary key.  */
    biological_context_id               SERIAL PRIMARY KEY NOT NULL,

    /** The organism from which a structure is identified. 
    *   Foreign key to taxonomy table. 
    */
    taxonomy_id                         INT NOT NULL
                                        REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,

    /** A taxonomic identifier below the level of organism, and orthogonal 
    *   to taxonomy_id, eg: brain, lung, alveolus, erythrocyte.  
    */
    tissue_taxonomy_id                  INT NOT NULL
                                        REFERENCES core.tissue_taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
            
    /** The contributor of this context */
--     contributor_id                      INT NOT NULL
--                                         REFERENCES core.contributor
--                                         ON UPDATE CASCADE ON DELETE CASCADE
--                                         DEFERRABLE INITIALLY DEFERRED,
                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
    
    /** User-specific comments relevant to this sample/biological_context. */
--     comments                            TEXT DEFAULT NULL
)
;


CREATE TABLE core.biological_context_contributor (
   /** Auto-incrementing primary key.  */
    biological_context_contributor_id               SERIAL PRIMARY KEY NOT NULL,

    biological_context_id   INT NOT NULL
			     REFERENCES core.biological_context(biological_context_id)
                             ON UPDATE CASCADE ON DELETE CASCADE
                             DEFERRABLE INITIALLY DEFERRED,

     /** The contributor of this context */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, 

    /** User-specific comments relevant to this sample/biological_context. */
    comments                            TEXT DEFAULT NULL,

    UNIQUE(biological_context_id, contributor_id)
);


/*  table perturbation  *//***************************************************** 
*
*   Reference table encapsulating all non-disease perturbations of biological
*   contexts. It includes all chemical perturbations of natural state, 
*   such as treatment with antibiotics, or high salt conditions.
*
*   Depending on how much information we store about the exact detail of the 
*   chemical treatment, this table could get very complex. We should probably 
*   at least store references to pertinent chemical compounds used.
*
*   This table could use some extra information to further characterise  
*   perturbations more comprehensively. 
*/
CREATE TABLE core.perturbation 
(
    /** Auto-incrementing primary key  */
    perturbation_id                      SERIAL PRIMARY KEY NOT NULL,

    /** Parent perturbation id */
    parent_perturbation_id               INT 
                                         REFERENCES core.perturbation
                                         ON UPDATE CASCADE ON DELETE CASCADE
                                         DEFERRABLE INITIALLY DEFERRED,

    /** Name/summary of this perturbation.  */
    perturbation_name                    VARCHAR(256) NOT NULL,
    
    /** MeSH id. This is the value of the MeSH 'MN' field.  */
    mesh_id                             VARCHAR(64) NOT NULL
                                        CONSTRAINT check_notnullstring_mesh_id
                                        CHECK( mesh_id <> '' ),
                                        
    /** MeSH description. This is the value of the MeSH 'MS' field. */
    description                         TEXT DEFAULT NULL,     
       
    /** The date on which this entry was last modified (most likely by MeSH). */
    date_last_modified                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP    
)
;


/*  disease  *//***************************************************************
*
*   Reference table of diseases, compiled from the NCBI MeSH disease vocabulary
*   (MeSH category C, see http://www.nlm.nih.gov/mesh/).
*/
CREATE TABLE core.disease 
(
    /** Auto-incrementing primary key  */
    disease_id                          SERIAL PRIMARY KEY NOT NULL,

    /** Parent disease id */
    parent_disease_id                    INT 
                                         REFERENCES core.disease
                                         ON UPDATE CASCADE ON DELETE CASCADE
                                         DEFERRABLE INITIALLY DEFERRED,

    /** Canonical disease name  */    
    disease_name                        VARCHAR(128) NOT NULL,
    
    /** MESH disease id */
    mesh_id                             VARCHAR(64) NOT NULL
                                        CONSTRAINT check_notnullstring_mesh_id
                                        CHECK( mesh_id <> '' ),
   
    /** MeSH description. This is the value of the MeSH 'MS' field. */
    description                         TEXT DEFAULT NULL,     
       
    /** The date on which this entry was last modified (most likely by MeSH). */
    date_last_modified                  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP    
)
;


/*  disease_synonym  *//*******************************************************
*
*   Disease synonyms for the reference table of diseases in the disease table.
*/
CREATE TABLE core.disease_synonym 
(
    /** Auto-incrementing primary key  */
    disease_synonym_id                  SERIAL PRIMARY KEY NOT NULL,

    /** The disease for which this is a synonym  */    
    disease_id                          INT NOT NULL 
                                        REFERENCES core.disease
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Synonym for this disease  */
    synonym                             VARCHAR(64) UNIQUE NOT NULL
)
;



/*  table glycoconjugate  *//************************************************** 
*
*   Table for storing references to glycoconjugates. 
*
*   For the purposes of the DB schema, "glycoconjugate" may be thought of as a
*   base type of all specific glycoconjugate types. 
*/
CREATE TABLE core.glycoconjugate 
(
    /** Auto-incrementing primary key.  */
    glycoconjugate_id                   SERIAL PRIMARY KEY NOT NULL,
    
    /** Type of glycoconjugate (enumeration). This is not an exhaustive list. */
    glycoconjugate_type                 VARCHAR(32) NOT NULL DEFAULT 'glycoprotein'
                                        CHECK( glycoconjugate_type 
                                            IN ( 'glycoprotein', 'glycolipid', 'gpi-anchor') ),
                                        
    /** Table in which reference information for this glyconjugate may be found. */
    glycoconjugate_table                VARCHAR(32) NOT NULL DEFAULT 'glycoprotein_attachment',
    
    /** Key corresponding to the correct row in #glycoconjugate_table# */
    glycoconjugate_table_id             INT NOT NULL
)
;




/*  table glycoprotein  *//****************************************************
*
*   Table for storing information about glycoproteins. 
*/
CREATE TABLE core.glycoprotein
(
    /** Auto-incrementing primary key. */
    glycoprotein_id                     SERIAL PRIMARY KEY NOT NULL,
    
    /** Uniprot accession id. "Foreign key" to Uniprot. Can be NULL,
    *   meaning that the protein is not found in uniprot. */
    uniprot_id                          VARCHAR(32) DEFAULT NULL,
                               
    /** Name of glycoprotein sequence variant, if applicable. */                                 
    variant                             VARCHAR(64) DEFAULT NULL,
    
   /** Canonical glycoprotein name, cached from uniprot. */
   glycoprotein_name                    VARCHAR(256) NOT NULL,
   
   /** Full protein or peptide sequence. only needed if the submitted sequence
   *    differs from the uniprot sequence. */
   glycoprotein_sequence                TEXT DEFAULT NULL,
   
   /** If only a *portion* of a full sequence is expressed, then this value 
   *   indicates the offset into the full sequence as reported by uniprot.  */
   sequence_offset                      INT DEFAULT NULL,
   
   /** This sequence's native species. NULL means that this sequence is artificial,
   *    and that its sequence should accordingly be stored into the glycoprotein_sequence 
   *    field. */
   native_species                       INT REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
   
   /** If expressed in a non-native species, this is the species in which this
   *   sequence was expressed. NULL means that this sequence is native. 
   *   Note that by this definition, native_species and recombinant species can 
   *   never be the same. */
   expressed_species                    INT REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED
                                        CONSTRAINT native_and_recombinant_species_cant_be_equal
                                        CHECK( native_species != expressed_species)
                                        
)
;



/*  table glycoprotein_attachment  *//*****************************************
*
*   Table for storing references to glycoproteins. 
*/
CREATE TABLE core.glycoprotein_attachment
(
    /** Primary key, generated from glycoconjugate table. This means that 
    *   these ids will not necessarily be contiguous. */
    glycoprotein_attachment_id          --SERIAL PRIMARY KEY NOT NULL,
                                        INT PRIMARY KEY 
                                        REFERENCES core.glycoconjugate( glycoconjugate_id )
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** The glycoprotein sequence */
    glycoprotein_id                     INT NOT NULL
                                        REFERENCES core.glycoprotein,
 
    /** Position of attached amino-acid in the sequence given by uniprot_id above.
    *   Note that this amino-acid must be the same as that given by aminoacid_attached. */
    aminoacid_position                  INT NOT NULL,
        
    /** Classification of attachement type, eg: O-linked, N-linked, C-linked */
    attachment_type                     VARCHAR(32), 
    
    /** 1-letter abbreviation of amino-acid to which this glycan is attached. */
    aminoacid_attached                  CHAR(1),
    
    UNIQUE( glycoprotein_id, aminoacid_position )    
)
;


/*  table technique  *//******************************************************* 
*
*   Experimental technique reference table.
*
*/
CREATE TABLE core.technique 
(
    /** Auto-incrementing primary key.  */
    technique_id                        SERIAL PRIMARY KEY NOT NULL,
    
    /** Short/abbreviated name of this technique, eg: ESI-MS.  */
    technique_abbrev                    VARCHAR(10) NOT NULL 
                                        CHECK( technique_abbrev <> '' ),

    /** Name of technique, eg: electrospray mass spectrometry */
    technique_name                      VARCHAR(50) NOT NULL
                                        CHECK( technique_name <> '' )
   
)
;



/*    table experiment  *//******************************************
*
*/
CREATE TABLE core.experiment
(
    /** Primary key */
    experiment_id                       SERIAL PRIMARY KEY NOT NULL,
    
    /** Link to the group/lab/remote installation that contributed this piece
    *   of evidence.  */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    /** The experiment name */
    experiment_name                     VARCHAR(128) NOT NULL 
                                        CHECK( experiment_name <> '' ),
    
    /** Additional user-specified comments relating to the objective/purpose 
    *   of this experiment.  */
    experiment_comments                 TEXT DEFAULT NULL,
    
    /** The date when this experiment was first created. */
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL 
                                        DEFAULT CURRENT_TIMESTAMP
)
;


/*  table evidence  *//******************************************************** 
*
*   This is the central lookup table for experimental data. One row in this table
*   refers to one result obtained from one technique (which may provide evidence
*   for multiple structures).
*/
CREATE TABLE core.evidence 
(
    /** Auto-incrementing primary key.  */
    evidence_id                         SERIAL PRIMARY KEY NOT NULL,

    /** The experiment during which this piece of evidence was acquired. */
    experiment_id                       INT
                                        REFERENCES core.experiment
                                        ON UPDATE CASCADE ON DELETE SET NULL
                                        DEFERRABLE INITIALLY DEFERRED,

    /** The technique/method that was used to obtain this piece of evidence.
    *   Foreign key to technique table.  */
    technique_id                        INT NOT NULL
                                        REFERENCES core.technique
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED, 
    
    /** Link to the group/lab/remote installation that contributed this piece
    *   of evidence.  */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** The evidence_id at the remote site.  */
    contributor_evidence_id             INT DEFAULT NULL,
                                        -- REFERENCES core.evidence
                                        -- ON UPDATE CASCADE ON DELETE RESTRICT
                                        -- DEFERRABLE INITIALLY DEFERRED,
    
    /** Type discriminator of the type of this Evidence, eg: ms, hplc, nmr. */
    evidence_type                       VARCHAR(8) NOT NULL,
                                        
    /** This is the date that the data was entered into the DB.  */
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL 
                                        DEFAULT CURRENT_TIMESTAMP
    

)
;


/*    table experiment_step  *//***************************************
*
*    This table's purpose is to record the sequence of techniques used 
*    to generate evidence, since not all techniques generate recordable
*    data (evidence).
*
*    This table comprises a recursive (tree) relationship between steps 
*    as it is conceivable that samples & the techniques applied to them
*    may diverge during an experimental workflow. 
*
*    Question: should we allow "free text" description of the techniques
*    used in experiment steps? The only alternative is to exhaustively
*    populate the technique table with as many techniques as are likely 
*    to be used by most people, and to provide the capability to add new
*    techniques on demand.
*/
CREATE TABLE core.experiment_step
(
    /** Primary key */
    experiment_step_id                  SERIAL PRIMARY KEY NOT NULL,
    
    /** Link to the experiment step that preceded this step. 
    *   Recursive secondary key to this table.
    */
    parent_experiment_step_id           INT 
                                        REFERENCES core.experiment_step
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Link to the experiment in which this experimental step was performed. */
    experiment_id                       INT
                                        REFERENCES core.experiment
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Link to any actual recorded _evidence_ (that is, data) arising
    *   from this experimental step. Not all steps will have associated 
    *   data (evidence); this is indicated by a NULL in this column. 
    *   Foreign key to evidence table. 
    */
    evidence_id                         INT 
                                        REFERENCES core.evidence
                                        ON UPDATE CASCADE ON DELETE SET NULL
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** The technique used in this experiment step. Foreign key to 
    *   the technique table.
    */
    technique_id                        INT NOT NULL
                                        REFERENCES core.technique
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
                                
    /** The usergroup that performed this experiment step. Foreign key 
    *   to the contributor table.
    */                    
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE SET NULL
                                        DEFERRABLE INITIALLY DEFERRED,                    

    /** This is the date that the data was entered into the DB.  */
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    /** This is the date the data was considered to have been collected. */
    date_obtained                       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        CONSTRAINT date_obtained_after_date_entered 
                                        CHECK( date_obtained <= date_entered ),
                                        
    /** XML column to store any/all additional experimental step parameters that are
    *   not able to be stored in the current schema but are deemed to be "worth" 
    *   storing. XML schema for validating/checking this XML will ideally be found
    *   in the 'parameter_schema' column of the technique table.
    */
    experiment_step_parameters          TEXT DEFAULT NULL,  
            
    /** Optional additional user-specified comments relating to the execution of this 
    *   experiment step.
    */                           
    experiment_step_comments            TEXT DEFAULT NULL
);


/*  table journal  *//********************************************************* 
*
*   Publication/reference journal catalog. 
*/
CREATE TABLE core.journal 
(
    /** Auto-incrementing primary key  */
    journal_id                          SERIAL PRIMARY KEY NOT NULL,

    /** Full title of journal.  */
    journal_title                       VARCHAR(128) NOT NULL UNIQUE
                                        CONSTRAINT journal_title_not_empty 
                                        CHECK( journal_title <> '' ),
    
    /** Abbreviated title of journal.  */
    journal_abbrev                      VARCHAR(64) NOT NULL
                                        CHECK( journal_abbrev <> '' )
)
;


/*  table reference  *//******************************************************* 
*
*   Publication/reference information. This includes: journal articles,
*   external database entries relevant to a Eurocarb entry, and/or an 
*   external website.
*
*   That is, in addition to containing numerous journal articles, this table
*   also contains rows for each structure contributed by Carbbank, as well
*   as a Reference for Carbbank itself.
*
*/
CREATE TABLE core.reference 
(
    /** Auto-incrementing primary key  */
    reference_id                        SERIAL PRIMARY KEY NOT NULL,

    /** The contributor who created "owns" this reference */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    
    /** Type of reference -- either a journal article, external database, or website. 
    Typical values: 'journal', 'database', 'website' */
    reference_type                      VARCHAR(8) NOT NULL
                                        DEFAULT 'journal',
 
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

    /** The ID of this reference as given by the external entity/organisation
    *   to which this reference is linked. */
    external_reference_id               VARCHAR(32) NOT NULL,
 
    /** The canonical name of the external reference source, eg: for journal 
    *   references this would usually be 'pubmed', for a carbbank reference
    *   'carbbank', for a KEGG reference 'kegg', etc.
    */
    external_reference_name             VARCHAR(32) NOT NULL,
                                        
    /** A direct URL for this reference, may be NULL if not relevant or 
    *   the URL can be entirely dynamically generated from the external_reference_id, 
    *   eg: Pubmed  */
    url                                 VARCHAR(256) DEFAULT NULL,
    
    /** Any extra information pertaining to this reference that may be useful. */
    reference_comments                  TEXT DEFAULT NULL
)
;
                                        
                                        
/*  table journal_reference  *//***********************************************
*
*   Publication/reference information specifically for journal articles.
*/
CREATE TABLE core.journal_reference 
(
    /** Auto-incrementing primary key  */
    journal_reference_id                SERIAL PRIMARY KEY NOT NULL,

    /** Auto-incrementing primary key  */
    reference_id                        INT NOT NULL
                                        REFERENCES core.reference
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Scientific journal id; foreign key to journal table.  */
    journal_id                          INT NOT NULL
                                        REFERENCES core.journal
                                        ON UPDATE CASCADE ON DELETE RESTRICT
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Pubmed id, from http://www.ncbi.nlm.nih.gov/entrez/query.fcgi  
    *   This should be equal to core.reference.external_reference_id. */
    pubmed_id                           INT,
    
    /** Authorlist of publication.  */
    authors                             VARCHAR(256) NOT NULL,
    
    /** title of journal article */
    title                               VARCHAR(512),
                                        
    /** Year of publication.  */
    publication_year                    INT NOT NULL
                                        CHECK( publication_year > 1800 ),
                                        
    /** Volume number of publication.  */
    journal_volume                      INT,
    
    /** Starting page number of publication.  */
    journal_start_page                  INT,
    
    /** Ending page number of publication.  */
    journal_end_page                    INT
)
;


/*  table glycan_sequence_to_biological_context  *//*************************** 
*
*   Join table to map glycan sequence to the one or more biological contexts 
*   in which it has been found.
*/
CREATE TABLE core.glycan_sequence_to_biological_context 
(
    /** Auto-incrementing primary key.  */
    glycan_sequence_to_biological_context_id 
                                        SERIAL PRIMARY KEY NOT NULL,    

    /** A glycan sequence. Foreign key to glycan_sequence table.  */
    glycan_sequence_id                  INT NOT NULL
                                        REFERENCES core.glycan_sequence 
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    /** The biological context for the glycan structure given by #glycan_sequence_id#. 
    *   Foreign key to #biological_context# table.  
    */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                                                     
    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

    UNIQUE( glycan_sequence_id, biological_context_id ) 
);


/*  table evidence_to_biological_context  *//*************************** 
*
*   Join table to map evidence to the biological contexts from which
*   said evidence was found
*/
CREATE TABLE core.evidence_to_biological_context 
(
    /** Auto-incrementing primary key.  */
    evidence_to_biological_context_id   SERIAL PRIMARY KEY NOT NULL,    

    /** Foreign key to evidence table.  */
    evidence_id                         INT NOT NULL
                                        REFERENCES core.evidence 
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    /** The biological context(s) from which the Evidence was obtained.  
    */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                                                     
    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

    UNIQUE( evidence_id, biological_context_id ) 
);


/*  table biological_context_to_perturbation  *//*******************************
*
*   Join table for the many-many relationship between biological_context and perturbation.
*   Biological contexts can have zero or more associated perturbations. These perturbations 
*   may refer to multiple independent biological contexts.
*/
CREATE TABLE core.biological_context_to_perturbation
(
    /** Auto-incrementing primary key  */
    biological_context_to_perturbation_id 
                                        SERIAL PRIMARY KEY NOT NULL,

    /** Foreign key to biological context table.  */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    
    /** Link to all disease-related and chemically-induced perturbations associated
    *   with this biological context.  */
    perturbation_id                     INT NOT NULL
                                        REFERENCES core.perturbation
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                        
   UNIQUE( biological_context_id, perturbation_id ) 
)
;



/*  table biological_context_to_disease  *//***********************************
*
*   Join table for the many-many relationship between biological_context and disease.
*   Biological contexts can have zero or more associated diseases. Diseases in turn may
*   be found in multiple independent biological contexts.
*/
CREATE TABLE core.biological_context_to_disease 
(
    /** Auto-incrementing primary key  */
    biological_context_to_disease_id    SERIAL PRIMARY KEY NOT NULL,
    
    /** Foreign key to biological_context table.  */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to disease table.  */
    disease_id                          INT NOT NULL
                                        REFERENCES core.disease
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

   UNIQUE( biological_context_id, disease_id ) 
)
;

--/*  table evidence_to_biological_context  *//**********************************
--*
--*   Map of evidence to the one or more biological contexts in which
--*   it has been found.
--*/
--CREATE TABLE core.evidence_to_biological_context 
--(
--    /** Auto-incrementing primary key.  */
--    evidence_to_biological_context      SERIAL PRIMARY KEY NOT NULL,    
--
--    /** The biological context for the glycan structure given by #glycan_sequence_id#. 
--    *   Foreign key to biological context table.  */
--    biological_context_id               INT NOT NULL
--                                        REFERENCES core.biological_context
--                                        ON UPDATE CASCADE ON DELETE CASCADE,
--
--    /** Link to the one or more pieces of experimental _evidence_ relating to
--    *   the given _biological context_. Foreign key to evidence table.  */
--    evidence_id                         INT NOT NULL
--                                        REFERENCES core.evidence
--                                        ON UPDATE CASCADE ON DELETE CASCADE
--);



/*  table glycan_sequence_to_evidence  *//*************************************
*
*   Join table to provide the mapping between glycan structures and the 
*   their associated experimental evidence.  
*/
CREATE TABLE core.glycan_sequence_to_evidence 
(
    /** Auto-incrementing primary key.  */
    glycan_sequence_to_evidence_id      SERIAL PRIMARY KEY NOT NULL,
        
    /** Foreign key to glycan_sequence table.  */
    glycan_sequence_id                  INT NOT NULL
                                        REFERENCES core.glycan_sequence
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED, 
    
    /** Foreign key to evidence table.  */
    evidence_id                         INT NOT NULL
                                        REFERENCES core.evidence
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Heterogeneity quantitation.  */
    quantitation_by_percent             FLOAT
                                        CHECK( quantitation_by_percent >= 0 
                                           AND quantitation_by_percent <= 100 ),
                                           
    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

   UNIQUE( glycan_sequence_id, evidence_id ) 
)
;



/*  table evidence_to_reference  *//******************************************* 
*
*   Maps the many-many relationship between the evidence and reference tables.
*   One piece of evidence may refer to several publications; one publication
*   may (will often) provide multiple pieces of evidence.
*/
CREATE TABLE core.evidence_to_reference 
(
    /** Auto-incrementing primary key  */
    evidence_to_reference_id            SERIAL PRIMARY KEY NOT NULL,
    
    /** Foreign key to evidence table.  */
    evidence_id                         INT NOT NULL
                                        REFERENCES core.evidence
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to reference table.  */
    reference_id                        INT NOT NULL
                                        REFERENCES core.reference
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                        
    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

   UNIQUE( evidence_id, reference_id ) 
)
;



/*  table biological_context_to_glycoconjugate  *//****************************
*
*   Maps the many-many relationship between the evidence and reference tables.
*   One piece of evidence may refer to several publications; one publication
*   may (will often) provide multiple pieces of evidence.
*/
CREATE TABLE core.biological_context_to_glycoconjugate 
(
    /** Auto-incrementing primary key  */
    biological_context_to_glycoconjugate_id            
                                        SERIAL PRIMARY KEY NOT NULL,
    
    /** Foreign key to biological_context table.  */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to glycoconjugate table.  */
    glycoconjugate_id                   INT NOT NULL
                                        REFERENCES core.glycoconjugate
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                        
   UNIQUE( biological_context_id, glycoconjugate_id ) 
)
;


/*  table glycan_sequence_to_reference  *//************************************
*
*   Maps the many-many relationship between the glycan_sequence and reference tables.
*   One sequence may be reported in several publications; likewise, one publication
*   may (will often) report multiple sequences.
*/
CREATE TABLE core.glycan_sequence_to_reference 
(
    /** Auto-incrementing primary key  */
    glycan_sequence_to_reference_id     SERIAL PRIMARY KEY NOT NULL,
    
    /** Foreign key to glycan_sequence table.  */
    glycan_sequence_id                  INT NOT NULL
                                        REFERENCES core.glycan_sequence
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to reference table.  */
    reference_id                        INT NOT NULL
                                        REFERENCES core.reference
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

   UNIQUE( glycan_sequence_id, reference_id ) 
)
;


/*  table biological_context_to_experiment  *//************************************
*
*/
CREATE TABLE core.biological_context_to_experiment 
(
    /** Auto-incrementing primary key  */
    biological_context_to_experiment_id SERIAL PRIMARY KEY NOT NULL,
    
    /** Foreign key to biological_context table.  */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to experiment table.  */
    experiment_id                       INT NOT NULL
                                        REFERENCES core.experiment
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,

    /** The contributor who created this link */
    contributor_id                      INT NOT NULL
                                        REFERENCES core.contributor
                                        ON UPDATE CASCADE ON DELETE CASCADE,

    /** Date this link was entered into the DB. */                                        
    date_entered                        TIMESTAMP WITH TIME ZONE NOT NULL
                                        DEFAULT CURRENT_TIMESTAMP,

   UNIQUE( biological_context_id, experiment_id ) 
)
;




/*  table tissue_taxonomy_relations  *//***************************************
*
*   Implements the nested set algorithm for performing parent/child queries
*   on the tissue_taxonomy table.
*
*   See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295
*/
CREATE TABLE core.tissue_taxonomy_relations 
(
    /** Foreign key to tissue_taxonomy table  */
    tissue_taxonomy_id                  INT PRIMARY KEY NOT NULL
                                        REFERENCES core.tissue_taxonomy
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    
    /** Lower (left) index for this node's position in the tree.  */
    left_index                          INT NOT NULL UNIQUE
                                        CHECK( left_index > 0 ), 
    
    /** Upper (right) index for this node's position in the tree.  */
    right_index                         INT NOT NULL UNIQUE
                                        CHECK( right_index > left_index ) 
    
)
;


/*  table taxonomy_relations  *//**********************************************
*
*   Implements the nested set algorithm for performing parent/child queries
*   on the taxonomy table.
*
*   See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295
*/
CREATE TABLE core.taxonomy_relations 
(
    /** Foreign key to taxonomy table  */
    taxonomy_id                         INT PRIMARY KEY NOT NULL
                                        REFERENCES core.taxonomy
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Lower (left) index for this node's position in the tree.  */
    left_index                          INT NOT NULL UNIQUE
                                        CHECK( left_index > 0 ), 
    
    /** Upper (right) index for this node's position in the tree.  */
    right_index                         INT NOT NULL UNIQUE
                                        CHECK( right_index > left_index ) 
    
)
;


/** 
*   Convenience view that links taxonomy_id to the full list of sub-taxonomies
*   of that taxonomy_id (recursively all the way to the leaves).
*/
CREATE VIEW core.taxonomy_subtype AS 
   SELECT node.taxonomy_id, nodetax.taxon, sub.taxonomy_id AS sub_taxonomy_id
   FROM core.taxonomy_relations sub
   JOIN core.taxonomy_relations node ON sub.left_index >= node.left_index AND sub.left_index <= node.right_index
   JOIN core.taxonomy nodetax ON nodetax.taxonomy_id = node.taxonomy_id
;

   
/*  table disease_relations  *//***************************************
*
*   Implements the nested set algorithm for performing parent/child queries
*   on the disease table.
*
*   See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295
*/
CREATE TABLE core.disease_relations 
(
    /** Foreign key to disease table  */
    disease_id                          INT PRIMARY KEY NOT NULL
                                        REFERENCES core.disease
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Lower (left) index for this node's position in the tree.  */
    left_index                          INT NOT NULL UNIQUE
                                        CHECK( left_index > 0 ), 
    
    /** Upper (right) index for this node's position in the tree.  */
    right_index                         INT NOT NULL UNIQUE
                                        CHECK( right_index > left_index ) 
    
)
;


/*  table perturbation_relations  *//***************************************
*
*   Implements the nested set algorithm for performing parent/child queries
*   on the perturbation table.
*
*   See also: http://www.intelligententerprise.com/001020/celko.jhtml?_requestid=1266295
*/
CREATE TABLE core.perturbation_relations 
(
    /** Foreign key to perturbation table  */
    perturbation_id                     INT PRIMARY KEY NOT NULL
                                        REFERENCES core.perturbation
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Lower (left) index for this node's position in the tree.  */
    left_index                          INT NOT NULL UNIQUE
                                        CHECK( left_index > 0 ), 
    
    /** Upper (right) index for this node's position in the tree.  */
    right_index                         INT NOT NULL UNIQUE
                                        CHECK( right_index > left_index ) 
    
)
;




/*  table glycoprotein_association  *//***************************************
*
*   Captures the association between a specific glycan sequence and a specific
*   glycoprotein at an individual amino-acid level.
*/
CREATE TABLE core.glycoprotein_association 
(
    /** Primary key  */
    glycoprotein_association_id         SERIAL PRIMARY KEY NOT NULL,

    /** Foreign key to glycan_sequence table  */
    glycan_sequence_id                  INT NOT NULL
                                        REFERENCES core.glycan_sequence
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
    
    /** Foreign key to glycoconjugate table  */
    glycoconjugate_id                   INT NOT NULL
                                        REFERENCES core.glycoconjugate
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                        
    /** Foreign key to biological context table  */
    biological_context_id               INT NOT NULL
                                        REFERENCES core.biological_context
                                        ON UPDATE CASCADE ON DELETE CASCADE
                                        DEFERRABLE INITIALLY DEFERRED,
                                        
    /** The identity of the amino-acid, 3 letter code */                                        
    amino_acid                          VARCHAR(6),                                    
                                        
    /** Amino-acid position in the glycoprotein sequence. */
    amino_acid_position                 INT,
    
    /** Proportion of this amino-acid that is glycosylated in this biological context. 
    *   1.0 means this site is 100% glycosylated with the given glycan; 0.1 means
    *   that 10% of the glycoprotein population is glycosylated with this glycan.
    */
    occupancy                           FLOAT DEFAULT 1.0
                                        CHECK( occupancy > 0.0 )
)
;




