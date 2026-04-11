import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import '../services/service_exception.dart';

/// Demo execution plan data. In production, fetched from Spring Boot.
final _demoSteps = [
  {'stepOrder': 1, 'actionType': 'DISPENSE_DRY', 'assignedTo': 'MACHINE', 'status': 'COMPLETE', 'estimatedDurationSeconds': 15},
  {'stepOrder': 2, 'actionType': 'HEAT', 'assignedTo': 'MACHINE', 'status': 'IN_PROGRESS', 'estimatedDurationSeconds': 60},
  {'stepOrder': 3, 'actionType': 'USER_CONFIRM', 'assignedTo': 'USER', 'status': 'PENDING', 'estimatedDurationSeconds': 30},
  {'stepOrder': 4, 'actionType': 'STIR', 'assignedTo': 'MACHINE', 'status': 'PENDING', 'estimatedDurationSeconds': 45},
];

const _demoPlan = {
  'mealName': 'Grilled Chicken & Rice',
  'estimatedDurationSeconds': 150,
  'servings': 4,
  'status': 'IN_PROGRESS',
};

class CookingScreen extends ConsumerStatefulWidget {
  const CookingScreen({super.key});

  @override
  ConsumerState<CookingScreen> createState() => _CookingScreenState();
}

class _CookingScreenState extends ConsumerState<CookingScreen> {
  final List<Map<String, dynamic>> _steps = List.from(_demoSteps);
  final Map<String, dynamic> _plan = Map.from(_demoPlan);
  Timer? _pollTimer;
  Timer? _countdownTimer;
  int _remainingSeconds = 0;
  String? _activeIntervention;

  @override
  void initState() {
    super.initState();
    _startPolling();
    _startCountdown();
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    _countdownTimer?.cancel();
    super.dispose();
  }

  void _startPolling() {
    // Poll Spring Boot every 3 seconds for execution state
    _pollTimer = Timer.periodic(const Duration(seconds: 3), (_) => _pollExecution());
  }

  void _startCountdown() {
    final active = _steps.where((s) => s['status'] == 'IN_PROGRESS').firstOrNull;
    if (active != null) {
      _remainingSeconds = (active['estimatedDurationSeconds'] as int?) ?? 0;
    }
    _countdownTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (_remainingSeconds > 0) {
        setState(() => _remainingSeconds--);
      }
    });
  }

  Future<void> _pollExecution() async {
    // In production: GET /executions/{id} from Spring Boot
    // Spring Boot reads Redis session + intervention keys and includes in response
    // Flutter NEVER reads Redis directly

    // Simulate intervention detection
    final userStep = _steps.where((s) => s['assignedTo'] == 'USER' && s['status'] == 'IN_PROGRESS').firstOrNull;
    if (userStep != null && _activeIntervention == null) {
      setState(() => _activeIntervention = 'intervention-${userStep['stepOrder']}');
      _showInterventionModal();
    }
  }

  void _showInterventionModal() {
    showModalBottomSheet(
      context: context,
      isDismissible: false,
      backgroundColor: AppColors.surface2,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (_) => _InterventionSheet(
        interventionId: _activeIntervention!,
        onResolved: _resolveIntervention,
      ),
    );
  }

  Future<void> _resolveIntervention() async {
    // In production: POST /executions/{id}/interventions/{iid}/resolve
    // Spring Boot deletes Redis intervention key; simulation resumes
    try {
      // await AuthorityService().resolveIntervention(executionId, interventionId);
      setState(() => _activeIntervention = null);
      if (mounted) Navigator.of(context).pop();
    } on ServiceException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: ${e.message}')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final totalDuration = _plan['estimatedDurationSeconds'] as int? ?? 0;
    final mealName = _plan['mealName'] as String? ?? S.cookingTitle;
    final servings = _plan['servings'] as int? ?? 0;
    final status = _plan['status'] as String? ?? 'IDLE';

    return Scaffold(
      appBar: AppBar(
        title: Text(mealName),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Plan overview
          _OverviewCard(
            status: status,
            totalDuration: totalDuration,
            servings: servings,
            remainingSeconds: _remainingSeconds,
          ),
          const SizedBox(height: 16),

          // Step list
          const Text('Steps', style: TextStyle(color: AppColors.text3, fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          ..._steps.map((step) => _StepTile(
            step: step,
            isActive: step['status'] == 'IN_PROGRESS',
            remainingSeconds: step['status'] == 'IN_PROGRESS' ? _remainingSeconds : null,
          )),
        ],
      ),
    );
  }
}

class _OverviewCard extends StatelessWidget {
  final String status;
  final int totalDuration;
  final int servings;
  final int remainingSeconds;

  const _OverviewCard({
    required this.status, required this.totalDuration,
    required this.servings, required this.remainingSeconds,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.surface2),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              _StatusChip(status: status),
              Text('$servings servings', style: const TextStyle(color: AppColors.text3, fontSize: 13)),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Total: ${_formatTime(totalDuration)}', style: const TextStyle(color: AppColors.text4, fontSize: 12)),
              if (remainingSeconds > 0)
                Text('Remaining: ${_formatTime(remainingSeconds)}',
                  style: const TextStyle(color: AppColors.green1, fontSize: 14, fontWeight: FontWeight.w600)),
            ],
          ),
        ],
      ),
    );
  }

  String _formatTime(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return '${m}m ${s.toString().padLeft(2, '0')}s';
  }
}

class _StatusChip extends StatelessWidget {
  final String status;
  const _StatusChip({required this.status});

  Color get _color {
    switch (status) {
      case 'IN_PROGRESS': return AppColors.green1;
      case 'COMPLETED': return AppColors.green2;
      case 'PAUSED': return AppColors.amber1;
      case 'FAILED': case 'ABORTED': return AppColors.red1;
      default: return AppColors.text4;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: _color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(8)),
      child: Text(status, style: TextStyle(color: _color, fontSize: 12, fontWeight: FontWeight.w600)),
    );
  }
}

class _StepTile extends StatelessWidget {
  final Map<String, dynamic> step;
  final bool isActive;
  final int? remainingSeconds;

  const _StepTile({required this.step, this.isActive = false, this.remainingSeconds});

  @override
  Widget build(BuildContext context) {
    final order = step['stepOrder'] as int;
    final action = step['actionType'] as String;
    final assignedTo = step['assignedTo'] as String;
    final status = step['status'] as String;
    final duration = step['estimatedDurationSeconds'] as int? ?? 0;

    final isUser = assignedTo == 'USER';
    final chipColor = isUser ? AppColors.amber1 : AppColors.green1;

    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: isActive ? AppColors.surface2 : AppColors.surface,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: isActive ? AppColors.green1.withValues(alpha: 0.4) : AppColors.surface2,
        ),
      ),
      child: Row(
        children: [
          // Step number
          Container(
            width: 28, height: 28,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: status == 'COMPLETE'
                  ? AppColors.green2.withValues(alpha: 0.2)
                  : AppColors.surface2,
            ),
            child: Center(
              child: status == 'COMPLETE'
                  ? const Icon(Icons.check, size: 16, color: AppColors.green2)
                  : Text('$order', style: const TextStyle(color: AppColors.text3, fontSize: 12)),
            ),
          ),
          const SizedBox(width: 12),

          // Action and assignment
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(action, style: const TextStyle(color: AppColors.text1, fontSize: 13)),
                const SizedBox(height: 2),
                Text('${duration}s', style: const TextStyle(color: AppColors.text4, fontSize: 11)),
              ],
            ),
          ),

          // Assignment chip
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
            decoration: BoxDecoration(
              color: chipColor.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(6),
            ),
            child: Text(assignedTo, style: TextStyle(color: chipColor, fontSize: 10, fontWeight: FontWeight.w600)),
          ),

          // Active timer
          if (isActive && remainingSeconds != null) ...[
            const SizedBox(width: 8),
            Text('${remainingSeconds}s', style: const TextStyle(color: AppColors.green1, fontSize: 13, fontWeight: FontWeight.bold)),
          ],
        ],
      ),
    );
  }
}

class _InterventionSheet extends StatelessWidget {
  final String interventionId;
  final VoidCallback onResolved;

  const _InterventionSheet({required this.interventionId, required this.onResolved});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.pan_tool, color: AppColors.amber1, size: 40),
          const SizedBox(height: 16),
          const Text(S.userActionRequired,
            style: TextStyle(color: AppColors.text1, fontSize: 18, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          const Text(S.tapWhenDone,
            style: TextStyle(color: AppColors.text3, fontSize: 14), textAlign: TextAlign.center),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: onResolved,
              child: const Text(S.done, style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
            ),
          ),
        ],
      ),
    );
  }
}
