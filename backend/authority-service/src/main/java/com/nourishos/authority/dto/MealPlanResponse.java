package com.nourishos.authority.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nourishos.authority.domain.MealPlan;
import com.nourishos.authority.domain.MealPlanCandidate;
import com.nourishos.authority.domain.ServingProfile;
import lombok.Data;

@Data
public class MealPlanResponse {

    private UUID id;
    private UUID mealRequestId;
    private UUID selectedMealOptionId;
    private int servings;
    private Instant plannedTime;
    private BigDecimal proteinScoreSnapshot;
    private BigDecimal wasteScoreSnapshot;
    private BigDecimal reliabilityScoreSnapshot;
    private List<CandidateDto> candidates;
    private ServingProfileDto servingProfile;

    @Data
    public static class CandidateDto {
        private UUID id;
        private UUID mealOptionId;
        private BigDecimal compositeScore;
        private String scoreBreakdown;

        public static CandidateDto from(MealPlanCandidate c) {
            CandidateDto d = new CandidateDto();
            d.setId(c.getId());
            d.setMealOptionId(c.getMealOption().getId());
            d.setCompositeScore(c.getCompositeScore());
            d.setScoreBreakdown(c.getScoreBreakdown());
            return d;
        }
    }

    @Data
    public static class ServingProfileDto {
        private UUID id;
        private int householdSize;
        private String participatingMemberIds;
        private BigDecimal scalingFactor;

        public static ServingProfileDto from(ServingProfile sp) {
            ServingProfileDto d = new ServingProfileDto();
            d.setId(sp.getId());
            d.setHouseholdSize(sp.getHouseholdSize());
            d.setParticipatingMemberIds(sp.getParticipatingMemberIds());
            d.setScalingFactor(sp.getScalingFactor());
            return d;
        }
    }

    public static MealPlanResponse from(MealPlan plan, List<MealPlanCandidate> candidates,
                                         ServingProfile servingProfile) {
        MealPlanResponse r = new MealPlanResponse();
        r.setId(plan.getId());
        r.setMealRequestId(plan.getMealRequest().getId());
        r.setSelectedMealOptionId(plan.getSelectedMealOption() != null
                ? plan.getSelectedMealOption().getId() : null);
        r.setServings(plan.getServings());
        r.setPlannedTime(plan.getPlannedTime());
        r.setProteinScoreSnapshot(plan.getProteinScoreSnapshot());
        r.setWasteScoreSnapshot(plan.getWasteScoreSnapshot());
        r.setReliabilityScoreSnapshot(plan.getReliabilityScoreSnapshot());
        r.setCandidates(candidates.stream().map(CandidateDto::from).toList());
        r.setServingProfile(servingProfile != null ? ServingProfileDto.from(servingProfile) : null);
        return r;
    }
}
