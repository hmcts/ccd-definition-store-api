List of files that can be used to clean ccd-definition-store DB:

1. Remove case_type older than 3 months - Optimised.sql
   This file contains manual steps to clean up the data, currently set to delete
   all data older than 3 months, retaining the highest version of each case_type
   and ensuring base types are not deleted.
   Comments within the file, detail how to run the various steps.
2. new-clean-up-quaries/types_older_than_3_months.sql
   This file is semi automated, just copy the entire script into i.e. DBWeaver and execute as one single transaction.
   It will create various Functions and call these sequently. As 1. above this also defaults to deleting data older than 3 months, honouring data that should always be kept.
3. new-clean-up-quaries/         stored-procedure-safe_delete_query_case-types_older_than_3_months.sql
   This file represents a DB store procedure. Copy the entire script into i.e. DBWeaver and execute as one transaction. This will create and persist the SP into the DB.
   For subsequent data-clean-up operation (i.e. either automated via a cron job or on a manual scheduled clean-up exercise) simply run the following command within i.e DBWeaver
   call cleanup_case_types(2000)
   The above call, will trigger the SP execution, 2000 signifies the batch operation, i.e. processed batched deletions in loops of 2000 records at a time. If a batch deletion fails (i.e. any record in the batch of 2000 encountering an error) the process will fall-back to a record-by-record deletion.