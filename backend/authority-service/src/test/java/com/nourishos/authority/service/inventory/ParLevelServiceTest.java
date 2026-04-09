package com.nourishos.authority.service.inventory;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.Ingredient;
import com.nourishos.authority.domain.ParLevel;
import com.nourishos.authority.domain.PerishabilityClass;
import com.nourishos.authority.repository.IngredientRepository;
import com.nourishos.authority.repository.ParLevelRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParLevelServiceTest {

    @Mock
    private ParLevelRepository parLevelRepository;

    @Mock
    private IngredientService ingredientService;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private ParLevelService service;

    private Ingredient ingredient;
    private UUID ingredientId;

    @BeforeEach
    void setUp() {
        ingredientId = UUID.randomUUID();
        ingredient = new Ingredient();
        ingredient.setId(ingredientId);
        ingredient.setName("Flour");
        ingredient.setDefaultUnit("g");
        ingredient.setCategory("dry_goods");
        ingredient.setPerishabilityClass(PerishabilityClass.SHELF_STABLE);
    }

    @Test
    void validParLevelCreatesSuccessfully() {
        ParLevel pl = makeParLevel(new BigDecimal("1000"), new BigDecimal("200"), "g");
        when(ingredientService.findById(ingredientId)).thenReturn(ingredient);
        when(unitConversionService.areCompatible("g", "g")).thenReturn(true);
        when(parLevelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ParLevel result = service.create(pl);
        assertNotNull(result);
        verify(parLevelRepository).save(pl);
    }

    @Test
    void minimumAbovePreferredThrows() {
        ParLevel pl = makeParLevel(new BigDecimal("200"), new BigDecimal("500"), "g");

        assertThrows(InvalidParLevelException.class, () -> service.create(pl));
        verify(parLevelRepository, never()).save(any());
    }

    @Test
    void incompatibleUnitThrows() {
        ParLevel pl = makeParLevel(new BigDecimal("1000"), new BigDecimal("200"), "ml");
        when(ingredientService.findById(ingredientId)).thenReturn(ingredient);
        when(unitConversionService.areCompatible("ml", "g")).thenReturn(false);

        assertThrows(InvalidParLevelException.class, () -> service.create(pl));
        verify(parLevelRepository, never()).save(any());
    }

    @Test
    void equalMinAndPreferredAllowed() {
        ParLevel pl = makeParLevel(new BigDecimal("500"), new BigDecimal("500"), "g");
        when(ingredientService.findById(ingredientId)).thenReturn(ingredient);
        when(unitConversionService.areCompatible("g", "g")).thenReturn(true);
        when(parLevelRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> service.create(pl));
    }

    private ParLevel makeParLevel(BigDecimal preferred, BigDecimal minimum, String unit) {
        ParLevel pl = new ParLevel();
        pl.setIngredient(ingredient);
        pl.setPreferredQuantity(preferred);
        pl.setMinimumQuantity(minimum);
        pl.setUnit(unit);
        return pl;
    }
}
