"""
Weekly planner — assigns meals to 7-day slots.
Perishables first (days 1-2), protein balance across week, member dietary fit per slot.
FastAPI proposes; Spring Boot persists on user confirm.
"""

from dataclasses import dataclass, field
from typing import List, Optional, Set, Dict
from uuid import UUID
from decimal import Decimal
from datetime import date, timedelta


@dataclass
class IngredientInfo:
    ingredient_id: UUID
    base_quantity: Decimal
    unit: str
    optional: bool = False
    perishability_class: Optional[str] = None  # HIGHLY_PERISHABLE, PERISHABLE, SHELF_STABLE


@dataclass
class MealCandidate:
    meal_id: UUID
    meal_name: str
    meal_type: str  # BREAKFAST, LUNCH, DINNER, SNACK
    estimated_protein_grams: Decimal = Decimal("0")
    ingredient_infos: List[IngredientInfo] = field(default_factory=list)
    composite_score: float = 0.0
    allergens: Set[UUID] = field(default_factory=set)
    tags: Set[str] = field(default_factory=set)


@dataclass
class MemberProfile:
    member_id: UUID
    display_name: str
    dietary_restrictions: Set[str] = field(default_factory=set)  # e.g., {"vegetarian", "gluten-free"}
    allergen_ingredient_ids: Set[UUID] = field(default_factory=set)
    protein_target: Optional[Decimal] = None


@dataclass
class SlotAssignment:
    day: int  # 1-7
    day_of_week: str  # MONDAY, TUESDAY, ...
    meal_type: str
    meal_id: UUID
    meal_name: str
    estimated_protein_grams: Decimal
    is_perishable_priority: bool = False


@dataclass
class WeeklyPlanResult:
    household_id: UUID
    week_start_date: date
    slots: List[SlotAssignment] = field(default_factory=list)
    missing_ingredients: List[Dict] = field(default_factory=list)
    member_fit_summary: Dict[str, Dict] = field(default_factory=dict)


DAYS_OF_WEEK = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]
MEAL_TYPES = ["BREAKFAST", "LUNCH", "DINNER"]


def _is_highly_perishable(meal: MealCandidate) -> bool:
    return any(
        ing.perishability_class == "HIGHLY_PERISHABLE"
        for ing in meal.ingredient_infos
    )


def _member_can_eat(meal: MealCandidate, member: MemberProfile) -> bool:
    # Check allergens
    if member.allergen_ingredient_ids & meal.allergens:
        return False
    # Check dietary restrictions
    if "vegetarian" in member.dietary_restrictions and "meat" in meal.tags:
        return False
    if "vegan" in member.dietary_restrictions and ("meat" in meal.tags or "dairy" in meal.tags):
        return False
    if "gluten-free" in member.dietary_restrictions and "gluten" in meal.tags:
        return False
    return True


def _all_members_can_eat(meal: MealCandidate, members: List[MemberProfile]) -> bool:
    return all(_member_can_eat(meal, m) for m in members)


def plan_weekly_slots(
    household_id: UUID,
    week_start_date: date,
    candidates: List[MealCandidate],
    members: List[MemberProfile],
    meal_types: Optional[List[str]] = None,
) -> WeeklyPlanResult:
    """
    Assign meals to 7-day slots.
    - Perishable meals scheduled in days 1-2
    - Protein balanced across the week
    - All members' dietary restrictions respected per slot
    """
    if meal_types is None:
        meal_types = MEAL_TYPES

    result = WeeklyPlanResult(
        household_id=household_id,
        week_start_date=week_start_date,
    )

    # Filter candidates to only those all members can eat
    compatible = [c for c in candidates if _all_members_can_eat(c, members)]

    # Separate perishable and non-perishable candidates
    perishable = [c for c in compatible if _is_highly_perishable(c)]
    non_perishable = [c for c in compatible if not _is_highly_perishable(c)]

    # Sort by composite score descending
    perishable.sort(key=lambda c: c.composite_score, reverse=True)
    non_perishable.sort(key=lambda c: c.composite_score, reverse=True)

    used_meal_ids: Set[UUID] = set()
    daily_protein: Dict[int, Decimal] = {d: Decimal("0") for d in range(1, 8)}

    def _pick_meal(pool: List[MealCandidate], meal_type: str, day: int) -> Optional[MealCandidate]:
        """Pick the best unused meal of the right type, preferring lower-protein days for balance."""
        best = None
        best_idx = -1
        for i, c in enumerate(pool):
            if c.meal_id in used_meal_ids:
                continue
            if c.meal_type != meal_type:
                continue
            if best is None:
                best = c
                best_idx = i
            # Prefer meal that balances protein: if current day is below avg, pick higher protein
            elif daily_protein[day] < sum(daily_protein.values()) / 7:
                if c.estimated_protein_grams > best.estimated_protein_grams:
                    best = c
                    best_idx = i
            else:
                if c.composite_score > best.composite_score:
                    best = c
                    best_idx = i
        return best

    # Phase 1: Assign perishable meals to days 1-2
    for day in range(1, 3):
        for meal_type in meal_types:
            meal = _pick_meal(perishable, meal_type, day)
            if meal:
                used_meal_ids.add(meal.meal_id)
                daily_protein[day] += meal.estimated_protein_grams
                result.slots.append(SlotAssignment(
                    day=day,
                    day_of_week=DAYS_OF_WEEK[day - 1],
                    meal_type=meal_type,
                    meal_id=meal.meal_id,
                    meal_name=meal.meal_name,
                    estimated_protein_grams=meal.estimated_protein_grams,
                    is_perishable_priority=True,
                ))

    # Phase 2: Fill remaining slots from non-perishable + leftover perishable
    remaining_pool = non_perishable + [p for p in perishable if p.meal_id not in used_meal_ids]
    remaining_pool.sort(key=lambda c: c.composite_score, reverse=True)

    for day in range(1, 8):
        for meal_type in meal_types:
            # Skip if already assigned (perishable phase)
            if any(s.day == day and s.meal_type == meal_type for s in result.slots):
                continue
            meal = _pick_meal(remaining_pool, meal_type, day)
            if meal:
                used_meal_ids.add(meal.meal_id)
                daily_protein[day] += meal.estimated_protein_grams
                result.slots.append(SlotAssignment(
                    day=day,
                    day_of_week=DAYS_OF_WEEK[day - 1],
                    meal_type=meal_type,
                    meal_id=meal.meal_id,
                    meal_name=meal.meal_name,
                    estimated_protein_grams=meal.estimated_protein_grams,
                ))

    # Sort slots by day then meal type
    type_order = {t: i for i, t in enumerate(meal_types)}
    result.slots.sort(key=lambda s: (s.day, type_order.get(s.meal_type, 99)))

    # Build member fit summary
    for member in members:
        compliant = 0
        total = len(result.slots)
        for slot in result.slots:
            assigned = next((c for c in candidates if c.meal_id == slot.meal_id), None)
            if assigned and _member_can_eat(assigned, member):
                compliant += 1
        result.member_fit_summary[str(member.member_id)] = {
            "display_name": member.display_name,
            "compliant_slots": compliant,
            "total_slots": total,
            "compliance_rate": compliant / total if total > 0 else 0.0,
        }

    return result
