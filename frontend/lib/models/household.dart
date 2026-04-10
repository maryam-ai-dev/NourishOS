class Household {
  final String id;
  final String name;
  final double? weeklyBudgetLimit;

  Household({required this.id, required this.name, this.weeklyBudgetLimit});

  factory Household.fromJson(Map<String, dynamic> json) {
    return Household(
      id: json['id'] as String,
      name: json['name'] as String,
      weeklyBudgetLimit: (json['weeklyBudgetLimit'] as num?)?.toDouble(),
    );
  }
}

class HouseholdMember {
  final String id;
  final String householdId;
  final String displayName;
  final String ageGroup;
  final String? effortSensitivity;
  final bool participatesInMealPlanning;

  HouseholdMember({
    required this.id,
    required this.householdId,
    required this.displayName,
    required this.ageGroup,
    this.effortSensitivity,
    this.participatesInMealPlanning = true,
  });

  factory HouseholdMember.fromJson(Map<String, dynamic> json) {
    return HouseholdMember(
      id: json['id'] as String,
      householdId: json['householdId'] as String,
      displayName: json['displayName'] as String,
      ageGroup: json['ageGroup'] as String,
      effortSensitivity: json['effortSensitivity'] as String?,
      participatesInMealPlanning: json['participatesInMealPlanning'] as bool? ?? true,
    );
  }
}
