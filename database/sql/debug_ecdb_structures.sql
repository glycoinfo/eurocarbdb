--  
--  run this script as: psql -U <db-user> <eurocarb-db-name> -f debug_structures.sql
--  to get an indented tree view of all structures in the DB.
--  author: mjh
--
select 
    count( distinct( glycan_sequence_id )) 
    || ' structure(s) in total' 
    as listing_all_structures 
from 
    seq.glycan_residue;

select  case when tree_node.number_of_parents > 1 
        then lpad('+-- ', tree_node.number_of_parents * 4, '   ' )
        else '
----- structure_id ' || tree_node.structure_id || ' ----- 
' end
    ||  case when anomer is null 
        and linkage_parent is null 
        and linkage_child is null 
        then '' else 
        '('
        ||  case when anomer is not null then anomer else 'u' end
        ||  case when linkage_child is not null then linkage_child::varchar else '?' end
        ||  '-'
        ||  case when linkage_parent is not null then linkage_parent::varchar else '?' end
        ||  ') ' 
        end   
    ||  residue_name
    ||  '_'
    ||  tree_node.glycan_residue_id
    as structure
from (
        select  node.residue_name
            ,   node.glycan_sequence_id as structure_id
            ,   node.glycan_residue_id 
            ,   node.anomer
            ,   node.linkage_parent
            ,   node.linkage_child
            ,   cast(count(parent.glycan_residue_id) as int) as number_of_parents
        from    seq.glycan_residue as node
            ,   seq.glycan_residue as parent
        where   node.left_index between parent.left_index and parent.right_index
        and     node.glycan_sequence_id = parent.glycan_sequence_id
        group by node.glycan_sequence_id
            ,   node.glycan_residue_id
            ,   node.left_index
            ,   node.residue_name
            ,   node.anomer
            ,   node.linkage_parent
            ,   node.linkage_child
        order by node.glycan_sequence_id
            ,   node.left_index
    ) as tree_node
;


