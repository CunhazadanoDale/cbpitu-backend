DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'tb_times'
        AND column_name = 'trofeus'
        AND data_type IN ('character varying', 'text')
    ) THEN
        ALTER TABLE tb_times ALTER COLUMN trofeus TYPE integer USING (CASE WHEN trofeus = '' THEN 0 ELSE trofeus::integer END);
    END IF;
END $$;
///
