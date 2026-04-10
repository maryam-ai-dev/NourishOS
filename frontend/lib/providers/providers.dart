import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/authority_service.dart';
import '../services/intelligence_service.dart';
import '../services/service_exception.dart';
import '../models/household.dart';

// Hardcoded for now — in production, set from login/onboarding
const _defaultHouseholdId = '';

final householdIdProvider = StateProvider<String>((ref) => _defaultHouseholdId);

final householdProvider = FutureProvider.family<Household, String>((ref, id) async {
  try {
    return await AuthorityService().getHousehold(id);
  } on ServiceException {
    rethrow;
  }
});

final membersProvider = FutureProvider.family<List<HouseholdMember>, String>((ref, householdId) async {
  try {
    return await AuthorityService().getMembers(householdId);
  } on ServiceException {
    rethrow;
  }
});

final inventoryExpiringProvider = FutureProvider<List<dynamic>>((ref) async {
  try {
    return await AuthorityService().getExpiringItems();
  } on ServiceException {
    rethrow;
  }
});

final inventoryLowStockProvider = FutureProvider<List<dynamic>>((ref) async {
  try {
    return await AuthorityService().getLowStockItems();
  } on ServiceException {
    rethrow;
  }
});

final mealPlanProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  try {
    return await AuthorityService().getCurrentSchedule(householdId);
  } on ServiceException {
    rethrow;
  }
});

final executionProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  // Placeholder — execution state fetched from Spring Boot which enriches with Redis
  return {'status': 'IDLE'};
});

final foodflowProvider = FutureProvider.family<Map<String, dynamic>, String>((ref, householdId) async {
  try {
    return await IntelligenceService().analyzeFoodFlow(householdId);
  } on ServiceException {
    rethrow;
  }
});
