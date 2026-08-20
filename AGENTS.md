# Agents

Repo-local workflow skills live under `docs/skills/`.

## CaseTypeTab ListElementCode

Use `docs/skills/CaseTypeTab_ListElementCode/SKILL.md` when working on CCD-6251 CaseTypeTab `ListElementCode` support.

### Trigger Phrases

- "Use CaseTypeTab_ListElementCode"
- "Implement CaseTypeTab ListElementCode"
- "Test CaseTypeTab ListElementCode imports"

### Scope

- Add optional `CaseTypeTab.ListElementCode` parsing.
- Validate populated values only.
- Preserve whole-field behaviour when the column is missing or blank.
- Reject simple-field and collection subfield usage on `CaseTypeTab`.
- Keep Search/Workbasket `ListElementCode` behaviour unchanged.
- Run `F-127 CaseTypeTab ListElementCode` BEFTA import tests.

### Current Test Commands

```bash
./gradlew :aat:highLevelDataSetup --args=local --console=plain | tee /tmp/hlds.log
./gradlew functional -Ptags='@S-127.1 or @S-127.2'
```
