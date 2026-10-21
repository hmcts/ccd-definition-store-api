package uk.gov.hmcts.net.ccd.definition.store.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@TestPropertySource(properties = {"ccd.authorised.services=ccd_data"})
@Transactional
class CaseTypesOrderImportTest extends BaseTest {

    private static final String SAMPLE = "/CCD_TestDefinition_CaseTypesOrder.xlsx";
    private static final String CASE_TYPE = "TestAddressBookCase";
    private static final String SECOND_CASE_TYPE = "TestComplexAddressBookCase";

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<UUID> importJobIds = new ArrayList<>();

    @AfterEach
    void cleanImportJobs() {
        // Import jobs commit independently of the test transaction and must be deleted independently too.
        TransactionTemplate cleanup = new TransactionTemplate(transactionManager);
        cleanup.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        cleanup.executeWithoutResult(status -> importJobIds.forEach(
            id -> jdbcTemplate.update("DELETE FROM import_jobs WHERE id = ?", id)));
    }

    @Test
    void shouldImportSampleWithPopulatedAndMissingCaseTypesOrders() throws Exception {
        assertSuccessfulImport(readWorkbook(SAMPLE));

        assertThat(persistedOrder(CASE_TYPE)).isEqualTo(42);
        assertThat(persistedOrder(SECOND_CASE_TYPE)).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 42, 99})
    void shouldPersistNumericCaseTypesOrder(int order) throws Exception {
        assertSuccessfulImport(workbookWithOrder(order));

        assertThat(persistedOrder(CASE_TYPE)).isEqualTo(order);
        assertThat(persistedOrder(SECOND_CASE_TYPE)).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldImportAllBlankCaseTypesOrders(String order) throws Exception {
        assertSuccessfulImport(workbookWithOrder(order));

        assertThat(persistedOrder(CASE_TYPE)).isNull();
        assertThat(persistedOrder(SECOND_CASE_TYPE)).isNull();
    }

    @Test
    void shouldImportLegacyWorkbookWithoutCaseTypesOrderColumn() throws Exception {
        assertSuccessfulImport(readWorkbook(EXCEL_FILE_CCD_DEFINITION));

        assertThat(persistedOrder(CASE_TYPE)).isNull();
        assertThat(persistedOrder(SECOND_CASE_TYPE)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "-1", "100", "1.5", "2147483648"})
    void shouldRejectInvalidOrderWithoutCreatingCaseTypeVersion(String order) throws Exception {
        assertInvalidOrderDoesNotCreateVersion(order);
    }

    @Test
    void shouldRejectNumericFractionEvenWhenCellFormatRoundsToWholeNumber() throws Exception {
        assertInvalidOrderDoesNotCreateVersion(1.5);
    }

    private void assertInvalidOrderDoesNotCreateVersion(Object order) throws Exception {
        assertSuccessfulImport(readWorkbook(SAMPLE));
        List<Map<String, Object>> before = persistedCaseTypes();

        MockHttpServletResponse response = importWorkbook(workbookWithOrder(order));

        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains(
            "CaseType", "CaseTypesOrder", CASE_TYPE, order.toString());
        assertThat(persistedCaseTypes()).isEqualTo(before);
    }

    private Integer persistedOrder(String reference) {
        return jdbcTemplate.queryForObject(
            "SELECT case_types_order FROM case_type WHERE reference = ? ORDER BY version DESC LIMIT 1",
            Integer.class, reference);
    }

    private List<Map<String, Object>> persistedCaseTypes() {
        return jdbcTemplate.queryForList("SELECT reference, version, case_types_order FROM case_type ORDER BY id");
    }

    private void assertSuccessfulImport(byte[] workbook) throws Exception {
        stubFor(put(urlEqualTo("/dictionary")).willReturn(ok()));
        MockHttpServletResponse response = importWorkbook(workbook);
        assertThat(response.getStatus()).as(response.getContentAsString()).isEqualTo(201);
    }

    private MockHttpServletResponse importWorkbook(byte[] workbook) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", workbook);
        MockHttpServletResponse response = mockMvc.perform(multipart(IMPORT_URL).file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn().getResponse();
        String jobId = response.getHeader(ImportController.IMPORT_JOB_ID_HEADER);
        if (jobId != null) {
            importJobIds.add(UUID.fromString(jobId));
        }
        return response;
    }

    private byte[] readWorkbook(String resource) throws Exception {
        try (InputStream input = new ClassPathResource(resource, getClass()).getInputStream()) {
            return input.readAllBytes();
        }
    }

    private byte[] workbookWithOrder(Object value) throws Exception {
        try (InputStream input = new ClassPathResource(SAMPLE, getClass()).getInputStream();
             XSSFWorkbook workbook = new XSSFWorkbook(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Row row = workbook.getSheet("CaseType").getRow(3);
            Cell cell = row.getCell(9);
            if (value == null) {
                row.removeCell(cell);
            } else if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
                CellStyle style = workbook.createCellStyle();
                style.setDataFormat(workbook.createDataFormat().getFormat("0"));
                cell.setCellStyle(style);
            } else {
                cell.setCellValue(value.toString());
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
