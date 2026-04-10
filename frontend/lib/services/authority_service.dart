import 'package:dio/dio.dart';
import '../config/environment.dart';
import '../models/household.dart';
import 'service_exception.dart';

class AuthorityService {
  static final AuthorityService _instance = AuthorityService._internal();
  factory AuthorityService() => _instance;

  late final Dio _dio;

  AuthorityService._internal() {
    _dio = Dio(BaseOptions(
      baseUrl: Environment.authorityBaseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));
  }

  Future<Household> getHousehold(String id) async {
    try {
      final response = await _dio.get('/households/$id');
      return Household.fromJson(response.data);
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch household',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<List<HouseholdMember>> getMembers(String householdId) async {
    try {
      final response = await _dio.get('/households/$householdId/members');
      return (response.data as List)
          .map((m) => HouseholdMember.fromJson(m))
          .toList();
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch members',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<List<dynamic>> getExpiringItems() async {
    try {
      final response = await _dio.get('/inventory/expiring');
      return response.data as List;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch expiring items',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<List<dynamic>> getLowStockItems() async {
    try {
      final response = await _dio.get('/inventory/low-stock');
      return response.data as List;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch low stock items',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<Map<String, dynamic>> getCurrentSchedule(String householdId) async {
    try {
      final response = await _dio.get('/planning/schedules/$householdId/current');
      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch schedule',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<void> updatePreferences(String householdId, String memberId, Map<String, dynamic> prefs) async {
    try {
      await _dio.patch('/households/$householdId/members/$memberId/preferences', data: prefs);
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to update preferences',
        statusCode: e.response?.statusCode,
      );
    }
  }
}
