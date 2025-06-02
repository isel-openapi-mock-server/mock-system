INSERT INTO SPECS (name, description, transaction_token) VALUES
('ChIMP API', 'API for Instant messaging application', 'transaction1'),
('ChIMP API', 'API for Instant messaging application', 'transaction2');

INSERT INTO PATHS (full_path, operations, spec_id) VALUES
('/users/search', '[{"method": "GET", "headers": [], "servers": [], "security": true, "responses": [{"schema": {"@type": "ContentField", "content": {"application/json": {"@type": "SchemaObject", "schema": "{\"type\":\"array\",\"exampleSetFlag\":false,\"items\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"example\":1,\"exampleSetFlag\":true,\"types\":[\"integer\"]},\"username\":{\"type\":\"string\",\"example\":\"bob123\",\"exampleSetFlag\":true,\"types\":[\"string\"]}},\"exampleSetFlag\":false,\"types\":[\"object\"]},\"types\":[\"array\"]}"}}}, "headers": [{"name": "A", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"string\",\"exampleSetFlag\":false,\"types\":[\"string\"]}"}, "style": "FORM", "explode": false, "required": false, "description": "bom dia"}], "statusCode": "OK"}, {"schema": null, "headers": [], "statusCode": "INTERNAL_SERVER_ERROR"}], "parameters": [{"name": "username", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"string\",\"exampleSetFlag\":false,\"types\":[\"string\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": true, "description": null, "allowEmptyValue": false}, {"name": "limit", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"integer\",\"exampleSetFlag\":false,\"types\":[\"integer\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": false, "description": null, "allowEmptyValue": false}, {"name": "skip", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"integer\",\"exampleSetFlag\":false,\"types\":[\"integer\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": false, "description": null, "allowEmptyValue": false}], "requestBody": {"content": {"@type": "ContentField", "content": {}}, "required": false}}]'::jsonb, 1),
    ('/users/{id}',
        '[
          {
            "method": "GET",
            "headers": [],
            "servers": ["http://localhost:8080/api"],
            "security": false,
            "responses": [
              {
                "schema": {
                  "@type": "ContentField",
                  "content": {
                    "application/json": {
                      "@type": "SchemaObject",
                      "schema": "{ \"type\": \"object\", \"properties\": { \"id\": { \"type\": \"integer\" }, \"username\": { \"type\": \"string\" } } }"
                    }
                  }
                },
                "headers": [],
                "statusCode": "OK"
              },
              {
                "schema": {
                  "@type": "ContentField",
                  "content": {
                    "application/json": {
                      "@type": "SchemaObject",
                      "schema": "{ \"type\": \"string\", \"example\": \"User not found\" }"
                    }
                  }
                },
                "headers": [],
                "statusCode": "NOT_FOUND"
              },
              {
                "schema": null,
                "headers": [],
                "statusCode": "INTERNAL_SERVER_ERROR"
              }
            ],
            "parameters": [
              {
                "name": "id",
                "type": {
                  "@type": "SchemaObject",
                  "schema": "{ \"type\": \"integer\" }"
                },
                "style": "SIMPLE",
                "explode": false,
                "location": "PATH",
                "required": true,
                "description": "The ID of the user",
                "allowEmptyValue": false
              }
            ],
            "requestBody": {
              "content": {
                "@type": "ContentField",
                "content": {}
              },
              "required": false
            }
          }
        ]'::jsonb,
        1
    ),
('/users/search', '[{"method": "GET", "headers": [], "servers": [], "security": true, "responses": [{"schema": {"@type": "ContentField", "content": {"application/json": {"@type": "SchemaObject", "schema": "{\"type\":\"array\",\"exampleSetFlag\":false,\"items\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"example\":1,\"exampleSetFlag\":true,\"types\":[\"integer\"]},\"username\":{\"type\":\"string\",\"example\":\"bob123\",\"exampleSetFlag\":true,\"types\":[\"string\"]}},\"exampleSetFlag\":false,\"types\":[\"object\"]},\"types\":[\"array\"]}"}}}, "headers": [{"name": "A", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"string\",\"exampleSetFlag\":false,\"types\":[\"string\"]}"}, "style": "FORM", "explode": false, "required": false, "description": "bom dia"}], "statusCode": "OK"}, {"schema": null, "headers": [], "statusCode": "INTERNAL_SERVER_ERROR"}], "parameters": [{"name": "username", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"string\",\"exampleSetFlag\":false,\"types\":[\"string\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": true, "description": null, "allowEmptyValue": false}, {"name": "limit", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"integer\",\"exampleSetFlag\":false,\"types\":[\"integer\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": false, "description": null, "allowEmptyValue": false}, {"name": "skip", "type": {"@type": "SchemaObject", "schema": "{\"type\":\"integer\",\"exampleSetFlag\":false,\"types\":[\"integer\"]}"}, "style": "FORM", "explode": true, "location": "QUERY", "required": false, "description": null, "allowEmptyValue": false}], "requestBody": {"content": {"@type": "ContentField", "content": {}}, "required": false}}]'::jsonb, 2);

INSERT INTO transactions (uuid, host) VALUES
('transaction1', 'host1');

INSERT INTO OPEN_TRANSACTIONS (uuid, host, spec_id, isAlive) VALUES
('transaction1', 'host1', 1, false),
('transaction2', 'host2', 2, true);

INSERT INTO requests (uuid, external_key, url, method, path, host, spec_id, headers) values
('request1','type2', '/users/search', 'GET', '/users/search', 'host1', 1, '{"A": "bom dia"}'::jsonb),
('request2', 'type2', '/users/search', 'GET', '/users/search', 'host1', 1, '{"A": "bom dia"}'::jsonb);

INSERT INTO request_params (type, location, name, content, uuid) VALUES
('{"type":"string","exampleSetFlag":false,"types":["string"]}', 'query', 'username', 'bob123', 'request1'),
('{"type":"string","exampleSetFlag":false,"types":["string"]}', 'query', 'username', '1', 'request2');

INSERT INTO problems (description, type, uuid) VALUES
('Invalid parameter type: {"type":"string","exampleSetFlag":false,"types":["string"]} for parameter username in query', 'InvalidType', 'request2');

INSERT INTO responses (uuid, status_code, headers) VALUES
('request1', '200', '{"host": "RnQdue2a6rh45A6t", "accept": "*/*", "connection": "keep-alive", "user-agent": "PostmanRuntime/7.44.0", "external-key": "type2", "authorization": "Bearer 8WSe5Uf9htPDkH3yza_mjNnUi01W76C6hS0dcc38dMI=", "cache-control": "no-cache", "postman-token": "bb77b13d-abca-417b-ac36-f56558394913", "scenario-name": "test2", "accept-encoding": "gzip, deflate, br"}'::jsonb);

INSERT INTO response_body (response_id, content_type, content) VALUES
(1, 'application/json', decode('W3siaWQiOjUsInVzZXJuYW1lIjoiZGlvZ28ifSx7ImlkIjo0MCwidXNlcm5hbWUiOiJtYXJ0aW0ifV0=', 'base64'));

INSERT INTO scenarios (name, spec_id, transaction_token, method, path) VALUES
('test1', 1, 'transaction1', 'GET', '/users/search'),
('test2', 2, 'transaction2', 'GET', '/users/search');

INSERT INTO scenario_responses (index, status_code, body, headers, content_type, spec_id, scenario_name) values
(0, '200', decode('W3siaWQiOjUsInVzZXJuYW1lIjoiZGlvZ28ifSx7ImlkIjo0MCwidXNlcm5hbWUiOiJtYXJ0aW0ifV0=', 'base64'), '{"A": "bom dia"}'::jsonb, 'application/json', 1, 'test1');
