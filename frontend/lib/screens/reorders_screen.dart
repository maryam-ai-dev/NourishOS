import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';

/// Demo suggestion data. In production, fetched from Spring Boot GET /replenishment/suggestions.
class _Suggestion {
  final String id;
  final String ingredientName;
  final double quantity;
  final String unit;
  final String urgency; // CRITICAL, WARNING
  final bool adjustedForWaste;
  final String? explanation;
  String status; // PENDING, APPROVED, REJECTED, DEFERRED

  _Suggestion({
    required this.id, required this.ingredientName, required this.quantity,
    required this.unit, required this.urgency, this.adjustedForWaste = false,
    this.explanation, this.status = 'PENDING',
  });
}

final _demoSuggestions = [
  _Suggestion(id: '1', ingredientName: 'Rice', quantity: 800, unit: 'g', urgency: 'CRITICAL',
    adjustedForWaste: true, explanation: 'Quantity reduced by 20% due to recurring waste history.'),
  _Suggestion(id: '2', ingredientName: 'Chicken Breast', quantity: 1000, unit: 'g', urgency: 'CRITICAL'),
  _Suggestion(id: '3', ingredientName: 'Olive Oil', quantity: 500, unit: 'ml', urgency: 'WARNING'),
  _Suggestion(id: '4', ingredientName: 'Spinach', quantity: 200, unit: 'g', urgency: 'WARNING',
    adjustedForWaste: true, explanation: 'Recurring waste detected — quantity adjusted.', status: 'APPROVED'),
];

final reorderSuggestionsProvider = StateProvider<List<_Suggestion>>((ref) {
  final sorted = List<_Suggestion>.from(_demoSuggestions);
  sorted.sort((a, b) {
    const order = {'CRITICAL': 0, 'WARNING': 1};
    return (order[a.urgency] ?? 2).compareTo(order[b.urgency] ?? 2);
  });
  return sorted;
});

class ReordersScreen extends ConsumerWidget {
  const ReordersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final suggestions = ref.watch(reorderSuggestionsProvider);
    final pending = suggestions.where((s) => s.status == 'PENDING').toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text(S.reordersTitle),
        actions: [
          if (pending.isNotEmpty)
            TextButton(
              onPressed: () => _approveAll(ref, suggestions),
              child: const Text(S.approveAll, style: TextStyle(color: AppColors.green2)),
            ),
        ],
      ),
      body: suggestions.isEmpty
          ? const Center(child: Text(S.noSuggestions, style: TextStyle(color: AppColors.text4, fontSize: 16)))
          : ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: suggestions.length,
              itemBuilder: (context, index) => _SuggestionCard(
                suggestion: suggestions[index],
                onApprove: () => _updateStatus(ref, suggestions, index, 'APPROVED'),
                onReject: () => _updateStatus(ref, suggestions, index, 'REJECTED'),
                onDefer: () => _updateStatus(ref, suggestions, index, 'DEFERRED'),
              ),
            ),
    );
  }

  void _updateStatus(WidgetRef ref, List<_Suggestion> suggestions, int index, String status) {
    suggestions[index].status = status;
    ref.read(reorderSuggestionsProvider.notifier).state = List.from(suggestions);
  }

  void _approveAll(WidgetRef ref, List<_Suggestion> suggestions) {
    for (final s in suggestions) {
      if (s.status == 'PENDING') s.status = 'APPROVED';
    }
    ref.read(reorderSuggestionsProvider.notifier).state = List.from(suggestions);
  }
}

class _SuggestionCard extends StatelessWidget {
  final _Suggestion suggestion;
  final VoidCallback onApprove;
  final VoidCallback onReject;
  final VoidCallback onDefer;

  const _SuggestionCard({
    required this.suggestion, required this.onApprove,
    required this.onReject, required this.onDefer,
  });

  Color get _urgencyColor {
    switch (suggestion.urgency) {
      case 'CRITICAL': return AppColors.red1;
      case 'WARNING': return AppColors.amber1;
      default: return AppColors.text4;
    }
  }

  Color get _statusColor {
    switch (suggestion.status) {
      case 'APPROVED': return AppColors.green2;
      case 'REJECTED': return AppColors.red1;
      case 'DEFERRED': return AppColors.amber1;
      default: return AppColors.text4;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.surface2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header: name + urgency + status
          Row(
            children: [
              Expanded(
                child: Text(suggestion.ingredientName,
                  style: const TextStyle(color: AppColors.text1, fontSize: 15, fontWeight: FontWeight.w500)),
              ),
              _Badge(label: suggestion.urgency, color: _urgencyColor),
              const SizedBox(width: 6),
              _Badge(label: suggestion.status, color: _statusColor),
            ],
          ),
          const SizedBox(height: 8),

          // Quantity
          Text('${suggestion.quantity} ${suggestion.unit}',
            style: const TextStyle(color: AppColors.text3, fontSize: 13)),

          // Waste adjustment label
          if (suggestion.adjustedForWaste) ...[
            const SizedBox(height: 6),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
              decoration: BoxDecoration(
                color: AppColors.amber3,
                borderRadius: BorderRadius.circular(6),
              ),
              child: const Text(S.reducedWasted,
                style: TextStyle(color: AppColors.amber1, fontSize: 11, fontWeight: FontWeight.w500)),
            ),
          ],

          // Explanation
          if (suggestion.explanation != null) ...[
            const SizedBox(height: 6),
            Text(suggestion.explanation!,
              style: const TextStyle(color: AppColors.text4, fontSize: 12, fontStyle: FontStyle.italic)),
          ],

          // Action buttons (only for PENDING)
          if (suggestion.status == 'PENDING') ...[
            const SizedBox(height: 10),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton(onPressed: onDefer, child: const Text('Defer', style: TextStyle(color: AppColors.text4, fontSize: 12))),
                const SizedBox(width: 8),
                TextButton(onPressed: onReject, child: const Text('Reject', style: TextStyle(color: AppColors.red1, fontSize: 12))),
                const SizedBox(width: 8),
                ElevatedButton(
                  onPressed: onApprove,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.green2, foregroundColor: AppColors.text1,
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                    minimumSize: Size.zero,
                  ),
                  child: const Text('Approve', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600)),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  final String label;
  final Color color;
  const _Badge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(6)),
      child: Text(label, style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w600)),
    );
  }
}
