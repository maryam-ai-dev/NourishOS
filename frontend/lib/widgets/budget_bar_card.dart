import 'package:flutter/material.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import 'app_card.dart';

/// Backwards-compat: retained for widgets still importing BudgetCategory
class BudgetCategory {
  final String name;
  final double spent;
  const BudgetCategory({required this.name, required this.spent});
}

class BudgetBarCard extends StatelessWidget {
  final double weeklyLimit;
  final double totalSpent;
  final double spentPercent;
  final bool isLoading;
  final VoidCallback? onTap;

  /// Unused now (categories are shown only in budget detail screen) — kept
  /// for backwards compatibility with HomeScreen call sites.
  final List<BudgetCategory> categories;

  const BudgetBarCard({
    super.key,
    this.weeklyLimit = 0,
    this.totalSpent = 0,
    this.spentPercent = 0,
    this.isLoading = false,
    this.onTap,
    this.categories = const [],
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return AppCard(child: SizedBox(height: 40, child: Center(child: Text(S.loading, style: TextStyle(color: AppColors.text3, fontSize: 13)))));
    }
    if (weeklyLimit == 0) {
      return GestureDetector(
        onTap: onTap,
        child: AppCard(
          borderColor: AppColors.green3,
          child: Row(
            children: [
              Icon(Icons.add_circle_outline, color: AppColors.green1, size: 18),
              const SizedBox(width: 8),
              Text(S.setBudget, style: TextStyle(color: AppColors.green1, fontSize: 13, fontWeight: FontWeight.w600)),
            ],
          ),
        ),
      );
    }

    final remaining = (weeklyLimit - totalSpent).clamp(0.0, weeklyLimit);
    final pct = spentPercent.clamp(0.0, 999.0);
    final overBudget = pct > 1.0;

    return GestureDetector(
      onTap: onTap,
      child: AppCard(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Single line summary
            Text(
              '\u00a3${totalSpent.toStringAsFixed(0)} of \u00a3${weeklyLimit.toStringAsFixed(0)} spent · \u00a3${remaining.toStringAsFixed(0)} remaining',
              style: TextStyle(color: AppColors.text1, fontSize: 13, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 8),
            // Bar
            Container(
              height: 6,
              decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.full)),
              child: FractionallySizedBox(
                alignment: Alignment.centerLeft,
                widthFactor: pct.clamp(0.0, 1.0),
                child: Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(colors: overBudget ? [AppColors.red2, AppColors.red1] : [AppColors.green1, AppColors.green2]),
                    borderRadius: BorderRadius.circular(AppRadius.full),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
