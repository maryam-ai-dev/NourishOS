package com.nourishos.authority.service.planning;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nourishos.authority.domain.Household;
import com.nourishos.authority.domain.MealConstraint;
import com.nourishos.authority.domain.MealRequest;
import com.nourishos.authority.domain.RequestType;
import com.nourishos.authority.repository.MealConstraintRepository;
import com.nourishos.authority.repository.MealRequestRepository;
import com.nourishos.authority.service.HouseholdService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealRequestServiceTest {

    @Mock private MealRequestRepository mealRequestRepository;
    @Mock private MealConstraintRepository mealConstraintRepository;
    @Mock private HouseholdService householdService;

    @InjectMocks private MealRequestService service;

    private MealConstraint makeConstraint(int servings) {
        MealConstraint c = new MealConstraint();
        c.setServings(servings);
        return c;
    }

    @Test
    void servingsZeroThrows() {
        UUID hhId = UUID.randomUUID();
        when(householdService.findById(hhId)).thenReturn(new Household());

        assertThrows(InvalidServingsException.class,
                () -> service.createRequest(hhId, RequestType.TONIGHT, makeConstraint(0)));
    }

    @Test
    void servingsNegativeThrows() {
        UUID hhId = UUID.randomUUID();
        when(householdService.findById(hhId)).thenReturn(new Household());

        assertThrows(InvalidServingsException.class,
                () -> service.createRequest(hhId, RequestType.TONIGHT, makeConstraint(-1)));
    }

    @Test
    void duplicateWithinFiveMinutesThrows() {
        UUID hhId = UUID.randomUUID();
        MealRequest existing = new MealRequest();
        existing.setId(UUID.randomUUID());

        when(householdService.findById(hhId)).thenReturn(new Household());
        when(mealRequestRepository.findFirstByHouseholdIdAndRequestTypeAndRequestedAtAfter(
                eq(hhId), eq(RequestType.TONIGHT), any()))
                .thenReturn(Optional.of(existing));

        DuplicateMealRequestException ex = assertThrows(DuplicateMealRequestException.class,
                () -> service.createRequest(hhId, RequestType.TONIGHT, makeConstraint(2)));

        assertEquals(existing.getId(), ex.getExistingRequestId());
    }

    @Test
    void validRequestCreatesSuccessfully() {
        UUID hhId = UUID.randomUUID();
        when(householdService.findById(hhId)).thenReturn(new Household());
        when(mealRequestRepository.findFirstByHouseholdIdAndRequestTypeAndRequestedAtAfter(
                eq(hhId), eq(RequestType.TONIGHT), any()))
                .thenReturn(Optional.empty());
        when(mealRequestRepository.save(any())).thenAnswer(i -> {
            MealRequest r = i.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(mealConstraintRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MealRequest result = service.createRequest(hhId, RequestType.TONIGHT, makeConstraint(4));

        assertNotNull(result.getId());
        verify(mealRequestRepository).save(any());
        verify(mealConstraintRepository).save(any());
    }
}
