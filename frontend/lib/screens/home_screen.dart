import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import '../providers/providers.dart';
import '../widgets/savings_hero_card.dart';
import '../widgets/meal_hero_card.dart';
import '../widgets/budget_bar_card.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final expiring = ref.watch(inventoryExpiringProvider);
    final lowStock = ref.watch(inventoryLowStockProvider);

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            ref.invalidate(inventoryExpiringProvider);
            ref.invalidate(inventoryLowStockProvider);
          },
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // Header
              Row(
                children: [
                  Container(
                    width: 36, height: 36,
                    decoration: const BoxDecoration(color: AppColors.green4, shape: BoxShape.circle),
                    child: const Center(child: Text('🌿', style: TextStyle(fontSize: 18))),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(S.greeting, style: TextStyle(color: AppColors.text3, fontSize: 12)),
                        Text(S.appName, style: GoogleFonts.dmSerifDisplay(fontSize: 22, color: AppColors.text1, letterSpacing: -0.2), overflow: TextOverflow.ellipsis),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),

              // Savings hero (white + green)
              const SavingsHeroCard(
                savedGbp: 4.20,
                previousWeekGbp: 2.80,
                wasteItems: 2,
                previousWasteItems: 5,
                mealsCompletedRate: 0.85,
                totalSpent: 42,
              ),
              const SizedBox(height: 12),

              // Meal hero
              MealHeroCard(
                mealName: 'Grilled Chicken & Rice',
                matchScore: 92,
                memberCount: 3,
                emoji: '🍗',
                hasExpiringIngredient: true,
                expiringNote: 'Uses spinach expiring today',
                onStartCooking: () => context.go('/cooking'),
              ),
              const SizedBox(height: 12),

              // Simplified budget bar
              BudgetBarCard(
                weeklyLimit: 80,
                totalSpent: 42,
                spentPercent: 0.525,
                onTap: () {},
              ),
              const SizedBox(height: 20),

              // Status pills (subtle, at bottom)
              Row(
                children: [
                  Expanded(child: _StatusPill(
                    emoji: '⚠️',
                    count: expiring.whenOrNull(data: (items) => items.length) ?? 0,
                    label: S.expiringSoon,
                    variant: _PillVariant.alert,
                  )),
                  const SizedBox(width: 8),
                  Expanded(child: _StatusPill(
                    emoji: '📦',
                    count: lowStock.whenOrNull(data: (items) => items.length) ?? 0,
                    label: S.lowStock,
                    variant: _PillVariant.warn,
                  )),
                  const SizedBox(width: 8),
                  Expanded(child: _StatusPill(
                    emoji: '✅',
                    count: 0,
                    label: S.allGood,
                    variant: _PillVariant.healthy,
                  )),
                ],
              ),
              const SizedBox(height: 12),

              // Insights chip (bottom right)
              Align(
                alignment: Alignment.centerRight,
                child: GestureDetector(
                  onTap: () => context.go('/insights'),
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: AppColors.surface,
                      borderRadius: BorderRadius.circular(AppRadius.full),
                      border: Border.all(color: AppColors.green3, width: 1),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text('View insights', style: TextStyle(color: AppColors.green1, fontSize: 11, fontWeight: FontWeight.w600)),
                        const SizedBox(width: 4),
                        Icon(Icons.arrow_forward, size: 12, color: AppColors.green1),
                      ],
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

enum _PillVariant { alert, warn, healthy }

class _StatusPill extends StatelessWidget {
  final String emoji;
  final int count;
  final String label;
  final _PillVariant variant;

  const _StatusPill({required this.emoji, required this.count, required this.label, required this.variant});

  @override
  Widget build(BuildContext context) {
    final (bg, fg) = switch (variant) {
      _PillVariant.alert when count > 0 => (AppColors.red3, AppColors.red1),
      _PillVariant.warn when count > 0 => (AppColors.amber3, AppColors.amber1),
      _ => (AppColors.surface, AppColors.text3),
    };

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 8),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.sm),
        boxShadow: AppShadows.xs,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(emoji, style: const TextStyle(fontSize: 14)),
          const SizedBox(height: 2),
          Text('$count', style: TextStyle(color: fg, fontSize: 15, fontWeight: FontWeight.w700)),
          Text(label, style: TextStyle(color: fg.withValues(alpha: 0.8), fontSize: 9, fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }
}
