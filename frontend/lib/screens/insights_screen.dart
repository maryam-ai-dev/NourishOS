import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

// --- Demo data models ---

class _WasteMetrics {
  final double wasteRatio;
  final double wasteRatioLastWeek;
  final List<_FrequentWaste> frequentlyWasted;

  _WasteMetrics({required this.wasteRatio, required this.wasteRatioLastWeek, required this.frequentlyWasted});
}

class _FrequentWaste {
  final String ingredientName;
  final int wasteCount;
  final String topReason;

  _FrequentWaste({required this.ingredientName, required this.wasteCount, required this.topReason});
}

class _MealReliabilityItem {
  final String mealName;
  final double completionRate;
  final bool isLowReliability;

  _MealReliabilityItem({required this.mealName, required this.completionRate, required this.isLowReliability});
}

class _ReplenishmentPattern {
  final String ingredientName;
  final int restockCount;
  final bool overBought;
  final bool recurringWaste;
  final bool adjustedForWaste;

  _ReplenishmentPattern({
    required this.ingredientName, required this.restockCount,
    this.overBought = false, this.recurringWaste = false, this.adjustedForWaste = false,
  });
}

// --- Demo data providers (in production, sourced from Spring Boot food flow snapshot) ---

final wasteMetricsProvider = FutureProvider<_WasteMetrics>((ref) async {
  return _WasteMetrics(
    wasteRatio: 0.15,
    wasteRatioLastWeek: 0.22,
    frequentlyWasted: [
      _FrequentWaste(ingredientName: 'Spinach', wasteCount: 4, topReason: 'EXPIRED'),
      _FrequentWaste(ingredientName: 'Bread', wasteCount: 3, topReason: 'LEFTOVER_UNUSED'),
      _FrequentWaste(ingredientName: 'Yogurt', wasteCount: 2, topReason: 'EXPIRED'),
    ],
  );
});

final mealReliabilityProvider = FutureProvider<List<_MealReliabilityItem>>((ref) async {
  return [
    _MealReliabilityItem(mealName: 'Pasta Primavera', completionRate: 0.92, isLowReliability: false),
    _MealReliabilityItem(mealName: 'Grilled Chicken', completionRate: 0.85, isLowReliability: false),
    _MealReliabilityItem(mealName: 'Soufflé', completionRate: 0.35, isLowReliability: true),
    _MealReliabilityItem(mealName: 'Stir Fry', completionRate: 0.78, isLowReliability: false),
  ];
});

final replenishmentPatternsProvider = FutureProvider<List<_ReplenishmentPattern>>((ref) async {
  return [
    _ReplenishmentPattern(ingredientName: 'Rice', restockCount: 6, overBought: true, recurringWaste: true, adjustedForWaste: true),
    _ReplenishmentPattern(ingredientName: 'Chicken', restockCount: 5),
    _ReplenishmentPattern(ingredientName: 'Milk', restockCount: 4, overBought: true),
    _ReplenishmentPattern(ingredientName: 'Spinach', restockCount: 3, recurringWaste: true, adjustedForWaste: true),
  ];
});

// --- Screen ---

class InsightsScreen extends ConsumerWidget {
  const InsightsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0B0F),
      appBar: AppBar(
        title: const Text('Insights'),
        backgroundColor: const Color(0xFF121218),
        foregroundColor: Colors.white,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _WasteSection(metrics: ref.watch(wasteMetricsProvider)),
          const SizedBox(height: 16),
          _NutritionReliabilitySection(reliability: ref.watch(mealReliabilityProvider)),
          const SizedBox(height: 16),
          _ReplenishmentSection(patterns: ref.watch(replenishmentPatternsProvider)),
        ],
      ),
    );
  }
}

// --- Sprint 18.1: Waste Metrics Section ---

class _WasteSection extends StatelessWidget {
  final AsyncValue<_WasteMetrics> metrics;
  const _WasteSection({required this.metrics});

  @override
  Widget build(BuildContext context) {
    return _Card(
      title: 'Waste',
      child: metrics.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: Colors.redAccent)),
        data: (m) {
          final avoided = m.wasteRatioLastWeek - m.wasteRatio;
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Waste avoided
              Row(
                children: [
                  Icon(
                    avoided > 0 ? Icons.trending_down : Icons.trending_up,
                    color: avoided > 0 ? const Color(0xFF7CFF6B) : Colors.redAccent,
                    size: 20,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    avoided > 0
                        ? '${(avoided * 100).toStringAsFixed(1)}% less waste than last week'
                        : '${(avoided.abs() * 100).toStringAsFixed(1)}% more waste than last week',
                    style: TextStyle(
                      color: avoided > 0 ? const Color(0xFF7CFF6B) : Colors.redAccent,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              Text('Current waste ratio: ${(m.wasteRatio * 100).toStringAsFixed(1)}%',
                style: const TextStyle(color: Colors.white54, fontSize: 12)),
              const SizedBox(height: 12),

              // Frequently wasted
              if (m.frequentlyWasted.isEmpty)
                const Text('No waste events recorded', style: TextStyle(color: Colors.white38, fontSize: 12))
              else ...[
                const Text('Frequently Wasted', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
                const SizedBox(height: 6),
                ...m.frequentlyWasted.map((fw) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(fw.ingredientName, style: const TextStyle(color: Colors.white70, fontSize: 13))),
                      Text('${fw.wasteCount}x', style: const TextStyle(color: Colors.redAccent, fontSize: 12, fontWeight: FontWeight.w600)),
                      const SizedBox(width: 8),
                      Text(fw.topReason, style: const TextStyle(color: Colors.white38, fontSize: 10)),
                    ],
                  ),
                )),
              ],
            ],
          );
        },
      ),
    );
  }
}

// --- Sprint 18.2: Nutrition & Reliability Section ---

class _NutritionReliabilitySection extends StatelessWidget {
  final AsyncValue<List<_MealReliabilityItem>> reliability;
  const _NutritionReliabilitySection({required this.reliability});

  @override
  Widget build(BuildContext context) {
    return _Card(
      title: 'Nutrition & Reliability',
      child: reliability.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: Colors.redAccent)),
        data: (meals) {
          // Sort by completion rate descending
          final sorted = List<_MealReliabilityItem>.from(meals)
            ..sort((a, b) => b.completionRate.compareTo(a.completionRate));

          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Protein bar placeholder
              const Text('Weekly Protein', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: 0.78,
                  backgroundColor: Colors.white.withValues(alpha: 0.08),
                  valueColor: const AlwaysStoppedAnimation<Color>(Color(0xFF7CFF6B)),
                  minHeight: 8,
                ),
              ),
              const SizedBox(height: 4),
              const Text('78% of household goal', style: TextStyle(color: Colors.white38, fontSize: 11)),
              const SizedBox(height: 16),

              // Reliable meals
              const Text('Meal Reliability', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              ...sorted.map((meal) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 3),
                child: Row(
                  children: [
                    Expanded(child: Text(meal.mealName, style: const TextStyle(color: Colors.white70, fontSize: 13))),
                    if (meal.isLowReliability)
                      Container(
                        margin: const EdgeInsets.only(right: 6),
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                        decoration: BoxDecoration(
                          color: Colors.amber.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text('LOW', style: TextStyle(color: Colors.amber, fontSize: 9, fontWeight: FontWeight.w600)),
                      ),
                    Text('${(meal.completionRate * 100).toInt()}%',
                      style: TextStyle(
                        color: meal.isLowReliability ? Colors.amber : const Color(0xFF7CFF6B),
                        fontSize: 13, fontWeight: FontWeight.w600,
                      )),
                  ],
                ),
              )),
            ],
          );
        },
      ),
    );
  }
}

// --- Sprint 18.3: Replenishment Patterns Section ---

class _ReplenishmentSection extends StatelessWidget {
  final AsyncValue<List<_ReplenishmentPattern>> patterns;
  const _ReplenishmentSection({required this.patterns});

  @override
  Widget build(BuildContext context) {
    return _Card(
      title: 'Replenishment Patterns',
      child: patterns.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: Colors.redAccent)),
        data: (items) {
          final restocked = List<_ReplenishmentPattern>.from(items)
            ..sort((a, b) => b.restockCount.compareTo(a.restockCount));
          final overBought = items.where((i) => i.overBought).toList();
          final wasteAdjusted = items.where((i) => i.adjustedForWaste).toList();

          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Most frequently restocked
              const Text('Most Restocked', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (restocked.isEmpty)
                const Text('Nothing to report', style: TextStyle(color: Colors.white38, fontSize: 12))
              else
                ...restocked.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: Colors.white70, fontSize: 13))),
                      Text('${p.restockCount}x', style: const TextStyle(color: Colors.white54, fontSize: 12)),
                    ],
                  ),
                )),
              const SizedBox(height: 14),

              // Over-bought
              const Text('Consider Buying Less', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (overBought.isEmpty)
                const Text('Nothing to report', style: TextStyle(color: Colors.white38, fontSize: 12))
              else
                ...overBought.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: Colors.white70, fontSize: 13))),
                      if (p.recurringWaste)
                        Container(
                          margin: const EdgeInsets.only(right: 4),
                          padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                          decoration: BoxDecoration(
                            color: Colors.redAccent.withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text('WASTE', style: TextStyle(color: Colors.redAccent, fontSize: 9, fontWeight: FontWeight.w600)),
                        ),
                      const Text('over-bought', style: TextStyle(color: Colors.amber, fontSize: 11)),
                    ],
                  ),
                )),
              const SizedBox(height: 14),

              // Waste-adjusted suggestions
              const Text('Waste-Adjusted Orders', style: TextStyle(color: Colors.white54, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (wasteAdjusted.isEmpty)
                const Text('Nothing to report', style: TextStyle(color: Colors.white38, fontSize: 12))
              else
                ...wasteAdjusted.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: Colors.white70, fontSize: 13))),
                      const Text('qty adjusted', style: TextStyle(color: Colors.amber, fontSize: 11)),
                    ],
                  ),
                )),
            ],
          );
        },
      ),
    );
  }
}

// --- Shared Card Widget ---

class _Card extends StatelessWidget {
  final String title;
  final Widget child;
  const _Card({required this.title, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF121218),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withValues(alpha: 0.08)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}
