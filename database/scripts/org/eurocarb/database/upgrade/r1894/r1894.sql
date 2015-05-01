-- make guests unique
update core.contributor set contributor_name=contributor_name||contributor_id where contributor_name='guest';

-- remove not null fields;
update core.contributor set contributor_name='guest'||contributor_id where contributor_name='';

-- make contributor_name unique column.
alter table core.contributor add constraint contributor_name_unique UNIQUE (contributor_name);

-- make contributor_name not null.
alter table core.contributor alter column contributor_name SET NOT NULL;

-- update full names
update core.contributor set full_name=contributor_name where full_name='' or full_name is null;

-- make full name not null
alter table core.contributor alter column full_name SET NOT NULL;

-- add a column email.
alter table core.contributor add column email character varying(128);

-- add a column is activated.
alter table core.contributor add column is_activated boolean NOT NULL DEFAULT false;

-- add a column last login.
alter table core.contributor add column last_login date DEFAULT NULL;

-- add a column is blocked.
alter table core.contributor add column is_blocked boolean NOT NULL DEFAULT false;

-- add a user account admin 
insert into core.contributor(contributor_name,full_name,is_admin,is_activated,institution) values('admin', 'admin', true, true,'eurocarb');

-- encrypt the password.
update core.contributor set password='JJOZN4fP+OuGn30Qr36JYkJPuSkyDGDo';

-- make email unique by inserting some random data.
update core.contributor set email=contributor_name||'@'||contributor_name||contributor_id||'.com';

-- make email unique constaint.
alter table core.contributor add constraint email_unique UNIQUE (email);


