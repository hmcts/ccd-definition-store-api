--
-- Name: pk_event_post_state; Type: INDEX; Schema: public; Owner: -
-- Removes an INVALID index left by an interrupted earlier run so the CREATE below can rebuild it.
-- No-op on a clean run.
--
DROP INDEX CONCURRENTLY IF EXISTS pk_event_post_state;

--
-- Name: pk_event_post_state; Type: INDEX; Schema: public; Owner: -
--
CREATE UNIQUE INDEX CONCURRENTLY pk_event_post_state ON event_post_state USING btree (id);
