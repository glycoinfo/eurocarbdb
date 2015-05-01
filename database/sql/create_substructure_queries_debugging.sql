
select '
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
' as query1;

select  case when residue.number_of_parents > 1 
        then lpad('+-- ', residue.number_of_parents * 4, '.   ' )
        else '' end
        ||  residue.name
        ||  residue.substruct_residue_id
        as matched_structure
    ,   matching.*
from (
        select  r.name 
            ,   node.linkage_nr 
            ,   node.substruct_id 
            ,   node.substruct_residue_id
            ,   cast(count(parent.substruct_id) as int) as number_of_parents
        from    test.substruct_residue as node
            ,   test.substruct_residue as parent
            ,   test.residue as r
        where   node.left_index between parent.left_index and parent.right_index
            and node.residue_id = r.residue_id 
            and node.substruct_id = parent.substruct_id
        group by node.substruct_id, node.left_index, node.substruct_residue_id, r.name, node.linkage_nr
        order by node.substruct_id, node.left_index
    ) as residue
    join 
    (
        --  copy and paste substructure query from here -> 
        select  g1.substruct_id as substruct_id
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
        --> to here ^^^^^
    ) as matching 
    on matching.substruct_id = residue.substruct_id
order by matching.substruct_id
;


