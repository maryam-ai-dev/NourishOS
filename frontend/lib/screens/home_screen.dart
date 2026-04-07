import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final healthProvider = FutureProvider<String>((ref) async {
  final dio = Dio(BaseOptions(baseUrl: 'http://localhost:8080'));
  try {
    final response = await dio.get('/actuator/health');
    final status = response.data['status'] as String;
    return status;
  } catch (e) {
    return 'unreachable';
  }
});

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final health = ref.watch(healthProvider);

    return Scaffold(
      backgroundColor: const Color(0xFF0B0B0F),
      appBar: AppBar(
        title: const Text('NourishOS'),
        backgroundColor: const Color(0xFF121218),
        foregroundColor: Colors.white,
      ),
      body: Center(
        child: health.when(
          data: (status) => Text(
            'Authority Service: $status',
            style: const TextStyle(color: Colors.white, fontSize: 18),
          ),
          loading: () => const CircularProgressIndicator(),
          error: (err, _) => Text(
            'Error: $err',
            style: const TextStyle(color: Colors.redAccent),
          ),
        ),
      ),
    );
  }
}
