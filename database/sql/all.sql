delete from TRANSACTIONS;
delete from OPEN_TRANSACTIONS;
delete from SCENARIO_RESPONSES;
delete from SCENARIOS;
delete from RESPONSE_BODY;
delete from RESPONSES;
delete from REQUEST_BODY;
delete from PROBLEMS;
delete from REQUEST_PARAMS;
delete from REQUESTS;
delete from PATHS;
delete from SPECS;

drop table if exists TRANSACTIONS;
drop table if exists OPEN_TRANSACTIONS;
drop table if exists SCENARIO_RESPONSES;
drop table if exists SCENARIOS;
drop table if exists RESPONSE_BODY;
drop table if exists RESPONSES;
drop table if exists REQUEST_BODY;
drop table if exists PROBLEMS;
drop table if exists REQUEST_PARAMS;
drop table if exists REQUESTS;
drop table if exists PATHS;
drop table if exists SPECS;

CREATE TABLE IF NOT EXISTS TRANSACTIONS(
                                           uuid VARCHAR(256) PRIMARY KEY,
                                           host varchar(256) not null CHECK(LENGTH(host) >= 1 and LENGTH(host) <= 256)
);

CREATE TABLE IF NOT EXISTS OPEN_TRANSACTIONS(
                                                uuid VARCHAR(256) PRIMARY KEY,
                                                host varchar(256),
                                                spec_id integer not null,
                                                isAlive boolean not null DEFAULT true
);

CREATE TABLE IF NOT EXISTS SPECS(
                                    id serial primary key,
                                    name varchar(256) not null CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
                                    description varchar(256) CHECK(LENGTH(description) >= 1 and LENGTH(description) <= 256),
                                    transaction_token varchar(256) not null
);

CREATE TABLE IF NOT EXISTS PATHS(
                                    id serial unique,
                                    full_path varchar(256) not null,
                                    operations jsonb not null,
                                    spec_id integer not null,
                                    foreign key (spec_id) references SPECS(id) on delete cascade,
                                    primary key (full_path, spec_id)
);

CREATE TABLE IF NOT EXISTS REQUESTS(
                                       uuid VARCHAR(256) PRIMARY KEY,
                                       external_key VARCHAR(256) CHECK(LENGTH(external_key) >= 5 and LENGTH(external_key) <= 256),
                                       url VARCHAR(256) NOT NULL,
                                       method VARCHAR(256) NOT NULL,
                                       path VARCHAR(256) NOT NULL,
                                       host VARCHAR(256) NOT NULL,
                                       spec_id integer not null,
                                       headers jsonb,
                                       foreign key (spec_id, path) references PATHS(spec_id, full_path) on delete cascade
);

CREATE TABLE IF NOT EXISTS REQUEST_PARAMS(
                                             id SERIAL unique,
                                             type VARCHAR(256) NOT NULL CHECK(LENGTH(type) >= 1 and LENGTH(type) <= 256),
                                             location VARCHAR(256) NOT NULL CHECK(LENGTH(location) >= 1 and LENGTH(location) <= 256),
                                             name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
                                             content varchar(256) NOT NULL CHECK(LENGTH(content) >= 1 and LENGTH(content) <= 256),
                                             uuid VARCHAR(256) NOT NULL,
                                             FOREIGN KEY (uuid) REFERENCES REQUESTS(uuid) ON DELETE CASCADE,
                                             PRIMARY KEY (id, uuid)
);

CREATE TABLE IF NOT EXISTS PROBLEMS(
                                       id SERIAL unique,
                                       description VARCHAR(256) NOT NULL CHECK(LENGTH(description) >= 1 and LENGTH(description) <= 256),
                                       type VARCHAR(256) NOT NULL CHECK(LENGTH(type) >= 1 and LENGTH(type) <= 256),
                                       uuid VARCHAR(256) NOT NULL,
                                       FOREIGN KEY (uuid) REFERENCES REQUESTS(uuid) ON DELETE CASCADE,
                                       PRIMARY KEY (id, uuid)
);

CREATE TABLE IF NOT EXISTS REQUEST_BODY(
                                           id SERIAL unique,
                                           content_type VARCHAR(1024),
                                           content BYTEA NOT NULL,
                                           uuid VARCHAR(256) NOT NULL,
                                           FOREIGN KEY (uuid) REFERENCES REQUESTS(uuid) ON DELETE CASCADE,
                                           PRIMARY KEY (id, uuid)
);

CREATE TABLE IF NOT EXISTS RESPONSES(
                                        id SERIAL unique,
                                        status_code VARCHAR(256) NOT NULL CHECK(LENGTH(status_code) >= 1 and LENGTH(status_code) <= 256),
                                        uuid VARCHAR(256) NOT NULL,
                                        headers jsonb,
                                        FOREIGN KEY (uuid) REFERENCES REQUESTS(uuid) ON DELETE CASCADE,
                                        PRIMARY KEY (id, uuid)
);

CREATE TABLE IF NOT EXISTS RESPONSE_BODY(
                                            id SERIAL unique,
                                            content_type VARCHAR(256) NOT NULL CHECK(LENGTH(content_type) >= 1 and LENGTH(content_type) <= 256),
                                            content BYTEA NOT NULL,
                                            response_id integer NOT NULL,
                                            FOREIGN KEY (response_id) REFERENCES RESPONSES(id) ON DELETE CASCADE,
                                            PRIMARY KEY (id, response_id)
);

CREATE TABLE IF NOT EXISTS SCENARIOS(
                                        name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
                                        spec_id integer not null,
                                        transaction_token varchar(256) not null,
                                        method VARCHAR(256) NOT NULL CHECK(LENGTH(method) >= 1 and LENGTH(method) <= 256),
                                        path VARCHAR(256) NOT NULL CHECK(LENGTH(path) >= 1 and LENGTH(path) <= 256),
                                        FOREIGN KEY (spec_id, path) REFERENCES PATHS(spec_id, full_path) ON DELETE CASCADE,
                                        PRIMARY KEY (name, spec_id)
);

CREATE TABLE IF NOT EXISTS SCENARIO_RESPONSES(
                                                 index integer NOT NULL,
                                                 status_code VARCHAR(256) NOT NULL CHECK(LENGTH(status_code) >= 1 and LENGTH(status_code) <= 256),
                                                 body BYTEA,
                                                 headers jsonb,
                                                 content_type VARCHAR(256),
                                                 spec_id integer NOT NULL,
                                                 scenario_name VARCHAR(256) NOT NULL,
                                                 FOREIGN KEY (spec_id, scenario_name) REFERENCES SCENARIOS(spec_id, name) ON DELETE CASCADE,
                                                 PRIMARY KEY (index, scenario_name, spec_id)
);

create or replace function notify_specs_update()
    returns trigger as $$
begin
    PERFORM pg_notify('update_spec', 'specs was updated');
    return new;
end;
$$ language plpgsql;

create or replace trigger notify_spec_insert
    after insert on TRANSACTIONS
    for each row
execute procedure notify_specs_update();

create or replace trigger notify_spec_update
    after update on TRANSACTIONS
    for each row
execute procedure notify_specs_update();