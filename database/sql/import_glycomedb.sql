
create table core.glycomedb (
    glycoct     varchar(4096) not null,
    taxon_id    int not null,
    resource    varchar(64) not null,
    resource_id varchar(32) not null
);

copy core.glycomedb from '/Users/matt/eurocarb/trunk/database/glycomedb_exported.csv' with CSV;



