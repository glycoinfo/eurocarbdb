--  
--  run this script as: psql -U <db-user> <eurocarb-db-name> -f debug_structures.sql
--  to get an indented tree view of all structures in the DB.
--  author: mjh
--
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


