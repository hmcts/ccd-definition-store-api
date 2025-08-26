-- Migration to add version column to reindex table for optimistic locking
-- This addresses ObjectOptimisticLockingFailureException by enabling version-based concurrency control

ALTER TABLE reindex ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL;

-- Update existing records to have version 0
UPDATE reindex SET version = 0 WHERE version IS NULL;