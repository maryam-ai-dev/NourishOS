import 'package:dio/dio.dart';
import '../config/environment.dart';
import 'service_exception.dart';

class IntelligenceService {
  static final IntelligenceService _instance = IntelligenceService._internal();
  factory IntelligenceService() => _instance;

  late final Dio _dio;

  IntelligenceService._internal() {
    _dio = Dio(BaseOptions(
      baseUrl: Environment.intelligenceBaseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
    ));
  }

  Future<Map<String, dynamic>> rankMeals(Map<String, dynamic> request) async {
    try {
      final response = await _dio.post('/recommendation/rank', data: request);
      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to rank meals',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<Map<String, dynamic>> analyzeFoodFlow(String householdId) async {
    try {
      final response = await _dio.post('/foodflow/analyze', data: {
        'householdId': householdId,
      });
      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to analyze food flow',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<Map<String, dynamic>> planWeekly(Map<String, dynamic> request) async {
    try {
      final response = await _dio.post('/planning/weekly', data: request);
      return response.data as Map<String, dynamic>;
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to plan weekly meals',
        statusCode: e.response?.statusCode,
      );
    }
  }

  Future<List<dynamic>> getMealReliability(String householdId, List<String> mealOptionIds) async {
    try {
      final response = await _dio.post('/foodflow/meal-reliability', data: {
        'householdId': householdId,
        'mealOptionIds': mealOptionIds,
      });
      return (response.data['reliability'] as List?) ?? [];
    } on DioException catch (e) {
      throw ServiceException(
        e.message ?? 'Failed to fetch meal reliability',
        statusCode: e.response?.statusCode,
      );
    }
  }
}
