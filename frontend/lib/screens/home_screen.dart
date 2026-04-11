import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import '../providers/providers.dart';
import '../widgets/savings_hero_card.dart';
import '../widgets/budget_bar_card.dart';
import '../widgets/agent_nudge_card.dart';
import '../widgets/app_card.dart';

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
                    decoration: BoxDecoration(color: AppColors.green4, shape: BoxShape.circle),
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

              // Savings hero
              const SavingsHeroCard(
                savedGbp: 4.20,
                previousWeekGbp: 2.80,
                wasteItems: 2,
                previousWasteItems: 5,
                mealsCompletedRate: 0.85,
                totalSpent: 42,
              ),
              const SizedBox(height: 12),

              // Budget bar
              const BudgetBarCard(
                weeklyLimit: 80,
                totalSpent: 42,
                spentPercent: 0.525,
                categories: [
                  BudgetCategory(name: 'Groceries', spent: 28),
                  BudgetCategory(name: 'Pantry', spent: 10),
                  BudgetCategory(name: 'Other', spent: 4),
                ],
              ),
              const SizedBox(height: 12),

              // Agent nudge
              const AgentNudgeCard(
                nudgeType: 'GENERAL',
                message: 'Plan this week\'s meals to keep waste low.',
              ),
              const SizedBox(height: 12),

              // Health tiles
              Row(
                children: [
                  Expanded(child: _HealthTile(
                    emoji: '⚠️',
                    count: expiring.whenOrNull(data: (items) => items.length) ?? 0,
                    label: S.expiringSoon,
                    variant: _TileVariant.alert,
                  )),
                  const SizedBox(width: 8),
                  Expanded(child: _HealthTile(
                    emoji: '📦',
                    count: lowStock.whenOrNull(data: (items) => items.length) ?? 0,
                    label: S.lowStock,
                    variant: _TileVariant.warn,
                  )),
                  const SizedBox(width: 8),
                  Expanded(child: _HealthTile(
                    emoji: '✅',
                    count: 0,
                    label: S.allGood,
                    variant: _TileVariant.healthy,
                  )),
                ],
              ),
              const SizedBox(height: 12),

              // Meal card
              AppCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Meal image area
                    Container(
                      height: 80,
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(colors: [AppColors.green3, AppColors.green2]),
                        borderRadius: BorderRadius.circular(AppRadius.sm),
                      ),
                      child: const Center(child: Text('🍽️', style: TextStyle(fontSize: 32))),
                    ),
                    const SizedBox(height: 10),
                    Text(S.tonightsRecommendation, style: TextStyle(color: AppColors.text3, fontSize: 11)),
                    const SizedBox(height: 2),
                    Text('Grilled Chicken & Rice', style: TextStyle(color: AppColors.text1, fontSize: 15, fontWeight: FontWeight.w700, letterSpacing: -0.2)),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        _Chip(label: '32g protein'),
                        const SizedBox(width: 6),
                        _Chip(label: '4 servings'),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

enum _TileVariant { alert, warn, healthy }

class _HealthTile extends StatelessWidget {
  final String emoji;
  final int count;
  final String label;
  final _TileVariant variant;

  const _HealthTile({required this.emoji, required this.count, required this.label, required this.variant});

  @override
  Widget build(BuildContext context) {
    final (bg, border) = switch (variant) {
      _TileVariant.alert when count > 0 => (AppColors.red3, AppColors.red3),
      _TileVariant.warn when count > 0 => (AppColors.amber3, AppColors.amber3),
      _ => (AppColors.surface, Colors.transparent),
    };

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: Border.all(color: border.withValues(alpha: 0.25)),
        boxShadow: AppShadows.xs,
      ),
      child: Column(
        children: [
          Text(emoji, style: const TextStyle(fontSize: 20)),
          const SizedBox(height: 4),
          Text('$count', style: TextStyle(color: AppColors.text1, fontSize: 20, fontWeight: FontWeight.w700, letterSpacing: -0.4)),
          Text(label, style: TextStyle(color: AppColors.text3, fontSize: 10, fontWeight: FontWeight.w500)),
        ],
      ),
    );
  }
}

class _Chip extends StatelessWidget {
  final String label;
  const _Chip({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.full)),
      child: Text(label, style: TextStyle(color: AppColors.text2, fontSize: 11, fontWeight: FontWeight.w500)),
    );
  }
}
