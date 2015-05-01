create temporary table export_gaycomedb as
select 
    s.glyco_ct,
    t.taxon_id,
    r.resource,
    r.resource_id
from 
    glycomedb.core.structure s
left join 
    glycomedb.remote.remote_structure_has_structure h
        on s.structure_id = h.structure_id
left join
    glycomedb.remote.remote_structure r
        on h.remote_structure_id = r.remote_structure_id
left join 
    glycomedb.remote.remote_structure_has_taxon t
        on r.remote_structure_id = t.remote_structure_id

order by s.structure_id asc;

copy export_gaycomedb to stdout with CSV;



