-- Pipeline run audit log
CREATE TABLE pipeline_run_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    records_processed INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    error_summary TEXT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at TIMESTAMPTZ
);

CREATE INDEX idx_pipeline_run_name ON pipeline_run_log(pipeline_name);
CREATE INDEX idx_pipeline_run_name_started ON pipeline_run_log(pipeline_name, started_at DESC);

-- Data quality checks linked to pipeline runs
CREATE TABLE data_quality_checks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_run_id UUID NOT NULL REFERENCES pipeline_run_log(id) ON DELETE CASCADE,
    check_name VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    passed BOOLEAN NOT NULL,
    failure_reason TEXT,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dq_checks_run ON data_quality_checks(pipeline_run_id);
CREATE INDEX idx_dq_checks_passed_at ON data_quality_checks(passed, checked_at);

COMMENT ON TABLE pipeline_run_log IS 'Audit log for pipeline executions with lifecycle tracking';
COMMENT ON TABLE data_quality_checks IS 'Individual data quality check results linked to pipeline runs';
