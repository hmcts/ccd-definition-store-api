# CaseTypeTab ListElementCode

## Requirement

Add support for specifying a subfield of a complex case field to be displayed on its own within Case View screens on ExUI.

Currently, `CaseTypeTab` can reference a full `CaseFieldID`. If that field is complex, the full complex object is displayed. The new `ListElementCode` column allows a definition author to target one subfield path under that complex field.

Example:

| CaseFieldID | ListElementCode |
|---|---|
| Applicant | Address.Postcode |

This means ExUI should display as an example only `Applicant.Address.Postcode`, 
not the full `Applicant` complex object.

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

## API Contract

ExUI confirmed the CaseTypeTab API response property should be `caseFieldSubfieldCode`.

| Layer | Name |
|---|---|
| Spreadsheet column | `ListElementCode` |
| Java/internal field | `caseFieldElementPath` |
| DB column | `case_field_element_path` |
| CaseTypeTab API response | `caseFieldSubfieldCode` |

## Implementation Scope

Definition Store implementation:

| Area | Required change |
|---|---|
| Excel parsing | Read optional `CaseTypeTab.ListElementCode` without breaking older workbooks |
| Persistence | Persist the value against the display group case field |
| Validation | Validate `ListElementCode` as a subfield path relative to `CaseFieldID` |
| API mapping | Expose `CaseTypeTabField.caseFieldElementPath` as `caseFieldSubfieldCode` |
| Backward compatibility | Missing or blank `ListElementCode` keeps existing whole-field behaviour |

Implementation should keep CaseTypeTab-specific collection behaviour separate from Search/Workbasket layout behaviour because existing layout sheets support collection subfield paths, while this feature explicitly rejects them for CaseTypeTab.

## Internal Implementation Notes

- `repository/src/main/java/uk/gov/hmcts/ccd/definition/store/repository/entity/DisplayGroupCaseFieldEntity.java` persists `caseFieldElementPath`.
- `excel-importer/src/main/java/uk/gov/hmcts/ccd/definition/store/excel/parser/AbstractDisplayGroupParser.java` reads `CaseTypeTab.ListElementCode` only for `CaseTypeTab`.
- `repository/src/main/resources/db/migration/V20260709_6251__CCD-6251_CaseTypeTab_ListElementCode.sql` adds `display_group_case_field.case_field_element_path`.
- Uniqueness uses partial unique indexes to allow different populated paths for the same `display_group_id` and `case_field_id`, while still rejecting duplicate whole-field rows and duplicate same-path rows case-insensitively.

`CaseTypeTabField` carries `caseFieldElementPath` internally and serializes it as `caseFieldSubfieldCode` for the CaseTypeTab API response.

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
| API response | DTO JSON must expose `caseFieldSubfieldCode`, while DB/internal names remain unchanged |

## Test Coverage

Add focused unit/integration coverage for:

| Test area | Coverage |
|---|---|
| Parser | `CaseTypeTab.ListElementCode` is read when present and ignored when missing |
| Validator | Valid complex subfield, invalid subfield, simple field rejection, collection rejection, nested complex path |
| Mapper/API DTO | Internal mapping to `caseFieldElementPath`; JSON exposure as `caseFieldSubfieldCode` |
| Database/schema | Multiple rows for the same `CaseFieldID` with different `ListElementCode` can be stored, while duplicate rows still fail |
| Import/functional | Import valid/invalid workbooks supplied by `ccd-test-definitions` |
| API functional | After ExUI confirms the JSON property name, call Tab Structure By CaseType and assert CaseTypeTab subfield paths in the response |

## API Response Coverage

With ExUI confirmation, CaseTypeTab API response coverage should assert `caseFieldSubfieldCode`:

<table>
  <thead>
    <tr>
      <th>Layer</th>
      <th>Required coverage</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>DTO serialization</td>
      <td>Expose <code>CaseTypeTabField.caseFieldElementPath</code> as <code>caseFieldSubfieldCode</code>.</td>
    </tr>
    <tr>
      <td>Mapper/unit test</td>
      <td>Assert <code>DisplayGroupCaseFieldEntity.caseFieldElementPath</code> maps into <code>CaseTypeTabField</code>.</td>
    </tr>
    <tr>
      <td>End-to-end FT</td>
      <td>Call Tab Structure By CaseType for <code>FT_CTT_Subfield_Valid</code>, and assert <code>MySchool</code> has <code>Name</code>.</td>
    </tr>
    <tr>
      <td>End-to-end FT</td>
      <td>Call Tab Structure By CaseType for <code>FT_CTT_Subfield_Blank</code>, and assert <code>MySchool</code> is returned as a whole field with no subfield path.</td>
    </tr>
    <tr>
      <td>End-to-end FT</td>
      <td>Call Tab Structure By CaseType for <code>FT_CTT_Subfield_Nested</code>, and assert <code>FamilyDetails</code> has <code>FamilyAddress.Country</code>.</td>
    </tr>
    <tr>
      <td>End-to-end FT</td>
      <td>Call Tab Structure By CaseType for <code>FT_CTT_Subfield_Multiple</code>, and assert <code>MySchool</code> has both <code>Name</code> and <code>Number</code>.</td>
    </tr>
  </tbody>
</table>

## Import Test Plan

`highLevelDataSetup` imports valid setup definitions only. It can confirm `CCD_BEFTA_CTT_LISTELEMENCODE.xlsx` is generated from `valid/CCD_BEFTA_CTT_LISTELEMENCODE` and accepted, but it must not be used for the invalid workbook.

Local Docker check:

```bash
./gradlew :aat:highLevelDataSetup --args=local --console=plain | tee /tmp/hlds.log
```

Check the valid workbook was generated and imported without failure:

```bash
rg "Generated \\[definition_files/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx\\]|Importing definition_files/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx|Couldn't import definition_files/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx|http code" /tmp/hlds.log
```

Pass condition: the log contains the generated/importing lines and does not contain `Couldn't import definition_files/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx`.

Test the invalid workbook the same way existing invalid definition assets are tested, for example `F-106 New CategoryID Column/S-106.6.td.json` imports:

```text
uk/gov/hmcts/ccd/test_definitions/invalid/categories/BEFTA_Master_Definition_Invalid_CategoryID_Text_FieldType.xlsx
```

`CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx` should be tested by a negative BEFTA import scenario that posts:

```text
uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx
```

Expected result: `422 Unprocessable Entity` with validation text proving AC2, AC4, AC6 and AC7 are covered.

The valid workbook is expected to be available as setup data before API assertions run.
F-127 API response scenarios should call Tab Structure By CaseType directly and must not post
`build/tmp/definition_files_copy/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx`.

## Functional Tests

BEFTA coverage lives under `aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode`.

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
      <td>Tab structure API response check for direct CaseTypeTab subfield path</td>
    </tr>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/S-127.2.td.json</code> to <code>S-127.4.td.json</code></td>
      <td>Tab structure API response checks for blank, nested and multiple CaseTypeTab subfield paths</td>
    </tr>
    <tr>
      <td><code>aat/src/aat/resources/features/F-127 CaseTypeTab ListElementCode/S-127.5.td.json</code> to <code>S-127.8.td.json</code></td>
      <td>Invalid workbook imports</td>
    </tr>
  </tbody>
</table>

Current scenarios:

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
      <td>Tab structure API returns direct CaseTypeTab subfield path</td>
      <td>AC1, API response</td>
      <td><code>FT_CTT_Subfield_Valid</code></td>
      <td><code>S-127.1.td.json</code></td>
      <td>Uses tab structure endpoint against prepared setup data; no generated workbook path</td>
      <td><code>caseFieldSubfieldCode</code> is <code>Name</code></td>
    </tr>
    <tr>
      <td>Tab structure API returns blank CaseTypeTab subfield path as whole field</td>
      <td>AC3, API response</td>
      <td><code>FT_CTT_Subfield_Blank</code></td>
      <td><code>S-127.2.td.json</code></td>
      <td>Uses tab structure endpoint against prepared setup data; no generated workbook path</td>
      <td><code>caseFieldSubfieldCode</code> is <code>null</code></td>
    </tr>
    <tr>
      <td>Tab structure API returns nested CaseTypeTab subfield path</td>
      <td>AC5, API response</td>
      <td><code>FT_CTT_Subfield_Nested</code></td>
      <td><code>S-127.3.td.json</code></td>
      <td>Uses tab structure endpoint against prepared setup data; no generated workbook path</td>
      <td><code>caseFieldSubfieldCode</code> is <code>FamilyAddress.Country</code></td>
    </tr>
    <tr>
      <td>Tab structure API returns multiple CaseTypeTab subfield paths</td>
      <td>API response</td>
      <td><code>FT_CTT_Subfield_Multiple</code></td>
      <td><code>S-127.4.td.json</code></td>
      <td>Uses tab structure endpoint against prepared setup data; no generated workbook path</td>
      <td><code>caseFieldSubfieldCode</code> values are <code>Name</code> and <code>Number</code></td>
    </tr>
    <tr>
      <td>Invalid CaseTypeTab subfield path fails validation</td>
      <td>AC2</td>
      <td><code>FT_CTT_Subfield_BadPath</code></td>
      <td><code>S-127.5.td.json</code></td>
      <td><code>filePath: uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>422 Unprocessable Entity</code>, validation message contains <code>Invalid ListElementCode 'DoesNotExist' for case type 'FT_CTT_Subfield_BadPath', case field 'MySchool'</code></td>
    </tr>
    <tr>
      <td>CaseTypeTab <code>ListElementCode</code> on a simple field fails validation</td>
      <td>AC4</td>
      <td><code>FT_CTT_Subfield_Simple</code></td>
      <td><code>S-127.6.td.json</code></td>
      <td><code>filePath: uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>422 Unprocessable Entity</code>, validation message contains <code>ListElementCode 'Name' can be only defined for complex fields. Case Field 'Homeless', case type 'FT_CTT_Subfield_Simple'</code></td>
    </tr>
    <tr>
      <td>CaseTypeTab <code>ListElementCode</code> on a collection field fails validation</td>
      <td>AC6</td>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td><code>S-127.7.td.json</code></td>
      <td><code>filePath: uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>422 Unprocessable Entity</code>, validation message contains <code>ListElementCode 'Name' is not supported for collection fields. Case Field 'MyCompany', case type 'FT_CTT_Subfield_Collection'</code></td>
    </tr>
    <tr>
      <td>CaseTypeTab <code>ListElementCode</code> on a collection complex field fails validation</td>
      <td>AC7</td>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td><code>S-127.8.td.json</code></td>
      <td><code>filePath: uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>422 Unprocessable Entity</code>, validation message contains <code>ListElementCode 'AddressLine1' is not supported for collection fields. Case Field 'CollectionComplexField', case type 'FT_CTT_Subfield_Collection'</code></td>
    </tr>
  </tbody>
</table>

Scenario titles/specs and any workbook comments should include both the AC and FT case type name, for example: `AC1 - FT_CTT_Subfield_Valid - valid direct complex subfield`.

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
      <td><code>FT_CTT_Subfield_Valid</code></td>
      <td>AC1</td>
      <td><code>AC1 - FT_CTT_Subfield_Valid</code></td>
      <td>Valid direct complex subfield, e.g. <code>MySchool.Name</code></td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Multiple</code></td>
      <td>Multiple subfields</td>
      <td><code>FT_CTT_Subfield_Multiple</code></td>
      <td>Multiple valid rows for the same complex <code>CaseFieldID</code> with different <code>ListElementCode</code> values</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Blank</code></td>
      <td>AC3</td>
      <td><code>AC3 - FT_CTT_Subfield_Blank</code></td>
      <td>Blank <code>ListElementCode</code> keeps whole-field behaviour</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Nested</code></td>
      <td>AC5</td>
      <td><code>AC5 - FT_CTT_Subfield_Nested</code></td>
      <td>Valid nested complex subfield, e.g. <code>FamilyDetails.FamilyAddress.Country</code></td>
    </tr>
  </tbody>
</table>

Generated valid workbook rows verified from repo root at
`aat/build/tmp/definition_files_copy/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx`:

<table>
  <thead>
    <tr>
      <th>CaseTypeID</th>
      <th>CaseFieldID</th>
      <th>ListElementCode</th>
      <th>Coverage</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FT_CTT_Subfield_Valid</code></td>
      <td><code>MySchool</code></td>
      <td><code>Name</code></td>
      <td>Valid direct complex path</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Blank</code></td>
      <td><code>MySchool</code></td>
      <td></td>
      <td>Blank value keeps whole-field behaviour</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Nested</code></td>
      <td><code>FamilyDetails</code></td>
      <td><code>FamilyAddress.Country</code></td>
      <td>Valid nested complex path</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Multiple</code></td>
      <td><code>MySchool</code></td>
      <td><code>Name</code>, <code>Number</code></td>
      <td>Multiple valid subfields for the same complex field</td>
    </tr>
  </tbody>
</table>

The same generated valid workbook also contains blank `ListElementCode` whole-field rows for
`FamilyDetails`, `CaseHistory`, `MyCompany` and `CollectionComplexField`.

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
      <td><code>FT_CTT_Subfield_BadPath</code></td>
      <td>AC2</td>
      <td><code>AC2 - FT_CTT_Subfield_BadPath</code></td>
      <td>Invalid subfield path, e.g. <code>MySchool.DoesNotExist</code></td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Simple</code></td>
      <td>AC4</td>
      <td><code>AC4 - FT_CTT_Subfield_Simple</code></td>
      <td>Simple field with populated <code>ListElementCode</code>, e.g. <code>Homeless.Name</code></td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td>AC6, AC7</td>
      <td><code>AC6/AC7 - FT_CTT_Subfield_Collection</code></td>
      <td>Collection fields with populated <code>ListElementCode</code>, e.g. <code>MyCompany.Name</code> and <code>CollectionComplexField.AddressLine1</code></td>
    </tr>
  </tbody>
</table>

Invalid workbook rows verified from `../ccd-test-definitions/src/main/resources/uk/gov/hmcts/ccd/test_definitions/invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx`:

<table>
  <thead>
    <tr>
      <th>CaseTypeID</th>
      <th>CaseFieldID</th>
      <th>ListElementCode</th>
      <th>AC</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>FT_CTT_Subfield_BadPath</code></td>
      <td><code>MySchool</code></td>
      <td><code>DoesNotExist</code></td>
      <td>AC2</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Simple</code></td>
      <td><code>Homeless</code></td>
      <td><code>Name</code></td>
      <td>AC4</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td><code>MyCompany</code></td>
      <td><code>Name</code></td>
      <td>AC6</td>
    </tr>
    <tr>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td><code>CollectionComplexField</code></td>
      <td><code>AddressLine1</code></td>
      <td>AC7</td>
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
ccdTestDefinitionVersion = '7.26.7_CCD-6251-SNAPSHOT1'
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
      <td><code>excel/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx</code></td>
      <td><code>FT_CTT_Subfield_Valid</code></td>
      <td><code>AC1 - FT_CTT_Subfield_Valid</code></td>
      <td>Valid direct subfield under a complex field</td>
      <td>Import succeeds</td>
    </tr>
    <tr>
      <td>AC3</td>
      <td><code>excel/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx</code></td>
      <td><code>FT_CTT_Subfield_Blank</code></td>
      <td><code>AC3 - FT_CTT_Subfield_Blank</code></td>
      <td>Blank value keeps whole-field behaviour</td>
      <td>Import succeeds and targets the whole field</td>
    </tr>
    <tr>
      <td>AC5</td>
      <td><code>excel/CCD_BEFTA_CTT_LISTELEMENCODE.xlsx</code></td>
      <td><code>FT_CTT_Subfield_Nested</code></td>
      <td><code>AC5 - FT_CTT_Subfield_Nested</code></td>
      <td>Valid nested path under a complex field</td>
      <td>Import succeeds</td>
    </tr>
    <tr>
      <td>AC2</td>
      <td><code>invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>FT_CTT_Subfield_BadPath</code></td>
      <td><code>AC2 - FT_CTT_Subfield_BadPath</code></td>
      <td>Invalid path should fail validation</td>
      <td>Import fails: subfield does not exist</td>
    </tr>
    <tr>
      <td>AC4</td>
      <td><code>invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>FT_CTT_Subfield_Simple</code></td>
      <td><code>AC4 - FT_CTT_Subfield_Simple</code></td>
      <td>Simple fields must not accept subfield paths</td>
      <td>Import fails: subfields are only valid for complex fields</td>
    </tr>
    <tr>
      <td>AC6 / AC7</td>
      <td><code>invalid/CCD_BEFTA_CTT_LISTELEMENCODE_invalid.xlsx</code></td>
      <td><code>FT_CTT_Subfield_Collection</code></td>
      <td><code>AC6/AC7 - FT_CTT_Subfield_Collection</code></td>
      <td>Collection subfield resolution is unsupported for CaseTypeTab, including <code>MyCompany.Name</code> and <code>CollectionComplexField.AddressLine1</code></td>
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
