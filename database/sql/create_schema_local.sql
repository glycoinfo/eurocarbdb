/******************************************************************************

=head1  EuroCarbDB user schema 

=head2 Authors

Matt Harrison <matt@ebi.ac.uk>

Version: $Id:$ 

Changelog:
    
    + 2007-03-10 - first revision	

*/

CREATE SCHEMA local;

/*===  TABLES  ===*/


/*  table user_to_contributor  *//***********************************
*
*   Join table for the many-many relationship between local.user
*   and core.contributor.
*
*/
CREATE TABLE local.user_to_contributor
(
    user_to_contributor_id          SERIAL PRIMARY KEY NOT NULL, 
         
    user_id                         INT 
                                    REFERENCES local.user
                                    ON UPDATE CASCADE ON DELETE CASCADE
                                    DEFERRABLE INITIALLY DEFERRED,
    
    contributor_id                  INT 
                                    REFERENCES core.contributor
                                    ON UPDATE CASCADE ON DELETE CASCADE
                                    DEFERRABLE INITIALLY DEFERRED,

    date_entered                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP

)
;



/*  table user  *//***********************************************
*
*   Table that collates all local users of this database instance.
*/
CREATE TABLE local.user
(
    user_id                         SERIAL PRIMARY KEY NOT NULL, 

    user_name                       VARCHAR(16),
    
    user_password                   VARCHAR(16),                    
    
    user_firstname                  VARCHAR(32),
    
    user_lastname                   VARCHAR(32),
    
    date_entered                    TIMESTAMP DEFAULT CURRENT_TIMESTAMP

)
;



