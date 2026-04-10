import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/intelligence_service.dart';
import '../services/service_exception.dart';
import '../providers/providers.dart';

const _days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const _mealTypes = ['BREAKFAST', 'LUNCH', 'DINNER'];

/// Provider for the generated weekly plan proposal (before user confirms).
final weeklyPlanProposalProvider = StateProvider<Map<String, dynamic>?>((ref) => null);

class WeeklyPlannerScreen extends ConsumerStatefulWidget {
  const WeeklyPlannerScreen({super.key});

  @override
  ConsumerState<WeeklyPlannerScreen> createState() => _WeeklyPlannerScreenState();
}

class _WeeklyPlannerScreenState extends ConsumerState<WeeklyPlannerScreen> {
  bool _generating = false;
  bool _confirming = false;

  @override
  Widget build(BuildContext context) {
    final proposal = ref.watch(weeklyPlanProposalProvider);
    final slots = (proposal?['slots'] as List?) ?? [];
    final missing = (proposal?['missingIngredients'] as List?) ?? [];

    return Scaffold(
      backgroundColor: const Color(0xFF0B0B0F),
      appBar: AppBar(
        title: const Text('Weekly Planner'),
        backgroundColor: const Color(0xFF121218),
        foregroundColor: Colors.white,
        actions: [
          if (proposal != null)
            TextButton(
              onPressed: _confirming ? null : _confirmPlan,
              child: _confirming
                  ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Text('Confirm', style: TextStyle(color: Color(0xFF7CFF6B))),
            ),
        ],
      ),
      body: Column(
        children: [
          Expanded(child: _PlannerGrid(slots: slots)),
          if (missing.isNotEmpty) _MissingIngredientsPanel(missing: missing),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _generating ? null : _generatePlan,
        backgroundColor: const Color(0xFF9B5CFF),
        icon: _generating
            ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
            : const Icon(Icons.auto_awesome, color: Colors.white),
        label: Text(_generating ? 'Generating...' : 'Generate Plan', style: const TextStyle(color: Colors.white)),
      ),
    );
  }

  Future<void> _generatePlan() async {
    setState(() => _generating = true);
    try {
      final householdId = ref.read(householdIdProvider);
      final now = DateTime.now();
      final monday = now.subtract(Duration(days: now.weekday - 1));
      final result = await IntelligenceService().planWeekly({
        'householdId': householdId,
        'weekStartDate': '${monday.year}-${monday.month.toString().padLeft(2, '0')}-${monday.day.toString().padLeft(2, '0')}',
      });
      ref.read(weeklyPlanProposalProvider.notifier).state = result;
    } on ServiceException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Error: ${e.message}')));
      }
    } finally {
      if (mounted) setState(() => _generating = false);
    }
  }

  Future<void> _confirmPlan() async {
    setState(() => _confirming = true);
    try {
      // In production, POST confirmed schedule to Spring Boot
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Plan confirmed!')));
      }
    } finally {
      if (mounted) setState(() => _confirming = false);
    }
  }
}

class _PlannerGrid extends StatelessWidget {
  final List<dynamic> slots;
  const _PlannerGrid({required this.slots});

  Map<String, dynamic>? _findSlot(int day, String mealType) {
    for (final s in slots) {
      if (s['day'] == day && s['mealType'] == mealType) return s as Map<String, dynamic>;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(8),
      child: Column(
        children: [
          // Header row
          Row(
            children: [
              const SizedBox(width: 60),
              for (final day in _days)
                Expanded(
                  child: Center(
                    child: Text(day, style: const TextStyle(color: Colors.white54, fontSize: 11, fontWeight: FontWeight.w600)),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 4),
          // Meal rows
          for (var mealIdx = 0; mealIdx < _mealTypes.length; mealIdx++)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 2),
              child: Row(
                children: [
                  SizedBox(
                    width: 60,
                    child: Text(
                      _mealTypes[mealIdx][0] + _mealTypes[mealIdx].substring(1).toLowerCase(),
                      style: const TextStyle(color: Colors.white38, fontSize: 11),
                    ),
                  ),
                  for (var dayIdx = 1; dayIdx <= 7; dayIdx++)
                    Expanded(child: _SlotCell(slot: _findSlot(dayIdx, _mealTypes[mealIdx]))),
                ],
              ),
            ),
        ],
      ),
    );
  }
}

class _SlotCell extends StatelessWidget {
  final Map<String, dynamic>? slot;
  const _SlotCell({this.slot});

  @override
  Widget build(BuildContext context) {
    if (slot == null) {
      return Container(
        margin: const EdgeInsets.all(2),
        height: 56,
        decoration: BoxDecoration(
          color: const Color(0xFF171720),
          borderRadius: BorderRadius.circular(6),
          border: Border.all(color: Colors.white.withValues(alpha: 0.05)),
        ),
        child: const Center(
          child: Text('—', style: TextStyle(color: Colors.white24, fontSize: 12)),
        ),
      );
    }

    final name = (slot!['mealName'] as String?) ?? '';
    final protein = slot!['estimatedProteinGrams'];
    final perishable = slot!['isPerishablePriority'] == true;

    return Container(
      margin: const EdgeInsets.all(2),
      height: 56,
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: const Color(0xFF121218),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: const Color(0xFF9B5CFF).withValues(alpha: 0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            name.length > 10 ? '${name.substring(0, 10)}...' : name,
            style: const TextStyle(color: Colors.white, fontSize: 9, fontWeight: FontWeight.w500),
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 2),
          Row(
            children: [
              if (protein != null)
                _MiniBadge(label: '${(protein as num).toInt()}g', color: Colors.teal),
              if (perishable)
                const _MiniBadge(label: '!', color: Colors.amber),
            ],
          ),
        ],
      ),
    );
  }
}

class _MiniBadge extends StatelessWidget {
  final String label;
  final Color color;
  const _MiniBadge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(right: 2),
      padding: const EdgeInsets.symmetric(horizontal: 3, vertical: 1),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Text(label, style: TextStyle(color: color, fontSize: 8, fontWeight: FontWeight.w600)),
    );
  }
}

class _MissingIngredientsPanel extends StatelessWidget {
  final List<dynamic> missing;
  const _MissingIngredientsPanel({required this.missing});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      decoration: const BoxDecoration(
        color: Color(0xFF171720),
        border: Border(top: BorderSide(color: Colors.white12)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text('Missing Ingredients', style: TextStyle(color: Colors.amber, fontSize: 13, fontWeight: FontWeight.w600)),
          const SizedBox(height: 6),
          Text('${missing.length} item(s) needed', style: const TextStyle(color: Colors.white54, fontSize: 12)),
          const SizedBox(height: 8),
          SizedBox(
            width: double.infinity,
            child: OutlinedButton(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Restock request submitted')),
                );
              },
              style: OutlinedButton.styleFrom(
                foregroundColor: const Color(0xFF7CFF6B),
                side: const BorderSide(color: Color(0xFF7CFF6B)),
              ),
              child: const Text('Approve Restock'),
            ),
          ),
        ],
      ),
    );
  }
}
