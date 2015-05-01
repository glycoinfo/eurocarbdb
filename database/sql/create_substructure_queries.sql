
-- find all structures with a trimannose with 3/6 linkages
select 'find all structures with a trimannose with 3/6 linkages:

                          Man1
                         /    \\
                        3      6  
                       /        \\
                     Man2       Man3    
' as query1;
--
select  p1.substruct_id as matching_structures
    ,   p1.substruct_residue_id as Man1
    ,   c1.substruct_residue_id as Man2
    ,   c2.substruct_residue_id as Man3
from    test.substruct_residue p1 
join    test.substruct_residue c1 
        on p1.substruct_residue_id = c1.parent_id 
        and p1.residue_id = 3 
        and c1.residue_id = 3 
        and c1.linkage_nr = 3 
join    test.substruct_residue c2 
        on p1.substruct_residue_id = c2.parent_id 
        and p1.residue_id = 3 
        and c2.residue_id = 3 
        and c2.linkage_nr = 6;
-- results:        
--  substruct_id | substruct_residue_id | substruct_residue_id | substruct_residue_id 
-- --------------+----------------------+----------------------+----------------------
            -- 1 |                    4 |                    5 |                    9
            -- 4 |                   25 |                   26 |                   30
            -- 4 |                   26 |                   27 |                   28
            -- 4 |                   30 |                   31 |                   33
-- (4 rows)


-- find all structures with a trimannose, ignore linkage
select 'find all structures with a trimannose, ignore linkage:

                          Man1
                         /    \\
                        /      \\  
                       /        \\
                     Man2       Man3    
' as query2;
select  p1.substruct_id as matching_structures
    ,   p1.substruct_residue_id as Man1
    ,   c1.substruct_residue_id as Man2
    ,   c2.substruct_residue_id as Man3
from    test.substruct_residue p1 
join    test.substruct_residue c1 
        on p1.substruct_residue_id = c1.parent_id 
        and p1.residue_id = 3 
        and c1.residue_id = 3 
join    test.substruct_residue c2 
        on p1.substruct_residue_id = c2.parent_id 
        and p1.residue_id = 3 
        and c2.residue_id = 3 
        and c2.linkage_nr != c1.linkage_nr
;
-- results:        
--  substruct_id | root_mannose | mannose3 | mannose6 
-- --------------+--------------+----------+----------
            -- 1 |            4 |        9 |        5
            -- 1 |            4 |        5 |        9
            -- 4 |           25 |       30 |       26
            -- 4 |           25 |       26 |       30
            -- 4 |           26 |       28 |       27
            -- 4 |           26 |       27 |       28
            -- 4 |           30 |       33 |       31
            -- 4 |           30 |       31 |       33
-- (8 rows)


-- find all structures with GlcNAc-Man
select 'find all structures with GlcNAc-Man:

                    GlcNAc1
                      |
                     Man1
' as query3;
select  m1.substruct_id as matching_structures
    ,   m1.substruct_residue_id as GlcNAc1
    ,   m2.substruct_residue_id as Man1
from    test.substruct_residue m1
    ,   test.substruct_residue m2
where   m1.substruct_residue_id = m2.parent_id 
    and m1.residue_id = 1 
    and m2.residue_id = 3 
;
--  substruct_id | substruct_residue_id | substruct_residue_id 
-- --------------+----------------------+----------------------
            -- 1 |                    3 |                    4
            -- 4 |                   24 |                   25
-- (2 rows)


-- find all structures with GlcNAc-Hex
select 'find all structures with GlcNAc-Hex:

                    GlcNAc1
                      |
                     Hex1
' as query4;
select  m1.substruct_id as matching_structures
    ,   m1.substruct_residue_id as GlcNAc1
    ,   m2.substruct_residue_id as Hex1
from    test.substruct_residue m1
    ,   test.substruct_residue m2
    ,   test.residue r2
where   m1.substruct_residue_id = m2.parent_id 
    and r2.residue_id = m2.residue_id
    and m1.residue_id = 1 
    and r2.massclass = 'Hex' 
;
--  substruct_id | substruct_residue_id | substruct_residue_id 
-- --------------+----------------------+----------------------
            -- 1 |                    3 |                    4
            -- 1 |                    6 |                    7
            -- 2 |                   15 |                   16
            -- 4 |                   24 |                   25
-- (4 rows)


-- find all structures with HexNAc-Hex
select 'find all structures with HexNAc-Hex

                    HexNAc1
                      |
                     Hex1
' as query5;
select  m1.substruct_id as matching_structures
    ,   m1.substruct_residue_id as HexNAc1
    ,   m2.substruct_residue_id as Hex1
from    test.substruct_residue m1
    ,   test.substruct_residue m2
    ,   test.residue r1
    ,   test.residue r2
where   m1.substruct_residue_id = m2.parent_id 
    and r1.residue_id = m1.residue_id
    and r2.residue_id = m2.residue_id
    and r1.massclass = 'HexNAc' 
    and r2.massclass = 'Hex' 
;
--  substruct_id | substruct_residue_id | substruct_residue_id 
-- --------------+----------------------+----------------------
            -- 1 |                    3 |                    4
            -- 1 |                    6 |                    7
            -- 2 |                   12 |                   13
            -- 2 |                   15 |                   16
            -- 3 |                   19 |                   21
            -- 4 |                   24 |                   25
-- (6 rows)


-- find all structures with N-linked core
select 'find all structures with N-linked core:

                         GlcNAc1
                            |
                            4
                            |
                          GlcNAc2
                            |
                            4
                            |
                          Man1
                         /    \\
                        3      6  
                       /        \\
                     Man2       Man3    
' as query6;
select  g1.substruct_id as matching_structures
    ,   g1.substruct_residue_id as GlcNAc1
    ,   g2.substruct_residue_id as GlcNAc2
    ,   m1.substruct_residue_id as Man1
    ,   m2.substruct_residue_id as Man2
    ,   m3.substruct_residue_id as Man3
from    test.substruct_residue g1
    ,   test.substruct_residue g2
    ,   test.substruct_residue m1
    ,   test.substruct_residue m2
    ,   test.substruct_residue m3
where   g1.substruct_residue_id = g2.parent_id 
    and g2.substruct_residue_id = m1.parent_id 
    and m1.substruct_residue_id = m2.parent_id 
    and m1.substruct_residue_id = m3.parent_id 
    and g1.residue_id = 1
    and g2.residue_id = 1
    and m1.residue_id = 3
    and m2.residue_id = 3
    and m3.residue_id = 3
    and g2.linkage_nr = 4
    and m1.linkage_nr = 4
    and m2.linkage_nr = 3
    and m3.linkage_nr = 6
;
--  substruct_id | g1 | g2 | m1 | m2 | m3 
-- --------------+----+----+----+----+----
            -- 1 |  1 |  3 |  4 |  5 |  9
            -- 4 | 23 | 24 | 25 | 26 | 30
-- (2 rows)


-- find all structures with partial substructure
select 'find all structures with the partial substructure:

                           Any1
                            |
                            4
                            |
                           Any2
                            |
                            3
                            |
                           Any3
' as query7;
select  a1.substruct_id as matching_structures
    ,   a1.substruct_residue_id as Any1
    ,   a2.substruct_residue_id as Any2
    ,   a3.substruct_residue_id as Any3
from    test.substruct_residue a1
    ,   test.substruct_residue a2
    ,   test.substruct_residue a3
where   a1.substruct_residue_id = a2.parent_id 
    and a2.substruct_residue_id = a3.parent_id 
    and a2.linkage_nr = 4
    and a3.linkage_nr = 3
;
--  matching_structures | any1 | any2 | any3 
-- ---------------------+------+------+------
                   -- 1 |    3 |    4 |    5
                   -- 1 |    6 |    7 |    8
                   -- 2 |   15 |   16 |   17
                   -- 3 |   18 |   19 |   20
                   -- 4 |   24 |   25 |   26
-- (5 rows)


