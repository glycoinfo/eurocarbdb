
--drop schema seq cascade;

create schema seq;

-------------------  tables  -------------------


-- /**
-- static reference library of residues, 1 row per residue.

-- includes: monosaccharides, and substituents.

-- residue type inferred from contents of 'class' column.

-- this table is biased towards monsaccharides; this table may change in the future.
-- */
-- create table seq.residue 
-- (
--     /** auto-incrementing primary key */
--     residue_id              serial primary key,
    
--     /** canonical (short)name of this residue */
--     name                    varchar(32) unique not null,
    
--     /** single character representation of this residue */
--     abbrev                  char(1),
    
--     /** type of residue. this is just a basic descriptor of chemical class, for now. 
--         a = amino-acid,
--         c = chemical substituent,
--         m = std monosaccharide (single basetype), 
--         n = std monosac with substituents (still single basetype)
--         o = non-std monosac with multiple basetypes
--     */
--     residue_type            char(1), 
    
--     /** superclass of monosac, if residue is a monosac: Hex/HexNAc/dHex/NeuAc */
--     massclass               varchar(8),
    
--     /** monoisotopic residue mass */
--     residue_mass            numeric, 
    
--     /** average residue mass */
--     residue_avgmass         numeric, 
    
--     /** if monosaccharide: bitmask for axial/equatorial: 1 means axial */
--     terminii                bit varying,       
    
--     /** bitmask for hydroxyls that are initially free/connectable: 
--     1 means it's connectable. i.e. in Java: 
    
--         int position = ...;
--         if ( bitmask & (1 << position) ) 
--         {
--             //  position is available for connection
--         }
        
--     */
--     connectable             bit varying     
-- );


-- create table seq.residue_synonym
-- (
--     residue_synonym_id      serial primary key,
    
--     residue_id              int not null
--                             references seq.residue
--                             on update cascade on delete cascade,
  
--     synonym                 varchar(512) unique
-- );


/**
*   Table that specifies individual residues in structures; 1 row in this table
*   is equivalent to a single residue (monosaccharide, substituent or other chemical) 
*   in a single structure.
*/
create table seq.glycan_residue 
(
    /** auto-incrementing primary key */
    glycan_residue_id       serial primary key,
    
    /** fkey to glycan sequence containing this residue */
    glycan_sequence_id      int not null 
                            references core.glycan_sequence
                            on update cascade on delete cascade,
                            
    /** fkey to residue reference data */
    -- residue_id              int not null 
    --                         references seq.residue
    --                         on update cascade on delete cascade,
        
    /** local fkey to parent residue of this residue */
    parent_id               int default null 
                            references seq.glycan_residue
                            on update cascade on delete cascade,
    
    /** type of residue. this is just a basic descriptor of chemical class, for now. 
    *   m = monosaccharide, 
    *   s = substituent, eg: P or S
    *   o = other chemical substituent (residue_name = inchi),
    *   ? = unknown
    */
    residue_type            char(1) default 's', 
    
    residue_name            varchar(64) not null,
    
    basetype_id             int default null,
    
    stereochem_id           int default null,
    
    /** anomeric configuration of monosaccharide:
    *   a = alpha, 
    *   b = beta,
    *   ? = unknown,
    *   null = N/A (ie: is not a glycosidic linkage)  
    */                            
    anomer                  char default null,
    
    /** Ring conformation of monosaccharide (see also org.eurocarbdb.sugar.RingConformation): 
    *   p = pyranose, 
    *   f = furanose, 
    *   o = open-chain, 
    *   ? = unknown, 
    *   null = N/A 
    */
    conformation            char default null,
    
    /** Superclass size. */
    superclass              smallint default null,
    
    /** reducing terminal side, ie: the parent of this residue's linkage 
    *   position, or null if unknown.  */                            
    linkage_parent          smallint default null,
    
    /** non-reducing terminal side of this residue's linkage position    
    *   to its parent or null if unknown */                            
    linkage_child           smallint default null,
    
    /** left index for nested set algorithm. */
    left_index              int not null check( left_index > 0 ),

    /** right index for nested set algorithm. */
    right_index             int not null check( right_index > left_index ), -- for nested set
    
    --,
    --graph_depth int, -- index of this residue relative to root, root = 0
    
    /** 2 residues can't have same parent & same linkage. */
    -- unique( parent_id, linkage_parent ), 
    
    /** in nested sets, left & right index cannot be equal. */
    unique( glycan_sequence_id, left_index, right_index )
);


/**
view for composition by single monosaccharide
*/
create view seq.composition_v as 
    select  
        gr.glycan_sequence_id,   
        gr.residue_name,   
        gr.basetype_id,   
        count(gr.residue_name) as count
    from    
        seq.glycan_residue gr
    group by 
        gr.residue_name, 
        gr.basetype_id,   
        gr.glycan_sequence_id
;

/**
view for composition by disaccharide + linkage.
*/
create view seq.composition2_v as
    select  
        parent.glycan_sequence_id,   
        parent.residue_name as parent_residue_name,   
        child.residue_name as child_residue_name,   
        count(parent.residue_name) as count,   
        parent.anomer as parent_anomer,   
        child.anomer as child_anomer,   
        child.linkage_parent,   
        child.linkage_child 
    from    
        seq.glycan_residue parent,   
        seq.glycan_residue child 
    where   
        parent.glycan_residue_id = child.parent_id 
    group by 
        parent.glycan_sequence_id,   
        parent.residue_name,   
        child.residue_name,   
        parent.anomer,   
        child.anomer,   
        child.linkage_parent,   
        child.linkage_child
;

-------------------  indexes  -------------------

/** primary key index */
-- create unique index index_glycan_residue_pkey on seq.glycan_residue ( glycan_residue_id );

/** parent key index */
create index index_glycan_residue_parent on seq.glycan_residue ( parent_id );

/** heavily used for substructure searching */
-- create index index_glycan_residue_linkage on seq.glycan_residue ( residue_id, linkage_nr );
create index index_glycan_residue_name on seq.glycan_residue ( residue_name );

create index index_glycan_residue_linkage on seq.glycan_residue ( anomer, linkage_parent, linkage_child );

create index index_glycan_residue_glycan_sequence on seq.glycan_residue ( glycan_sequence_id );

-- /** heavily used for substructure searching */
-- create index index_glycan_residue_linkage_full on seq.glycan_residue ( residue_id, linkage_nr, linkage_r, linkage_a );

-- /** currently not used for substructure searching, but will be... */
-- create index index_residue_type on seq.residue ( residue_type, massclass );

/** index for nested set-based queries */
create index index_glycan_residue_nested_set on seq.glycan_residue ( left_index, right_index );




