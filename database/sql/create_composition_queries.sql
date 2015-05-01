
--select 'find sequences that have composition D-GlcNAc:1-3;D-Man:3-5' as query1;
select  glycan_sequence_id 
from    seq.composition_v 
where   (  (residue_name = 'D-GlcNAc' and count between 1 and 3) 
        or (residue_name = 'D-Man' and count between 3 and 5) ) 
group by glycan_sequence_id 
having  count(glycan_sequence_id) = 2
;


--select 'find sequences that have composition D-GlcNAc:<3;D-Man:>3' as query1;

-- 3760msec, 669
select  glycan_sequence_id 
from    seq.composition_v 
where   (  (residue_name = 'D-GlcNAc' and count < 3) 
        or (residue_name = 'D-Man' and count > 3) ) 
group by glycan_sequence_id 
having  count(glycan_sequence_id) = 2
;

--  111msec, 669 rows
select  glycan_sequence_id 
from    seq.composition_v 
where  residue_name = 'D-GlcNAc' and count < 3
group by glycan_sequence_id 

intersect

select  glycan_sequence_id 
from    seq.composition_v 
where  residue_name = 'D-Man' and count > 3
group by glycan_sequence_id 
;



-------------

--  3770msec, 133 rows
select  glycan_sequence_id 
from    seq.composition_v 
where (  
        (residue_name = 'D-GlcNAc' and count between 2 and 4) 
    or  (residue_name = 'D-Man' and count between 3 and 6) 
    or  (residue_name = 'L-Fuc' and count between 2 and 3 ) 
) 
group by glycan_sequence_id 
having  count(glycan_sequence_id) = 3
;

--  139msec, 133 rows
    select  glycan_sequence_id 
    from    seq.composition_v 
    where  residue_name = 'D-GlcNAc' and count between 2 and 4
    group by glycan_sequence_id 
intersect
    select  glycan_sequence_id 
    from    seq.composition_v 
    where  residue_name = 'D-Man' and count between 3 and 6
    group by glycan_sequence_id 
intersect
    select  glycan_sequence_id 
    from    seq.composition_v 
    where  residue_name = 'L-Fuc' and count between 2 and 3
    group by glycan_sequence_id 
;
