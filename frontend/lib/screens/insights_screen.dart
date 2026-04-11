import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';

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
      appBar: AppBar(
        title: const Text(S.insightsTitle),
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
      title: S.wasteSection,
      child: metrics.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: AppColors.red1)),
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
                    color: avoided > 0 ? AppColors.green2 : AppColors.red1,
                    size: 20,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    avoided > 0
                        ? '${(avoided * 100).toStringAsFixed(1)}% less waste than last week'
                        : '${(avoided.abs() * 100).toStringAsFixed(1)}% more waste than last week',
                    style: TextStyle(
                      color: avoided > 0 ? AppColors.green2 : AppColors.red1,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 6),
              Text('Current waste ratio: ${(m.wasteRatio * 100).toStringAsFixed(1)}%',
                style: const TextStyle(color: AppColors.text3, fontSize: 12)),
              const SizedBox(height: 12),

              // Frequently wasted
              if (m.frequentlyWasted.isEmpty)
                const Text(S.nothingToReport, style: TextStyle(color: AppColors.text4, fontSize: 12))
              else ...[
                const Text('Frequently wasted', style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
                const SizedBox(height: 6),
                ...m.frequentlyWasted.map((fw) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(fw.ingredientName, style: const TextStyle(color: AppColors.text2, fontSize: 13))),
                      Text('${fw.wasteCount}x', style: const TextStyle(color: AppColors.red1, fontSize: 12, fontWeight: FontWeight.w600)),
                      const SizedBox(width: 8),
                      Text(fw.topReason, style: const TextStyle(color: AppColors.text4, fontSize: 10)),
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
      title: S.nutritionSection,
      child: reliability.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: AppColors.red1)),
        data: (meals) {
          // Sort by completion rate descending
          final sorted = List<_MealReliabilityItem>.from(meals)
            ..sort((a, b) => b.completionRate.compareTo(a.completionRate));

          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Protein bar placeholder
              const Text(S.weeklyProtein, style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: 0.78,
                  backgroundColor: AppColors.surface2,
                  valueColor: const AlwaysStoppedAnimation<Color>(AppColors.green2),
                  minHeight: 8,
                ),
              ),
              const SizedBox(height: 4),
              const Text('78% of household goal', style: TextStyle(color: AppColors.text4, fontSize: 11)),
              const SizedBox(height: 16),

              // Reliable meals
              const Text(S.mealReliability, style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              ...sorted.map((meal) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 3),
                child: Row(
                  children: [
                    Expanded(child: Text(meal.mealName, style: const TextStyle(color: AppColors.text2, fontSize: 13))),
                    if (meal.isLowReliability)
                      Container(
                        margin: const EdgeInsets.only(right: 6),
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                        decoration: BoxDecoration(
                          color: AppColors.amber1.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text(S.lowReliability, style: TextStyle(color: AppColors.amber1, fontSize: 9, fontWeight: FontWeight.w600)),
                      ),
                    Text('${(meal.completionRate * 100).toInt()}%',
                      style: TextStyle(
                        color: meal.isLowReliability ? AppColors.amber1 : AppColors.green2,
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
      title: S.replenishmentSection,
      child: patterns.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Text('Error: $err', style: const TextStyle(color: AppColors.red1)),
        data: (items) {
          final restocked = List<_ReplenishmentPattern>.from(items)
            ..sort((a, b) => b.restockCount.compareTo(a.restockCount));
          final overBought = items.where((i) => i.overBought).toList();
          final wasteAdjusted = items.where((i) => i.adjustedForWaste).toList();

          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Most frequently restocked
              const Text(S.mostRestocked, style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (restocked.isEmpty)
                const Text(S.nothingToReport, style: TextStyle(color: AppColors.text4, fontSize: 12))
              else
                ...restocked.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: AppColors.text2, fontSize: 13))),
                      Text('${p.restockCount}x', style: const TextStyle(color: AppColors.text3, fontSize: 12)),
                    ],
                  ),
                )),
              const SizedBox(height: 14),

              // Over-bought
              const Text(S.considerBuyingLess, style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (overBought.isEmpty)
                const Text(S.nothingToReport, style: TextStyle(color: AppColors.text4, fontSize: 12))
              else
                ...overBought.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: AppColors.text2, fontSize: 13))),
                      if (p.recurringWaste)
                        Container(
                          margin: const EdgeInsets.only(right: 4),
                          padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 1),
                          decoration: BoxDecoration(
                            color: AppColors.red1.withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(S.wastedRecently, style: TextStyle(color: AppColors.red1, fontSize: 9, fontWeight: FontWeight.w600)),
                        ),
                      const Text('over-bought', style: TextStyle(color: AppColors.amber1, fontSize: 11)),
                    ],
                  ),
                )),
              const SizedBox(height: 14),

              // Waste-adjusted suggestions
              const Text('Waste-adjusted orders', style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w600)),
              const SizedBox(height: 6),
              if (wasteAdjusted.isEmpty)
                const Text(S.nothingToReport, style: TextStyle(color: AppColors.text4, fontSize: 12))
              else
                ...wasteAdjusted.map((p) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 3),
                  child: Row(
                    children: [
                      Expanded(child: Text(p.ingredientName, style: const TextStyle(color: AppColors.text2, fontSize: 13))),
                      const Text(S.qtyAdjusted, style: TextStyle(color: AppColors.amber1, fontSize: 11)),
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
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.sm),
        boxShadow: AppShadows.xs,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: AppColors.text1, fontSize: 16, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}
