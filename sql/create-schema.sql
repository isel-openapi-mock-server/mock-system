CREATE TABLE IF NOT EXISTS REQUEST(
    uuid VARCHAR(256) PRIMARY KEY,
    external_key VARCHAR(256) NOT NULL UNIQUE CHECK(LENGTH(external_key) >= 5 and LENGTH(external_key) <= 256),
    url VARCHAR(256) NOT NULL,
    method VARCHAR(256) NOT NULL,
    path VARCHAR(256) NOT NULL,
    host VARCHAR(256) NOT NULL
);

CREATE TABLE IF NOT EXISTS REQUEST_PARAM(
    id SERIAL unique,
    type VARCHAR(256) NOT NULL UNIQUE CHECK(LENGTH(type) >= 1 and LENGTH(type) <= 256),
    location VARCHAR(256) NOT NULL CHECK(LENGTH(location) >= 1 and LENGTH(location) <= 256),
    name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
    content varchar(256) NOT NULL CHECK(LENGTH(content) >= 1 and LENGTH(content) <= 256),
    request_id VARCHAR(256) NOT NULL,
    FOREIGN KEY (request_id) REFERENCES REQUEST(uuid) ON DELETE CASCADE,
    PRIMARY KEY (id, request_id)
);

CREATE TABLE IF NOT EXISTS PROBLEM(
    id SERIAL unique,
    location VARCHAR(256) NOT NULL CHECK(LENGTH(location) >= 1 and LENGTH(location) <= 256),
    description VARCHAR(256) NOT NULL CHECK(LENGTH(description) >= 1 and LENGTH(description) <= 256),
    request_id VARCHAR(256) NOT NULL,
    FOREIGN KEY (request_id) REFERENCES REQUEST(uuid) ON DELETE CASCADE,
    PRIMARY KEY (id, request_id)
);

CREATE TABLE IF NOT EXISTS REQUEST_BODY(
    id SERIAL unique,
    type VARCHAR(256) NOT NULL CHECK(LENGTH(type) >= 1 and LENGTH(type) <= 256),
    content VARCHAR(256) NOT NULL CHECK(LENGTH(content) <= 256),
    request_id VARCHAR(256) NOT NULL,
    FOREIGN KEY (request_id) REFERENCES REQUEST(uuid) ON DELETE CASCADE,
    PRIMARY KEY (id, request_id)
);

CREATE TABLE IF NOT EXISTS RESPONSE(
    id SERIAL unique,
    status_code VARCHAR(256) NOT NULL CHECK(LENGTH(status_code) >= 1 and LENGTH(status_code) <= 256),
    request_id VARCHAR(256) NOT NULL,
    FOREIGN KEY (request_id) REFERENCES REQUEST(uuid) ON DELETE CASCADE,
    PRIMARY KEY (id, request_id)
);

CREATE TABLE IF NOT EXISTS RESPONSE_BODY(
    id SERIAL unique,
    type VARCHAR(256) NOT NULL CHECK(LENGTH(type) >= 1 and LENGTH(type) <= 256),
    content VARCHAR(256) NOT NULL CHECK(LENGTH(content) <= 256),
    response_id integer NOT NULL,
    FOREIGN KEY (response_id) REFERENCES RESPONSE(id) ON DELETE CASCADE,
    PRIMARY KEY (id, response_id)
);

CREATE TABLE IF NOT EXISTS RESPONSE_HEADER(
    id SERIAL unique,
    name VARCHAR(256) NOT NULL CHECK(LENGTH(name) >= 1 and LENGTH(name) <= 256),
    content VARCHAR(256) NOT NULL CHECK(LENGTH(content) >= 1 and LENGTH(content) <= 256),
    response_id integer NOT NULL,
    FOREIGN KEY (response_id) REFERENCES RESPONSE(id) ON DELETE CASCADE,
    PRIMARY KEY (id, response_id)
);

