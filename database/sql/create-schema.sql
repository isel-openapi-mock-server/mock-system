delete from REQUEST_HEADERS;
delete from RESPONSE_HEADERS;
delete from RESPONSE_BODY;
delete from RESPONSES;
delete from REQUEST_BODY;
delete from PROBLEMS;
delete from REQUEST_PARAMS;
delete from REQUESTS;

drop table if exists REQUEST_HEADERS;
drop table if exists RESPONSE_HEADERS;
drop table if exists RESPONSE_BODY;
drop table if exists RESPONSES;
drop table if exists REQUEST_BODY;
drop table if exists PROBLEMS;
drop table if exists REQUEST_PARAMS;
drop table if exists REQUESTS;

CREATE TABLE IF NOT EXISTS REQUESTS(
    uuid VARCHAR(256) PRIMARY KEY,
    external_key VARCHAR(256) NOT NULL CHECK(LENGTH(external_key) >= 5 and LENGTH(external_key) <= 256),
    url VARCHAR(256) NOT NULL,
    method VARCHAR(256) NOT NULL,
    path VARCHAR(256) NOT NULL,
    host VARCHAR(256) NOT NULL
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

CREATE TABLE IF NOT EXISTS RESPONSE_HEADERS(
    id SERIAL unique,
    name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
    content VARCHAR(256) NOT NULL CHECK(LENGTH(content) >= 1 and LENGTH(content) <= 256),
    response_id integer NOT NULL,
    FOREIGN KEY (response_id) REFERENCES RESPONSES(id) ON DELETE CASCADE,
    PRIMARY KEY (id, response_id)
);

CREATE TABLE IF NOT EXISTS REQUEST_HEADERS(
    id SERIAL unique,
    name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
    content VARCHAR(256) NOT NULL CHECK(LENGTH(content) >= 1 and LENGTH(content) <= 256),
    uuid VARCHAR(256) NOT NULL,
    FOREIGN KEY (uuid) REFERENCES REQUESTS(uuid) ON DELETE CASCADE,
    PRIMARY KEY (id, uuid)
);

