
drop schema test cascade;

create schema test;

-------------------  tables  -------------------

--  individual structures, 1 row per structure
create table test.substruct (
    substruct_id            serial primary key,
    is_definite             boolean default true, -- false if structure contains unknowns
    is_cyclic               boolean default false
);

--  static residue library, 1 row per monosaccharide
create table test.residue (
    residue_id              serial primary key,
    name                    varchar(8) not null,
    abbrev                  char(1),
    massclass               varchar(6) -- superclass of monosac: Hex/HexNAc/dHex/NeuAc
    
    --residue_mass numeric, -- monoiso residue mass 
    --residue_avgmass numeric, -- average residue mass 
    --terminii  int,        -- bitmask for axial/equatorial: 1 means axial; 
    --connectable int,      -- bitmask for hydroxyls that are initially free/connectable
);

--  individual residues in structures, 1 row per single residue in single structure
create table test.substruct_residue (
    substruct_residue_id    serial primary key,
    substruct_id            int not null references test.substruct,
    residue_id              int not null references test.residue,
    parent_id               int default null references test.substruct_residue,
    
    --linkage_is_glycosidic boolean default true,
    linkage_nr              smallint default null,
    linkage_r               smallint default null,
    linkage_a               char default null,
                                                            
    left_index              int not null unique check( left_index > 0 ), -- for nested set
    right_index             int not null unique check( right_index > left_index ), -- for nested set
    --,
    --graph_depth int, -- index of this residue relative to root, root = 0
    unique( parent_id, linkage_nr ), -- 2 residues can't have same parent & same linkage
    unique( left_index, right_index )
);

--  view for composition by single monosaccharide
create view test.composition_v as 
    select  sr.substruct_id as glycan_sequence_id
        ,   r.residue_id
        ,   r.name
        ,   count(r.residue_id) as count
    from    test.substruct_residue sr
            join test.residue r
            using ( residue_id )
    group by r.residue_id, sr.substruct_id, r.name
;

--  view for composition by disaccharide
create view test.composition2_v as
    select  parent.substruct_id
        ,   parent.residue_id as parent_residue_id
        ,   child.residue_id as child_residue_id
        ,   count(parent.residue_id) as count
        ,   child.linkage_a
        ,   child.linkage_r
        ,   child.linkage_nr 
    from    test.substruct_residue parent
        ,   test.substruct_residue child 
    where   parent.substruct_residue_id = child.parent_id 
    group by parent.substruct_id
        ,   parent.residue_id
        ,   child.residue_id
        ,   child.linkage_a
        ,   child.linkage_r
        ,   child.linkage_nr
;

-------------------  indexes  -------------------

create index index_substruct_residue on test.substruct_residue ( substruct_residue_id, parent_id, residue_id );

create index index_substruct_residue_linkage on test.substruct_residue ( residue_id, linkage_nr );

-------------------  data  -------------------

--  some sample structures  --
copy test.substruct ( substruct_id ) from stdin with csv;
1
2
3
4
\.

--  some sample monosacs  --
copy test.residue ( residue_id, name, abbrev, massclass ) from stdin with CSV;
1,GlcNAc,G,HexNAc
2,GalNAc,N,HexNAc
3,Man,M,Hex
4,Gal,L,Hex
5,Fuc,F,dHex
6,NeuAc,S,NeuAc
\.
             


--------------------  sample data --------------------
                   
-- structure 1: hybrid N-link
select '
------- struct -------+- linkage
                      |
         G-1 -- F-2   |  6
          |           |  4
         G-3          |
          |           |  4
         M-4          |
       /    \\         | 3 6
     M-5    M-9       |
      |      |        | 2 3
     G-6    M-10      |
      |      |        | 4 6
     L-7    S-11      |
      |               | 3
     S-8              |
' as structure1;                   
copy test.substruct_residue ( substruct_residue_id
                            , substruct_id
                            , residue_id
                            , parent_id
                            , linkage_a
                            , linkage_r
                            , linkage_nr
                            , left_index
                            , right_index ) 
from stdin with CSV;
1,1,1,,,,,1,22
2,1,5,1,b,1,6,2,3
3,1,1,1,b,1,4,4,21
4,1,3,3,a,1,4,5,20
5,1,3,4,a,1,3,6,13
6,1,1,5,b,1,2,7,12
7,1,4,6,b,1,4,8,11
8,1,6,7,a,2,3,9,10
9,1,3,4,a,1,6,14,19
10,1,3,9,a,1,3,15,18
11,1,6,10,a,2,6,16,17
\.

-- structure 2: core-2 O-linked
select '
------- struct -------+- linkage
                      |
         N-12         |  
       /    \\         | 3 6
     L-13   G-15      |
      |      |        | 3 4
     S-14   L-16      |
             |        |   3
            S-17      |
' as structure2;                   
copy test.substruct_residue ( substruct_residue_id
                            , substruct_id
                            , residue_id
                            , parent_id
                            , linkage_a
                            , linkage_r
                            , linkage_nr
                            , left_index
                            , right_index ) 
from stdin with CSV;
12,2,2,,,,,23,34
13,2,4,12,b,1,3,24,27
14,2,6,13,a,2,3,25,26
15,2,1,12,b,1,6,28,33
16,2,4,15,b,1,4,29,32
17,2,6,16,a,2,3,30,31
\.

-- structure 3: a made up O-linked-ish
select '
------- struct -------+- linkage
                      |
         N-18         |                        
          |           |  4
         N-19         |  
       /    \\         | 3 4
     S-20   L-21      |
             |        |   6
            S-22      |
' as structure3;                   
copy test.substruct_residue ( substruct_residue_id
                            , substruct_id
                            , residue_id
                            , parent_id
                            , linkage_a
                            , linkage_r
                            , linkage_nr
                            , left_index
                            , right_index ) 
from stdin with CSV;
18,3,2,,,,,35,44
19,3,2,18,b,1,4,36,43
20,3,6,19,a,2,3,37,38
21,3,4,19,b,1,4,39,42
22,3,6,21,a,2,6,40,41
\.

-- structure 4: high mannose N-link
select '                                         
------- struct --------+- linkage
                       |                                                  
         G-23          |  
          |            |  b1-4
         G-24          |                                          
          |            |  b1-4
         M-25          |
       /    \\          | 3 6
     M-26    M-30      |
   /  \\      |  \\      | 3 6 3 6
 M-27 M-28  M-31 M-33  |
       |     |    |    |   3 3 3
      M-29  S-32  S-34 |
' as structure4;                   
copy test.substruct_residue ( substruct_residue_id
                            , substruct_id
                            , residue_id
                            , parent_id
                            , linkage_a
                            , linkage_r
                            , linkage_nr
                            , left_index
                            , right_index ) 
from stdin with CSV;
23,4,1,,,,,45,68
24,4,1,23,b,1,4,46,67
25,4,3,24,a,1,4,47,66
26,4,3,25,a,1,3,48,55
27,4,3,26,a,1,3,49,50
28,4,3,26,a,1,6,51,54
29,4,3,28,a,1,3,52,53
30,4,3,25,a,1,6,56,65
31,4,3,30,a,1,3,57,60
32,4,6,31,a,2,3,58,59
33,4,3,30,a,1,6,61,64
34,4,6,33,a,2,3,62,63
\.


select count(*) || ' structure(s) in total' as listing_all_structures from test.substruct; 
select  case when tree_node.number_of_parents > 1 
        then lpad('+-- ', tree_node.number_of_parents * 4, '.   ' )
        else '
----- structure_id ' || tree_node.structure_id || ' ----- 
' end
    ||  residue.name
    ||  tree_node.substruct_residue_id
    as structure,
    case when linkage_a is null 
          and linkage_r is null 
          and linkage_nr is null then '' else 
        '  ('
        ||  case when linkage_a is not null then linkage_a else 'u' end
        ||  case when linkage_r is not null then linkage_r::varchar else '?' end
        ||  '-'
        ||  case when linkage_nr is not null then linkage_nr::varchar else '?' end
        ||  ') '
        end    
    as linkage
from (
        select  node.residue_id
            ,   node.substruct_id as structure_id
            ,   node.substruct_residue_id 
            ,   node.linkage_a
            ,   node.linkage_r
            ,   node.linkage_nr
            ,   cast(count(parent.substruct_residue_id) as int) as number_of_parents
        from    test.substruct_residue as node
            ,   test.substruct_residue as parent
        where   node.left_index between parent.left_index and parent.right_index
        and     node.substruct_id = parent.substruct_id
        group by node.substruct_id
            ,   node.substruct_residue_id
            ,   node.left_index
            ,   node.residue_id
            ,   node.linkage_a
            ,   node.linkage_r
            ,   node.linkage_nr
        order by node.substruct_id
            ,   node.left_index
    ) as tree_node
    join test.residue as residue
    on tree_node.residue_id = residue.residue_id
;





