package uk.gov.hmcts.ccd.definition.store.domain.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.category.CategoryTabServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.CategoryTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;

import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CategoryTabServiceImplTest {

    @Mock
    private CategoryTabRepository categoryTabRepository;
    private CategoryTabServiceImpl categoryTabServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.categoryTabServiceImpl = new CategoryTabServiceImpl(categoryTabRepository);
    }

    @DisplayName("should save all CategoryEntity")
    @Test
    void testSaveAll() {
        val entities = Arrays.asList(
            new CategoryEntity(), new CategoryEntity());

        when(categoryTabRepository.saveAll(entities)).thenReturn(entities);
        categoryTabServiceImpl.saveAll(entities);
        verify(categoryTabRepository).saveAll(entities);
    }
}
