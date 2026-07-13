# CaseTypeTab ListElementCode

## Requirement

Add support for specifying a subfield of a complex case field to be displayed on its own within Case View screens on ExUI.

Currently, `CaseTypeTab` can reference a full `CaseFieldID`. If that field is complex, the full complex object is displayed. The new `ListElementCode` column allows a definition author to target one subfield path under that complex field.

Example:

| CaseFieldID | ListElementCode |
|---|---|
| Applicant | Address.Postcode |

This means ExUI should display only `Applicant.Address.Postcode`, not the full `Applicant` complex object.

## CaseTypeTab Column

Add `ListElementCode` to `CaseTypeTab`, positioned next to `CaseFieldID`.

Expected order:

| ... | CaseFieldID | ListElementCode | TabFieldDisplayOrder | ... |
|---|---|---|---|---|

## Behaviour

| Scenario | Expected behaviour |
|---|---|
| `CaseFieldID` is complex and `ListElementCode` is a valid subfield path | Definition imports successfully |
| `CaseFieldID` is complex and `ListElementCode` is not a valid subfield path | Definition import fails |
| `ListElementCode` is blank | Existing behaviour is preserved; the whole `CaseFieldID` field is targeted |
| `ListElementCode` column is missing from an older definition file | Existing behaviour is preserved; the whole `CaseFieldID` field is targeted |
| `CaseFieldID` is simple and `ListElementCode` is populated | Definition import fails because subfields are only valid for complex fields |
| `CaseFieldID` is complex with nested complex fields and `ListElementCode` is a valid nested path | Definition imports successfully |
| `CaseFieldID` is a collection and `ListElementCode` is populated | Definition import fails because CaseTypeTab subfield resolution is not supported for collections |

## Clarification Required

The requirement defines the spreadsheet column name, but not the CaseTypeTab API response property consumed by ExUI.

Proposed ExUI question:

```text
Can we confirm the ExUI/API contract for the new CaseTypeTab subfield path?

The CCD definition spreadsheet column is `ListElementCode`, but it represents a subfield path under `CaseFieldID`.

Example:

| CaseFieldID | ListElementCode |
|---|---|
| Applicant | Address.Postcode |

Proposed implementation:

| Layer | Name |
|---|---|
| Spreadsheet column | `ListElementCode` |
| Java/internal field | `caseFieldElementPath` |
| DB column | `case_field_element_path` |
| CaseTypeTab API response | `case_field_element_path` |

Can ExUI consume `case_field_element_path` in the CaseTypeTab response, consistent with existing Search/Workbasket models, or does it expect the API property to be `list_element_code`?
```

## Implementation Scope

Definition Store implementation:

| Area | Required change |
|---|---|
| Excel parsing | Read optional `CaseTypeTab.ListElementCode` without breaking older workbooks |
| Persistence | Persist the value against the display group case field |
| Validation | Validate `ListElementCode` as a subfield path relative to `CaseFieldID` |
| API mapping | Return the value to ExUI using the confirmed API property name |
| Backward compatibility | Missing or blank `ListElementCode` keeps existing whole-field behaviour |

Implementation should keep CaseTypeTab-specific collection behaviour separate from Search/Workbasket layout behaviour because existing layout sheets support collection subfield paths, while this feature explicitly rejects them for CaseTypeTab.

## Risks / Backward Compatibility

Risk to normal field behaviour is low if the implementation preserves these rules:

| Risk area | Required safeguard |
|---|---|
| Existing definitions | Missing `ListElementCode` column must keep current whole-field behaviour |
| Blank values | Blank `ListElementCode` must keep current whole-field behaviour |
| Simple fields | Simple fields remain valid when `ListElementCode` is blank |
| Validation scope | Subfield validation only runs when `ListElementCode` is populated |
| Existing layout behaviour | Search/Workbasket `ListElementCode` handling must not be changed |
| Collections | CaseTypeTab collection rejection must not affect existing Search/Workbasket collection support |
| Persistence | Uniqueness/storage must allow valid rows for the same complex `CaseFieldID` with different `ListElementCode` values |
| ExUI contract | API response property name should follow ExUI confirmation before final implementation |

## Test Coverage

Add focused unit/integration coverage for:

| Test area | Coverage |
|---|---|
| Parser | `CaseTypeTab.ListElementCode` is read when present and ignored when missing |
| Validator | Valid complex subfield, invalid subfield, simple field rejection, collection rejection, nested complex path |
| Mapper/API DTO | CaseTypeTab field exposes the persisted subfield path using the confirmed response property |
| Database/schema | Multiple rows for the same `CaseFieldID` with different `ListElementCode` can be stored, while duplicate rows still fail |
| Import/functional | Import valid/invalid workbooks supplied by `ccd-test-definitions` |

## Functional Tests

Add BEFTA-style functional coverage under `aat/src/aat/resources/features`, following the existing import-definition pattern.

Use the same shape as `F-094 NoC ChallengeQuestions`:

Assumed next available feature number: `F-127` and scenario ids `S-127.1` / `S-127.2`. Re-check before creating files in case another feature has taken the number.

<table>
  <thead>
    <tr>
      <th>File</th>
      <th>Purpose</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/F-127.feature</code></td>
      <td>Feature and scenario definitions</td>
    </tr>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/F-127_Definition_Import_Test_Base_data.td.json</code></td>
      <td>Shared <code>/import</code> request base, same pattern as <code>F-094_Definition_Import_Test_Base_data.td.json</code></td>
    </tr>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/S-127.1.td.json</code></td>
      <td>Valid workbook import</td>
    </tr>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/S-127.2.td.json</code></td>
      <td>Invalid workbook import</td>
    </tr>
  </tbody>
</table>

Suggested scenarios:

<table>
  <thead>
    <tr>
      <th>Scenario</th>
      <th>AC covered</th>
      <th>FT case types</th>
      <th>Test data file</th>
      <th>Definition file reference</th>
      <th>Expected response</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Valid CaseTypeTab subfield paths import successfully</td>
      <td>AC1, AC3, AC5</td>
      <td><code>FT_CaseTypeTabSubfield_Valid</code>, <code>FT_CaseTypeTabSubfield_Blank</code>, <code>FT_CaseTypeTabSubfield_Nested</code></td>
      <td><code>S-127.1.td.json</code></td>
      <td><code>localFilePath: build/tmp/definition_files_copy/CaseTypeTab_ListElementCode_Valid.xlsx</code></td>
      <td><code>201 Created</code>, <code>Case Definition data successfully imported</code></td>
    </tr>
    <tr>
      <td>Invalid CaseTypeTab subfield paths fail validation</td>
      <td>AC2, AC4, AC6, AC7</td>
      <td><code>FT_CaseTypeTabSubfield_InvalidPath</code>, <code>FT_CaseTypeTabSubfield_SimpleField</code>, <code>FT_CaseTypeTabSubfield_Collection</code></td>
      <td><code>S-127.2.td.json</code></td>
      <td><code>filePath: uk/gov/hmcts/ccd/test_definitions/invalid/CaseTypeTab_ListElementCode_Invalid.xlsx</code></td>
      <td><code>422 Unprocessable Entity</code>, validation message mentions <code>ListElementCode</code></td>
    </tr>
  </tbody>
</table>

Scenario titles/specs and any workbook comments should include both the AC and FT case type name, for example: `AC1 - FT_CaseTypeTabSubfield_Valid - valid direct complex subfield`.

The valid workbook should cover these case types:

<table>
  <thead>
    <tr>
      <th>Case type</th>
      <th>AC covered</th>
      <th>Suggested comment</th>
      <th>Coverage</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_Valid</code></td>
      <td>AC1</td>
      <td><code>AC1 - FT_CaseTypeTabSubfield_Valid</code></td>
      <td>Valid direct complex subfield, e.g. <code>MySchool.Name</code></td>
    </tr>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_Blank</code></td>
      <td>AC3</td>
      <td><code>AC3 - FT_CaseTypeTabSubfield_Blank</code></td>
      <td>Blank <code>ListElementCode</code> keeps whole-field behaviour</td>
    </tr>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_Nested</code></td>
      <td>AC5</td>
      <td><code>AC5 - FT_CaseTypeTabSubfield_Nested</code></td>
      <td>Valid nested complex subfield, e.g. <code>MySchool.Class.ClassDetails.ClassLocation.Building.Name</code></td>
    </tr>
  </tbody>
</table>

The invalid workbook should cover these case types:

<table>
  <thead>
    <tr>
      <th>Case type</th>
      <th>AC covered</th>
      <th>Suggested comment</th>
      <th>Coverage</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_InvalidPath</code></td>
      <td>AC2</td>
      <td><code>AC2 - FT_CaseTypeTabSubfield_InvalidPath</code></td>
      <td>Invalid subfield path, e.g. <code>MySchool.DoesNotExist</code></td>
    </tr>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_SimpleField</code></td>
      <td>AC4</td>
      <td><code>AC4 - FT_CaseTypeTabSubfield_SimpleField</code></td>
      <td>Simple field with populated <code>ListElementCode</code>, e.g. <code>Homeless.Name</code></td>
    </tr>
    <tr>
      <td><code>FT_CaseTypeTabSubfield_Collection</code></td>
      <td>AC6, AC7</td>
      <td><code>AC6/AC7 - FT_CaseTypeTabSubfield_Collection</code></td>
      <td>Collection field with populated <code>ListElementCode</code>, e.g. <code>MyCompany.Name</code></td>
    </tr>
  </tbody>
</table>

Use the same request/response style as existing import tests such as `S-600.1.td.json` for valid imports and `S-108.1.td.json` for invalid imports.

## Acceptance Criteria Test Files

Add dedicated CCD definition Excel files in `ccd-test-definitions/src/main/resources/uk/gov/hmcts/ccd/test_definitions` for AC testing.
Where possible, use one valid workbook and one invalid workbook, with separate case types inside each workbook to cover the individual scenarios.

The shared test definition files will be available through:

```groovy
testImplementation group: 'com.github.hmcts', name: 'ccd-test-definitions', version: ccdTestDefinitionVersion
```

Temporary implementation version:

```groovy
ccdTestDefinitionVersion = '7.26.5_CCD-6251_subfields'
```

Replace this with the released `ccd-test-definitions` version before merge if the branch version is temporary.

<table>
  <thead>
    <tr>
      <th>AC</th>
      <th>Test definition file</th>
      <th>Suggested case type</th>
      <th>Suggested comment</th>
      <th>Intent</th>
      <th>Expected result</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>AC1</td>
      <td><code>excel/CaseTypeTab_ListElementCode_Valid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_Valid</code></td>
      <td><code>AC1 - FT_CaseTypeTabSubfield_Valid</code></td>
      <td>Valid direct subfield under a complex field</td>
      <td>Import succeeds</td>
    </tr>
    <tr>
      <td>AC3</td>
      <td><code>excel/CaseTypeTab_ListElementCode_Valid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_Blank</code></td>
      <td><code>AC3 - FT_CaseTypeTabSubfield_Blank</code></td>
      <td>Blank value keeps whole-field behaviour</td>
      <td>Import succeeds and targets the whole field</td>
    </tr>
    <tr>
      <td>AC5</td>
      <td><code>excel/CaseTypeTab_ListElementCode_Valid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_Nested</code></td>
      <td><code>AC5 - FT_CaseTypeTabSubfield_Nested</code></td>
      <td>Valid nested path under a complex field</td>
      <td>Import succeeds</td>
    </tr>
    <tr>
      <td>AC2</td>
      <td><code>invalid/CaseTypeTab_ListElementCode_Invalid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_InvalidPath</code></td>
      <td><code>AC2 - FT_CaseTypeTabSubfield_InvalidPath</code></td>
      <td>Invalid path should fail validation</td>
      <td>Import fails: subfield does not exist</td>
    </tr>
    <tr>
      <td>AC4</td>
      <td><code>invalid/CaseTypeTab_ListElementCode_Invalid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_SimpleField</code></td>
      <td><code>AC4 - FT_CaseTypeTabSubfield_SimpleField</code></td>
      <td>Simple fields must not accept subfield paths</td>
      <td>Import fails: subfields are only valid for complex fields</td>
    </tr>
    <tr>
      <td>AC6 / AC7</td>
      <td><code>invalid/CaseTypeTab_ListElementCode_Invalid.xlsx</code></td>
      <td><code>FT_CaseTypeTabSubfield_Collection</code></td>
      <td><code>AC6/AC7 - FT_CaseTypeTabSubfield_Collection</code></td>
      <td>Collection subfield resolution is unsupported for CaseTypeTab</td>
      <td>Import fails with collection-not-supported error</td>
    </tr>
    <tr>
      <td>AC8</td>
      <td>Existing older definition without <code>ListElementCode</code></td>
      <td>Existing legacy case type</td>
      <td><code>AC8 - legacy whole-field behaviour</code></td>
      <td>Older files without the new column remain compatible</td>
      <td>Import succeeds and targets the whole field</td>
    </tr>
    <tr>
      <td>AC9</td>
      <td>Latest valid template or generated definition</td>
      <td>Template/header check</td>
      <td><code>AC9 - CaseTypeTab header includes ListElementCode</code></td>
      <td>Latest template exposes the new column in the right place</td>
      <td><code>ListElementCode</code> appears next to <code>CaseFieldID</code> on <code>CaseTypeTab</code></td>
    </tr>
  </tbody>
</table>
