import 'package:flutter/material.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import 'app_card.dart';

class BudgetBarCard extends StatelessWidget {
  final double weeklyLimit;
  final double totalSpent;
  final double spentPercent;
  final List<BudgetCategory> categories;
  final bool isLoading;

  const BudgetBarCard({
    super.key,
    this.weeklyLimit = 0,
    this.totalSpent = 0,
    this.spentPercent = 0,
    this.categories = const [],
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return AppCard(child: SizedBox(height: 60, child: Center(child: Text(S.loading, style: TextStyle(color: AppColors.text3, fontSize: 13)))));
    }
    if (weeklyLimit == 0) {
      return AppCard(child: Center(child: Text(S.setBudget, style: TextStyle(color: AppColors.green1, fontSize: 13, fontWeight: FontWeight.w600))));
    }

    final pct = spentPercent.clamp(0.0, 999.0);
    final barColor = pct > 1.0 ? AppColors.red2 : AppColors.green1;

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text(S.budgetTitle, style: TextStyle(color: AppColors.text1, fontSize: 13, fontWeight: FontWeight.w600)),
                Text('${(pct * 100).toInt()}% ${S.remaining}', style: TextStyle(color: AppColors.text3, fontSize: 11)),
              ]),
              Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
                Text('\u00a3${totalSpent.toStringAsFixed(2)}', style: TextStyle(color: AppColors.text1, fontSize: 17, fontWeight: FontWeight.w700)),
                Text('of \u00a3${weeklyLimit.toStringAsFixed(0)}', style: TextStyle(color: AppColors.text3, fontSize: 11)),
              ]),
            ],
          ),
          const SizedBox(height: 10),
          // Budget bar
          Container(
            height: 6,
            decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.full)),
            child: FractionallySizedBox(
              alignment: Alignment.centerLeft,
              widthFactor: pct.clamp(0.0, 1.0),
              child: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(colors: [barColor, AppColors.green2]),
                  borderRadius: BorderRadius.circular(AppRadius.full),
                ),
              ),
            ),
          ),
          if (categories.isNotEmpty) ...[
            const SizedBox(height: 12),
            Row(
              children: categories.map((c) => Expanded(
                child: Container(
                  margin: const EdgeInsets.symmetric(horizontal: 3),
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 8),
                  decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.sm)),
                  child: Column(children: [
                    Text('\u00a3${c.spent.toStringAsFixed(0)}', style: TextStyle(color: AppColors.text1, fontSize: 13, fontWeight: FontWeight.w600)),
                    Text(c.name, style: TextStyle(color: AppColors.text3, fontSize: 10)),
                  ]),
                ),
              )).toList(),
            ),
          ],
        ],
      ),
    );
  }
}

class BudgetCategory {
  final String name;
  final double spent;
  const BudgetCategory({required this.name, required this.spent});
}
