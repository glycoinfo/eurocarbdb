/**
* Create a database schema for the resources db and generate the tables and views.
* Last update: 2007/12/13
*/


/* unkomment the following line to re-create a present resources_db scheme: */
--DROP SCHEMA resources_db CASCADE;

/**
* Create the schema:
*/
CREATE SCHEMA resources_db;
  

/* **************************************************************/
/* ** Element tables: *******************************************/
/* **************************************************************/

/** 
* Table: resources_db.periodic
* Stores element data 
*/
CREATE TABLE resources_db.periodic
(
	symbol VARCHAR(2) PRIMARY KEY NOT NULL,
	periodic_id INT UNIQUE NOT NULL,
	element_name VARCHAR(70),
	avg_mass FLOAT,
	element_stable BOOL,
	density FLOAT,
	melting_point FLOAT,
	boiling_point FLOAT,
	ionisation_potential FLOAT,
	specific_heat FLOAT
);

/** 
* Table: resources_db.isotope
* Stores element isotope data 
*/
CREATE TABLE resources_db.isotope
(
	id SERIAL PRIMARY KEY NOT NULL,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	mass FLOAT,
	spin VARCHAR(10),
	neutrons INT,
	natural_abundance FLOAT,
	common_name VARCHAR(50),
	half_life VARCHAR(20),
	is_stable bool
);

/**
* Table: resources_db.elementary_particle
*/
CREATE TABLE resources_db.elementary_particle
(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(30) NOT NULL, 
	symbol VARCHAR(5),
	mass FLOAT,
	charge INT,
	spin FLOAT,
	p_half_life VARCHAR(20)
);



/* **************************************************************/
/* ** Residue tables: *******************************************/
/* **************************************************************/

/**
* Table: resources_db.glycan_namescheme
* Lists the names of valid glycan notation schemes (GLYCOCT, IUPAC, CARBBANK, GLYDE, BCSDB, KCF, ...)
*/
CREATE TABLE resources_db.glycan_namescheme
(
	name VARCHAR(30) PRIMARY KEY NOT NULL,
	description VARCHAR(255)
);

/**
* Fill table resources_db.glycan_namescheme:
*/
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('GLYCOCT', 'GlycoCT: EuroCarbDB internal notation');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('CARBBANK', 'CarbBank notation');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('GLYDE', 'Glyde xml notation');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('BCSDB', 'Bacterial Carbohydrate Structure DB');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('IUPAC', 'IUPAC notation');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('CFG', 'Consortium for Functional Glycomics: LinearCode(TM)');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('KCF', 'KEGG Chemical Format');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('GLYCOSCIENCES', 'glycosciences.de format, CarbBank like');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('SWEET2', 'notation that can be interpreted by the SweetII software');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('MONOSACCHARIDEDB', 'MonoSaccharideDB internal format, GlycoCT based');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('GWB', 'notation that is used internally in the GlycoWorkbench software');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('PDB', 'Protein Data Bank residue names');
INSERT INTO resources_db.glycan_namescheme (name, description) VALUES('CCPN', 'CCPN ChemComp names');

/**
* Table: resources_db.basetype
* Stores monosaccharide base types
*/
CREATE TABLE resources_db.basetype
(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(70) UNIQUE NOT NULL,
	size INT DEFAULT 0,
	superclass VARCHAR(3) NOT NULL,
	stereocode VARCHAR(20) NOT NULL,
	ring_start INT NOT NULL DEFAULT 0,
	ring_end INT NOT NULL DEFAULT 0,
	anomer VARCHAR(1),
	abs_configuration VARCHAR(1),
	mono_mass FLOAT,
	avg_mass FLOAT,
	inchi VARCHAR(255),
	smiles VARCHAR(255),
	molfile TEXT,
	is_superclass BOOL NOT NULL DEFAULT FALSE,
	comments VARCHAR(255)
);

/** 
* Table: resources_db.substituent
* Stores valid substituents
* The content of this table has to be manually curated.
*/
CREATE TABLE resources_db.substituent
(
	name VARCHAR(50) PRIMARY KEY NOT NULL,
	min_valence INT NOT NULL DEFAULT 1,
	max_valence INT NOT NULL DEFAULT 1,
	mono_mass FLOAT,
	avg_mass FLOAT,
	inchi VARCHAR(255),
	smiles VARCHAR(255),
	formula VARCHAR(100),
	comments VARCHAR(255)
);

/**
* Table: resources_db.linkage_type
* Lists the valid linkage types by which a substituent may be linked to a monosaccharide
*/
CREATE TABLE resources_db.linkage_type
(
	name VARCHAR(30) PRIMARY KEY NOT NULL,
	description VARCHAR(255)
);

/**
* Fill table resources_db.linkage_type:
*/
INSERT INTO resources_db.linkage_type (name, description) VALUES('H_AT_OH','O-linked, linked component replaces H atom of OH group');
INSERT INTO resources_db.linkage_type (name, description) VALUES('DEOXY','deoxy, linked component replaces entire OH group');
INSERT INTO resources_db.linkage_type (name, description) VALUES('H_LOSE','C-linked, linked component replaces backbone H atom');
INSERT INTO resources_db.linkage_type (name, description) VALUES('R_CONFIG','C-linked, linked component replaces backbone H atom of an achiral center resulting in pro-r configuration');
INSERT INTO resources_db.linkage_type (name, description) VALUES('S_CONFIG','C-linked, linked component replaces backbone H atom of an achiral center resulting in pro-s configuration');

/**
* Table: resources_db.monosaccharide
* Stores monosaccharides, which consist of a basetype and (potentially) substituents.
* If substituents are present, the information from the resources_db.substituent table is linked via the resources_db.monosaccharide_substituent table.
*/
CREATE TABLE resources_db.monosaccharide
(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(255) UNIQUE NOT NULL,
	basetype_id INT NOT NULL REFERENCES resources_db.basetype,
	mono_mass FLOAT,
	avg_mass FLOAT,
	inchi VARCHAR(255),
	smiles VARCHAR(255),
	molfile TEXT,
	comments VARCHAR(255)
);

/**
* Table: resources_db.substitution
* Links the resources_db.monosaccharide and the resources_db.substituent tables, with information on linkage types and positions
*/
CREATE TABLE resources_db.substitution
(
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INT NOT NULL REFERENCES resources_db.monosaccharide,
	substituent_name VARCHAR(50) NOT NULL REFERENCES resources_db.substituent,
	ms_position1 INT NOT NULL,
	ms_position2 INT,
	subst_position1 INT NOT NULL,
	subst_position2 INT,
	linkagetype1 VARCHAR(30) NOT NULL REFERENCES resources_db.linkage_type,
	linkagetype2 VARCHAR(30) REFERENCES resources_db.linkage_type,
	subst_index INT
);

/**
* Table: resources_db.monosaccharide_mass_annotation
*/
CREATE TABLE resources_db.monosaccharide_mass_annotation
(
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INT NOT NULL REFERENCES resources_db.monosaccharide,
	mono_mass FLOAT,
	avg_mass FLOAT,
	connectivity INT,
	permethylation FLOAT,
	peracetylation FLOAT
);

/**
* Table: resources_db.amino_acid
* Stores information on amino acids.
*/
CREATE TABLE resources_db.amino_acid
(
	abbr_3 VARCHAR(3) PRIMARY KEY NOT NULL,
	name VARCHAR(20) NOT NULL,
	abbr_1 VARCHAR(1),
	mono_mass FLOAT,
	avg_mass FLOAT,
	mono_incr FLOAT,
	avg_incr FLOAT
);

/**
* Table: resources_db.aglycon
* Stores information on small molecules that can be present as aglycon in glycoconjugate molecules
*/
CREATE TABLE resources_db.aglycon
(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(45) NOT NULL,
	full_name VARCHAR(255),
	aglycon_class VARCHAR(40)
		CHECK(aglycon_class IN ('amino acid', 'lipid', 'nucleotide', 'small molecule', 'other')),
	mono_mass FLOAT,
	avg_mass FLOAT,
	molfile VARCHAR(1000),
	smiles VARCHAR(255),
	comments VARCHAR(255),
	inchi VARCHAR(255),
	formula VARCHAR(100)
);

/**
* Table: resources_db.core_modification_type
* Lists the valid monosaccharide core modifications
*/
CREATE TABLE resources_db.core_modification_type (
	name VARCHAR(20) PRIMARY KEY NOT NULL,
	valence INT,
	description VARCHAR(255)
);

/**
* Fill table resources_db.core_modification_type:
*/
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('deoxy', 1, 'deoxygenation');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('en', 2, 'double bond');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('enx', 2, 'double bond with unknown deoxygenation pattern');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('anhydro', 2, 'anhydro');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('acid', 1, 'carboxyl group');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('ulo', 1, 'carbonyl group');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('sp2', 1, 'sp2 hybride');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('sp', 1, 'sp hybride');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('aldi', 1, 'alditol');
INSERT INTO resources_db.core_modification_type (name, valence, description) VALUES('epoxy', 2, 'epoxide');

/**
* Table: resources_db.basetype_has_core_modification
* Links the resources_db.basetype and the resources_db.core_modification_type tables
*/
CREATE TABLE resources_db.basetype_has_core_modification (
	id SERIAL PRIMARY KEY NOT NULL,
	core_modification_name VARCHAR(20) NOT NULL REFERENCES resources_db.core_modification_type,
	basetype_id INT NOT NULL REFERENCES resources_db.basetype,
	position1 INT NOT NULL,
	position2 INT,
	mod_index INT
);


/**
* Table: resources_db.monosaccharide_linking_position
* Stores the positions at which other monosaccharides (or other sorts of residues) can be linked to the referenced monosaccharide
*/
CREATE TABLE resources_db.monosaccharide_linking_position (
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INTEGER NOT NULL REFERENCES resources_db.monosaccharide,
	position INT NOT NULL,
	is_anomeric BOOL DEFAULT FALSE,
	is_via_substitution INTEGER REFERENCES resources_db.substitution,
	commentary VARCHAR(500),
	position_index INTEGER
);

/* **************************************************************/
/* ** Synonyms tables: ******************************************/
/* **************************************************************/

/**
* Table: resources_db.monosaccharide_synonym
* For each monosaccharide, only one primary synonym is stored per glycan namescheme (the one used for output in that notation theme), secondary synonyms can be stored here as well if appropriate
*/
CREATE TABLE resources_db.monosaccharide_synonym
(
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INT NOT NULL REFERENCES resources_db.monosaccharide,
	glycan_namescheme VARCHAR(30) NOT NULL REFERENCES resources_db.glycan_namescheme,
	name VARCHAR(50) NOT NULL,
	is_primary bool NOT NULL,
	is_trivialname bool NOT NULL,
	commentary VARCHAR(1000)
);

CREATE TABLE resources_db.ms_synonym_external_substituent (
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_synonym_id INTEGER NOT NULL REFERENCES resources_db.monosaccharide_synonym,
	subst_index INT NOT NULL,
	name VARCHAR(50) NOT NULL,
	linkage_position1 INT NOT NULL,
	linkage_position2 INT,
	linkage_type1 VARCHAR NOT NULL REFERENCES resources_db.linkage_type,
	linkage_type2 VARCHAR REFERENCES resources_db.linkage_type,
	subst_position1 INT,
	subst_position2 INT
);

/**
* Table: resources_db.substituent_synonym
*/
CREATE TABLE resources_db.substituent_synonym
(
	id SERIAL PRIMARY KEY NOT NULL,
	residue_included_name VARCHAR(50),
	separate_display_name VARCHAR(50),
	glycan_namescheme VARCHAR(30) NOT NULL REFERENCES resources_db.glycan_namescheme,
	substituent_name VARCHAR(50) NOT NULL REFERENCES resources_db.substituent,
	linkage_type_name VARCHAR(30) REFERENCES resources_db.linkage_type,
	is_primary bool NOT NULL,
	commentary VARCHAR(1000)
	--,UNIQUE (residue_included_name, separate_display_name, glycan_namescheme)
);

/**
* Table: resources_db.aglycon_synonym
*/
CREATE TABLE resources_db.aglycon_synonym (
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(50) NOT NULL,
	glycan_namescheme VARCHAR(30) NOT NULL REFERENCES resources_db.glycan_namescheme,
	aglycon_id INT NOT NULL REFERENCES resources_db.aglycon,
	is_primary_name BOOL NOT NULL,
	commentary VARCHAR(1000),
	UNIQUE (name, glycan_namescheme)
);



/* ***************************************************************/
/* ** Tables / views for non-textual representation of residues: */
/* ***************************************************************/

/**
* Table: resources_db.representation_format
* Lists file formats, in which the data in the representation table can be present
* 'format_type' may be 
*   -'graphic' (graphical representation of the residue, e.g. GIF)
*   -'3d structure' (3D representation of the residue, e.g. PDB)
*/
CREATE TABLE resources_db.representation_format (
	name VARCHAR(20) PRIMARY KEY NOT NULL,
	format_type VARCHAR(50) NOT NULL
		CHECK(format_type IN ('graphic', '3d structure')),
	scalable BOOL,
	description VARCHAR(255)
);

/**
* Fill table resources_db.representation_format:
*/
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('gif', 'graphic', false, 'gif image format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('png', 'graphic', false, 'png image format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('jpg', 'graphic', false, 'jpg image format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('svg', 'graphic', true, 'scalable vector graphics format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('pdb', '3d structure', false, 'pdb structure file format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('mol2', '3d structure', false, 'mol2 structure file format');
INSERT INTO resources_db.representation_format (name, format_type, scalable, description) 
	VALUES('chem_comp', '3d structure', false, 'CCPN ChemComp file format');

/**
* Table: resources_db.representation_type
* Lists the valid types for residue representation, like Haworth structure, CFG icons, Oxford icons, 3d coordinates,...
*/
CREATE TABLE resources_db.representation_type (
	name VARCHAR(20) PRIMARY KEY NOT NULL,
	description VARCHAR(255)
);

/**
* Fill table resources_db.representation_type:
*/
INSERT INTO resources_db.representation_type (name, description)
	VALUES('cfg_symbol', 'icon used in the sugar graphs of the CFG');
INSERT INTO resources_db.representation_type (name, description)
	VALUES('cfg_symbol_bw', 'icon used in the sugar graphs of the CFG (grayscale version)');
INSERT INTO resources_db.representation_type (name, description)
	VALUES('oxford_symbol', 'icon used in the sugar graphs of the Oxford encoding scheme');
INSERT INTO resources_db.representation_type (name, description)
	VALUES('haworth', 'Haworth formula');
INSERT INTO resources_db.representation_type (name, description)
	VALUES('coordinates', '3d structural coordinates');
	
/**
* Table: resources_db.monosaccharide_representation
* Stores non-textual representations of monosaccharides, like icons, graphics or 3d structures.
*
*/
CREATE TABLE resources_db.monosaccharide_representation (
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INT NOT NULL REFERENCES resources_db.monosaccharide,
	representation_type VARCHAR(20) NOT NULL REFERENCES resources_db.representation_type,
	representation_format VARCHAR(20) NOT NULL REFERENCES resources_db.representation_format,
	representation_data BYTEA,
	/* width and height are mainly for graphical representations, should be NULL in case of 3d structures: */
	width INT DEFAULT NULL,
	height INT DEFAULT NULL,
	locked bool NOT NULL DEFAULT false
);

/**
* Table: resources_db.substituent_representation
* Stores non-textual representations of substituents, like icons, graphics or 3d structures.
*
*/
CREATE TABLE resources_db.substituent_representation (
	id SERIAL PRIMARY KEY NOT NULL,
	substituent_name VARCHAR(50) NOT NULL REFERENCES resources_db.substituent,
	representation_type VARCHAR(20) NOT NULL REFERENCES resources_db.representation_type,
	representation_format VARCHAR(20) NOT NULL REFERENCES resources_db.representation_format,
	representation_data BYTEA,
	/* width and height are mainly for graphical representations, should be NULL in case of 3d structures: */
	width INT DEFAULT NULL,
	height INT DEFAULT NULL,
	locked bool NOT NULL DEFAULT false
);

/**
* Table: resources_db.aglycon_representation
* Stores non-textual representations of aglyca, like icons, graphics or 3d structures.
*
*/
CREATE TABLE resources_db.aglycon_representation (
	id SERIAL PRIMARY KEY NOT NULL,
	aglycon_id INT NOT NULL REFERENCES resources_db.aglycon,
	representation_type VARCHAR(20) NOT NULL REFERENCES resources_db.representation_type,
	representation_format VARCHAR(20) NOT NULL REFERENCES resources_db.representation_format,
	representation_data BYTEA,
	/* width and height are mainly for graphical representations, should be NULL in case of 3d structures: */
	width INT DEFAULT NULL,
	height INT DEFAULT NULL,
	locked bool NOT NULL DEFAULT false
);


/* **************************************************************/
/* ** Atom related tables / views: ******************************/
/* **************************************************************/

/**
* Table: resources_db.basetype_atom
* Stores the atoms that compose the basetype residues.
*/
CREATE TABLE resources_db.basetype_atom
(
	id SERIAL PRIMARY KEY NOT NULL,
	basetype_id INT NOT NULL REFERENCES resources_db.basetype,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	name VARCHAR(10),
	charge FLOAT
);

/**
* Table: resources_db.monosaccharide_atom
* Stores the atoms that compose the monosaccharide residues.
*/
CREATE TABLE resources_db.monosaccharide_atom
(
	id SERIAL PRIMARY KEY NOT NULL,
	monosaccharide_id INT NOT NULL REFERENCES resources_db.monosaccharide,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	name VARCHAR(10),
	charge FLOAT
);

/**
* Table: resources_db.substituent_atom
* Stores the atoms that compose the substituent residues.
*/
CREATE TABLE resources_db.substituent_atom
(
	id SERIAL PRIMARY KEY NOT NULL,
	substituent_name VARCHAR(50) NOT NULL REFERENCES resources_db.substituent,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	name VARCHAR(10),
	charge FLOAT
);

/**
* Table: resources_db.valid_substituentlinking_position
* Lists the positions and the corresponding atoms via which a substiuent may be linked to a basetype
*/
CREATE TABLE resources_db.valid_substituent_linking_position (
	id SERIAL PRIMARY KEY NOT NULL,
	substituent_name VARCHAR(50) NOT NULL REFERENCES resources_db.substituent,
	substituent_linking_atom_id INT NOT NULL REFERENCES resources_db.substituent_atom,
	substituent_replaced_atom_id INT REFERENCES resources_db.substituent_atom,
	bond_order FLOAT NOT NULL,
	position INT NOT NULL
);

/**
* Table: resources_db.aglycon_atom
* Stores the atoms that compose the aglycon residues.
*/
CREATE TABLE resources_db.aglycon_atom
(
	id SERIAL PRIMARY KEY NOT NULL,
	aglycon_id INT NOT NULL REFERENCES resources_db.aglycon,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	name VARCHAR(10),
	charge FLOAT
);

/**
* Table: resources_db.valid_aglycon_linking_position
* Lists the positions and the corresponding atoms via which an aglycon may be linked to the reducing end of a glycan
*/
CREATE TABLE resources_db.valid_aglycon_linking_position (
	id SERIAL PRIMARY KEY NOT NULL,
	aglycon_id INT NOT NULL REFERENCES resources_db.aglycon,
	aglycon_linking_atom_id INT NOT NULL REFERENCES resources_db.aglycon_atom,
	aglycon_replaced_atom_id INT REFERENCES resources_db.aglycon_atom,
	bond_order FLOAT NOT NULL,
	position INT NOT NULL
);

/**
* Table: resources_db.amino_acid_atom
* Stores the atoms that compose the amino acid residues.
*/
CREATE TABLE resources_db.amino_acid_atom
(
	id SERIAL PRIMARY KEY NOT NULL,
	amino_acid_name VARCHAR(3) NOT NULL REFERENCES resources_db.amino_acid,
	element VARCHAR(2) NOT NULL REFERENCES resources_db.periodic,
	name VARCHAR(10),
	charge FLOAT
);

/**
* Table: resources_db.basetype_atom_connection
* Stores connections between the basetype atoms
*/
CREATE TABLE resources_db.basetype_atom_connection
(
	id SERIAL PRIMARY KEY NOT NULL,
	from_atom INT NOT NULL REFERENCES resources_db.basetype_atom,
	to_atom INT NOT NULL REFERENCES resources_db.basetype_atom,
	bond_order FLOAT
);

/**
* Table: resources_db.monosaccharide_atom_connection
* Stores connections between the monosaccharide atoms
*/
CREATE TABLE resources_db.monosaccharide_atom_connection
(
	id SERIAL PRIMARY KEY NOT NULL,
	from_atom INT NOT NULL REFERENCES resources_db.monosaccharide_atom,
	to_atom INT NOT NULL REFERENCES resources_db.monosaccharide_atom,
	bond_order FLOAT
);

/**
* Table: resources_db.substituent_atom_connection
* Stores connections between the substituent atoms
*/
CREATE TABLE resources_db.substituent_atom_connection
(
	id SERIAL PRIMARY KEY NOT NULL,
	from_atom INT NOT NULL REFERENCES resources_db.substituent_atom,
	to_atom INT NOT NULL REFERENCES resources_db.substituent_atom,
	bond_order FLOAT
);

/**
* Table: resources_db.aglycon_atom_connection
* Stores connections between the monosaccharide atoms
*/
CREATE TABLE resources_db.aglycon_atom_connection
(
	id SERIAL PRIMARY KEY NOT NULL,
	from_atom INT NOT NULL REFERENCES resources_db.aglycon_atom,
	to_atom INT NOT NULL REFERENCES resources_db.aglycon_atom,
	bond_order FLOAT
);

/**
* Table: resources_db.amino_acid_atom_connection
* Stores connections between the amino acid atoms
*/
CREATE TABLE resources_db.amino_acid_atom_connection
(
	id SERIAL PRIMARY KEY NOT NULL,
	from_atom INT NOT NULL REFERENCES resources_db.amino_acid_atom,
	to_atom INT NOT NULL REFERENCES resources_db.amino_acid_atom,
	bond_order FLOAT
);

/**
* View: resources_db.basetype_composition
* Displays the element compositions of the monosaccharide base types based on the data of the basetype_atoms table
*/  
CREATE OR REPLACE VIEW resources_db.basetype_composition AS 
 SELECT basetype_atom.basetype_id, basetype_atom.element, COUNT(*) AS quantity
   FROM resources_db.basetype_atom
  GROUP BY basetype_atom.basetype_id, basetype_atom.element
  ORDER BY basetype_atom.basetype_id;
  
/**
* View: resources_db.monosaccharide_composition
* Displays the element compositions of the monosaccharides based on the data of the monosaccharide_atoms table
*/  
CREATE OR REPLACE VIEW resources_db.monosaccharide_composition AS 
 SELECT monosaccharide_atom.monosaccharide_id, monosaccharide_atom.element, COUNT(*) AS quantity
   FROM resources_db.monosaccharide_atom
  GROUP BY monosaccharide_atom.monosaccharide_id, monosaccharide_atom.element
  ORDER BY monosaccharide_atom.monosaccharide_id;

/**
* View: resources_db.substituent_composition
* Displays the element compositions of the substituents based on the data of the substituent_atoms table
*/  
CREATE OR REPLACE VIEW resources_db.substituent_composition AS 
 SELECT substituent_atom.substituent_name, substituent_atom.element, COUNT(*) AS quantity
   FROM resources_db.substituent_atom
  GROUP BY substituent_atom.substituent_name, substituent_atom.element
  ORDER BY substituent_atom.substituent_name;

/**
* View: resources_db.aglycon_composition
* Displays the element compositions of the aglyca based on the data of the aglycon_atoms table
*/  
CREATE OR REPLACE VIEW resources_db.aglycon_composition AS 
 SELECT aglycon_atom.aglycon_id, aglycon_atom.element, COUNT(*) AS quantity
   FROM resources_db.aglycon_atom
  GROUP BY aglycon_atom.aglycon_id, aglycon_atom.element
  ORDER BY aglycon_atom.aglycon_id;

/**
* View: resources_db.amino_acid_composition
* Displays the element compositions of the aglyca based on the data of the aglycon_atoms table
*/  
CREATE OR REPLACE VIEW resources_db.amino_acid_composition AS 
 SELECT amino_acid_atom.amino_acid_name, amino_acid_atom.element, COUNT(*) AS quantity
   FROM resources_db.amino_acid_atom
  GROUP BY amino_acid_atom.amino_acid_name, amino_acid_atom.element
  ORDER BY amino_acid_atom.amino_acid_name;



/* **************************************************************/
/* ** Fragment tables: ******************************************/
/* **************************************************************/

/**
* Table: resources_db.fragment_superclass
* /
CREATE TABLE resources_db.fragment_superclass
(
  id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(20),
  symbol VARCHAR(10),
  pos1 INT,
  pos2 INT,
  pos3 INT,
  pos4 INT,
  pos5 INT,
  pos6 INT,
  pos7 INT,
  pos8 INT,
  pos9 INT,
  anomeric_center INT
);*/


/**
* Table: resources_db.fragment
*/
CREATE TABLE resources_db.fragment
(
	id SERIAL PRIMARY KEY NOT NULL,
	--fragment_superclass_id INT REFERENCES resources_db.fragment_superclass,
	fragment_type VARCHAR(1),
	cleavage1 INT,
	cleavage2 INT,
	image BYTEA,
	substraction VARCHAR(3)
);

/**
* Table: resources_db.fragment_composition
*/
CREATE TABLE resources_db.fragment_composition
(
	id SERIAL PRIMARY KEY NOT NULL,
	fragment_id INT REFERENCES resources_db.fragment,
	periodic_symbol VARCHAR(2) REFERENCES resources_db.periodic,
	quantity INT
);

/**
* Table: resources_db.persubstitution
*/
CREATE TABLE resources_db.persubstitution
(
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(50) UNIQUE NOT NULL,
	symbol VARCHAR(10) UNIQUE NOT NULL
);

/**
* Table: resources_db.persubstitution_composition
*/
CREATE TABLE resources_db.persubstitution_composition
(
	id SERIAL PRIMARY KEY NOT NULL,
	persubstitution_id INT REFERENCES resources_db.persubstitution,
	periodic_symbol VARCHAR(2) REFERENCES resources_db.periodic,
	quantity INT
);

/**
* Table: resources_db.fragment_has_persubstitution
*/
CREATE TABLE resources_db.fragment_has_persubstitution
(
	id SERIAL PRIMARY KEY NOT NULL,
	fragment_id INT REFERENCES resources_db.fragment,
	persubstitution_id INT REFERENCES resources_db.persubstitution,
	quantity INT
);

/**
* Table: resources_db.fragment_mass
*/
CREATE TABLE resources_db.fragment_mass
(
	id SERIAL PRIMARY KEY NOT NULL,
	fragment_id INT REFERENCES resources_db.fragment,
	persubstitution_id INT REFERENCES resources_db.persubstitution,
	mono_mass FLOAT,
	avg_mass FLOAT
);

/**
* Table: resources_db.monosaccharide_fragment
*/
CREATE TABLE resources_db.monosaccharide_fragment
(
	id SERIAL PRIMARY KEY NOT NULL,
	fragment_id INT REFERENCES resources_db.fragment,
	monosaccharide_id INT REFERENCES resources_db.persubstitution
);
