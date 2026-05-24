CREATE TABLE IF NOT EXISTS message_logs (
    id          BIGSERIAL    PRIMARY KEY,
    channel     VARCHAR(20)  NOT NULL,
    recipient   VARCHAR(512) NOT NULL,
    title       VARCHAR(255),
    status      VARCHAR(10)  NOT NULL,
    message_id  VARCHAR(255),
    error       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_message_logs_created_at ON message_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_logs_status     ON message_logs(status);
CREATE INDEX IF NOT EXISTS idx_message_logs_channel    ON message_logs(channel);
