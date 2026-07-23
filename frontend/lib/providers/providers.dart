import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/household.dart';

// Demo deployment: no backend attached, so every provider below returns
// canned data instead of calling AuthorityService/IntelligenceService.
// Swap these bodies back to the *Service() calls to reconnect a real backend.
const _defaultHouseholdId = 'demo-household';

final householdIdProvider = StateProvider<String>((ref) => _defaultHouseholdId);

final householdProvider = FutureProvider.family<Household, String>((ref, id) async {
  await Future.delayed(const Duration(milliseconds: 300));
  return Household(id: id, name: 'The Yousuf Home', weeklyBudgetLimit: 80);
});

final membersProvider = FutureProvider.family<List<HouseholdMember>, String>((ref, householdId) async {
  await Future.delayed(const Duration(milliseconds: 300));
  return [
    HouseholdMember(id: 'm1', householdId: householdId, displayName: 'Maryam', ageGroup: 'ADULT', effortSensitivity: 'LOW'),
    HouseholdMember(id: 'm2', householdId: householdId, displayName: 'Ahmed', ageGroup: 'ADULT', effortSensitivity: 'MEDIUM'),
    HouseholdMember(id: 'm3', householdId: householdId, displayName: 'Zara', ageGroup: 'CHILD', effortSensitivity: 'HIGH'),
  ];
});

final inventoryExpiringProvider = FutureProvider<List<dynamic>>((ref) async {
  await Future.delayed(const Duration(milliseconds: 250));
  return [
    {'id': 'e1', 'ingredientName': 'Spinach', 'expiresAt': '2026-07-23'},
    {'id': 'e2', 'ingredientName': 'Chicken Breast', 'expiresAt': '2026-07-24'},
  ];
});

final inventoryLowStockProvider = FutureProvider<List<dynamic>>((ref) async {
  await Future.delayed(const Duration(milliseconds: 250));
  return [
    {'id': 'l1', 'ingredientName': 'Rice'},
    {'id': 'l2', 'ingredientName': 'Olive Oil'},
    {'id': 'l3', 'ingredientName': 'Milk'},
  ];
});

final mealPlanProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  await Future.delayed(const Duration(milliseconds: 300));
  return {'householdId': householdId, 'slots': demoWeeklySlots()};
});

final executionProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  return {'status': 'IDLE'};
});

final foodflowProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  await Future.delayed(const Duration(milliseconds: 300));
  return {
    'householdId': householdId,
    'wasteRatio': 0.15,
    'topWastedIngredients': ['Spinach', 'Bread', 'Yogurt'],
  };
});

/// Shared by mealPlanProvider and the weekly planner's "generate" action.
///
/// Most slots are one shared household meal. A handful carry a `perMember`
/// override so the "Everyone / Maryam / Ahmed / Zara" filter on the planner
/// screen has something real to show — e.g. Zara gets a nut-free swap,
/// Ahmed gets an extra-spicy version, Maryam gets a protein-boosted one.
List<Map<String, dynamic>> demoWeeklySlots({bool shuffle = false}) {
  const meals = [
    {'name': 'Grilled Chicken & Rice', 'emoji': '🍗'},
    {'name': 'Pasta Primavera', 'emoji': '🍝'},
    {'name': 'Greek Salad', 'emoji': '🥗'},
    {'name': 'Chicken Biryani', 'emoji': '🥘'},
    {'name': 'Veggie Stir Fry', 'emoji': '🥦'},
    {'name': 'Overnight Oats', 'emoji': '🥣'},
    {'name': 'Avocado Toast', 'emoji': '🥑'},
    {'name': 'Lentil Soup', 'emoji': '🍲'},
  ];

  // (day, mealType) -> {member: {mealName, emoji, reason}}
  final overrides = <String, Map<String, Map<String, String>>>{
    '2-DINNER': {'Zara': {'mealName': 'Nut-Free Chicken Biryani', 'emoji': '🍛', 'reason': 'Nut allergy swap'}},
    '4-LUNCH': {'Ahmed': {'mealName': 'Extra-Spicy Stir Fry', 'emoji': '🌶️', 'reason': 'Loves spicy'}},
    '5-DINNER': {'Maryam': {'mealName': 'Protein Lentil Bowl', 'emoji': '🍲', 'reason': 'High protein, vegetarian'}},
    '6-BREAKFAST': {'Zara': {'mealName': 'Pasta Fan Pancakes', 'emoji': '🥞', 'reason': "Zara's pasta-fan pick"}},
  };

  final slots = <Map<String, dynamic>>[];
  var i = shuffle ? DateTime.now().millisecond % meals.length : 0;
  for (var day = 1; day <= 7; day++) {
    for (final mealType in ['BREAKFAST', 'LUNCH', 'DINNER']) {
      final meal = meals[i % meals.length];
      i++;
      slots.add({
        'day': day,
        'mealType': mealType,
        'mealName': meal['name'],
        'emoji': meal['emoji'],
        'overrides': overrides['$day-$mealType'] ?? const {},
      });
    }
  }
  return slots;
}
