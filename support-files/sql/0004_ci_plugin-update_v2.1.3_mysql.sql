USE devops_ci_plugin;
SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS ci_log_schema_update;

DELIMITER <CI_UBF>

CREATE PROCEDURE ci_plugin_schema_update()
BEGIN

    DECLARE db VARCHAR(100);
    SET AUTOCOMMIT = 0;
    SELECT DATABASE() INTO db;

    IF EXISTS(SELECT 1
              FROM information_schema.COLUMNS
              WHERE TABLE_SCHEMA = db
                AND TABLE_NAME = 'T_PLUGIN_CODECC_ELEMENT'
                AND COLUMN_NAME = 'ID') THEN
        IF NOT EXISTS(SELECT 1
                      FROM information_schema.COLUMNS
                      WHERE TABLE_SCHEMA = db
                        AND TABLE_NAME = 'T_PLUGIN_CODECC_ELEMENT'
                        AND COLUMN_NAME = 'ID'
                        AND COLUMN_TYPE = 'bigint(20)') THEN
            ALTER TABLE T_PLUGIN_CODECC_ELEMENT MODIFY COLUMN ID BIGINT(20) NOT NULL AUTO_INCREMENT;
        END IF;
    END IF;

    COMMIT;
END <CI_UBF>
DELIMITER ;
COMMIT;
CALL ci_plugin_schema_update();
