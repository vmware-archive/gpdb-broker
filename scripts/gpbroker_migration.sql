/*
 * Migrate GPDB broker data from original version to the updated version which supports
 * create-service-key and delete-service-key.
 *
 * 25 Oct. 2019
 */

/*
Query to determine which DBs to drop:

				"SELECT service_instance_id\n"
				+ "FROM " + tableName + "\n"
				+ "WHERE\n"
				+ "  disabled_at IS NOT NULL\n" -- THIS IS THE NEW COLUMN; IT WILL BE NULL.
				+ "  AND dropped_at IS NULL\n"
				+ "  AND service_instance_id NOT IN (SELECT datname FROM pg_stat_activity)\n"
				+ "ORDER BY disabled_at ASC";
*/

-- Create the new table
CREATE TABLE new_gpbroker_service
(
    service_instance_id character varying(200) DEFAULT ''::character varying NOT NULL,
    service_definition_id character varying(200) DEFAULT ''::character varying NOT NULL,
    plan_id character varying(200) DEFAULT ''::character varying NOT NULL,
    organization_guid character varying(200) DEFAULT ''::character varying NOT NULL,
    space_guid character varying(200) DEFAULT ''::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    last_access date,
    dropped_at timestamp with time zone,
    disabled_at timestamp with time zone
) DISTRIBUTED BY (service_instance_id);

-- Import the data
insert into new_gpbroker_service
select service_instance_id, service_definition_id, plan_id, organization_guid, space_guid, created_at, last_access, dropped_at, NULL
from gpbroker_service;

-- Switch the tables
begin;
alter table gpbroker_service rename to old_gpbroker_service;
alter table new_gpbroker_service rename to gpbroker_service;
commit;

