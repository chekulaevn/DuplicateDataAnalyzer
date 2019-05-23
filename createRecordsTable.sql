DROP TABLE records;
					
CREATE TABLE records
                    (id               SERIAL			  PRIMARY KEY,
                    name              CHAR(50)            NOT NULL,
                    lastname          CHAR(50)            NOT NULL,
                    gender            CHAR(50)            NOT NULL);