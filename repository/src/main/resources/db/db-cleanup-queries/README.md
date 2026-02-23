# 🧹 CCD Definition Store Database Cleanup Files

Below is a list of scripts that can be used to safely clean and optimise the **ccd-definition-store** database.

---

### 1. `Remove case_type older than 3 months - Optimised.sql`

This file contains **manual steps** to clean up the data.

- Currently set to delete all data **older than 3 months**.  
- Retains the **highest version** of each `case_type`.  
- Ensures that **base types** are not deleted.  
- Includes detailed comments explaining how to run each step.

> 💡 **Usage:**  
> Open the file, follow the comments, and execute each section manually in your SQL client (e.g. DBeaver or psql).

---

### 2. `new-clean-up-quaries/safe_delete_query_case-types_older_than_3_months.sql`

This file provides a **semi-automated cleanup** process.

- Copy the entire script into your SQL client (e.g. DBeaver).  
- Execute it **as a single transaction**.  
- It will automatically create various **functions** and **call them sequentially**.  
- Like (1), it deletes data **older than 3 months** while keeping required base data.
- Modifiable parameters are:
-   SELECT prepare_cleanup_temp_tables(3); -- the 3 here signifies delete data older than 3 months
-   SELECT run_safe_deletes(1000); -- the 1000 here signifies perform the deletions in batches of 1000 records

> ⚙️ **Notes:**
> - The script uses transactions for safety.  
> - You can adjust the 3-month threshold and number of batched deletions by editing the script logic.

---
 
### 3. `new-clean-up-quaries/stored-procedure-safe_delete_query_case-types_older_than_3_months.sql`

This file defines a **stored procedure** (`cleanup_case_types`) for automated clean-up.

- Copy the full script into your SQL client (e.g. DBeaver).  
- Execute it as **one transaction** — this will **create and persist** the stored procedure in the database.  
- Once created, you can call it any time for future clean-ups.

> 💻 **Run command:**
> ```sql
> CALL cleanup_case_types(2000, 3);
> ```
> This executes the stored procedure with a **batch size of 2000**, and **data deletion of 3 months** and older 
> Each iteration deletes records in batches of 2000 rows.  
> If any record in a batch fails to delete, the process automatically falls back to **record-by-record deletion** for reliability.

> 🕐 **Use Case:**
> - Ideal for scheduled clean-up jobs (cron, automation pipelines).  
> - Can also be triggered manually during maintenance windows.

---

### ✅ Summary

| Script | Type | Execution | Description |
|--------|------|------------|--------------|
| `Remove case_type older than 3 months - Optimised.sql` | Manual | Step-by-step | Cleans data older than 3 months manually |
| `safe_delete_query_case-types_older_than_3_months.sql` | Semi-Automated | Single transaction | Creates and runs helper functions |
| `stored-procedure-safe_delete_query_case-types_older_than_3_months.sql` | Fully Automated | Stored Procedure | Installs SP for reusable automated clean-ups |

---

**Recommendation:**  
Use **Script 3** for production or regular maintenance, as it is idempotent, batched, and safe for repeat execution.
