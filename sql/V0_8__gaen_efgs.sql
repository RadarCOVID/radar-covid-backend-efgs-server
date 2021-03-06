ALTER TABLE T_GAEN_EXPOSED
    ADD COLUMN COUNTRY_ORIGIN   CHAR(2),
    ADD COLUMN REPORT_TYPE      SMALLINT,
    ADD COLUMN DAYS_SINCE_ONSET SMALLINT,
    ADD COLUMN EFGS_SHARING     BOOLEAN,
    ADD COLUMN BATCH_TAG        CHAR VARYING(128)
;

UPDATE T_GAEN_EXPOSED
   SET COUNTRY_ORIGIN = 'ES',
       REPORT_TYPE    = 2,
       EFGS_SHARING   = TRUE
;

CREATE INDEX IN_GAEN_EXPOSED_COUNTRY_SHARING_RECEIVED
    ON T_GAEN_EXPOSED(COUNTRY_ORIGIN, EFGS_SHARING, RECEIVED_AT);

CREATE INDEX IN_GAEN_EXPOSED_COUNTRY_SHARING_BATCH_TAG
    ON T_GAEN_EXPOSED(COUNTRY_ORIGIN, EFGS_SHARING, BATCH_TAG);

CREATE TABLE T_VISITED (
    PFK_EXPOSED_ID INTEGER NOT NULL,
    COUNTRY        CHAR(2),
    CONSTRAINT PK_T_VISITED
        PRIMARY KEY (PFK_EXPOSED_ID, COUNTRY),
    CONSTRAINT R_GAEN_EXPOSED_VISITED
        FOREIGN KEY (PFK_EXPOSED_ID)
            REFERENCES T_GAEN_EXPOSED (PK_EXPOSED_ID) ON DELETE CASCADE
);

CREATE INDEX IDX_VISITED_EXPOSED_ID 
    ON T_VISITED(PFK_EXPOSED_ID);