--
-- Name: event_post_state pk_event_post_state; Type: CONSTRAINT; Schema: public; Owner: -
-- lock_timeout: fail fast (retried on next startup) instead of queueing for ACCESS EXCLUSIVE behind
-- a running import, which would block all reads of event_post_state. SET LOCAL keeps it to this transaction.
--
SET LOCAL lock_timeout = '5s';
ALTER TABLE ONLY event_post_state
    ADD CONSTRAINT pk_event_post_state PRIMARY KEY USING INDEX pk_event_post_state;
