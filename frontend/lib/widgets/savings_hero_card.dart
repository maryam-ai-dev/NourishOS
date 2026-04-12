import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import 'app_card.dart';

class SavingsHeroCard extends StatelessWidget {
  final double savedGbp;
  final double previousWeekGbp;
  final int wasteItems;
  final int previousWasteItems;
  final double mealsCompletedRate;
  final double totalSpent;
  final bool isLoading;

  const SavingsHeroCard({
    super.key,
    this.savedGbp = 0,
    this.previousWeekGbp = 0,
    this.wasteItems = 0,
    this.previousWasteItems = 0,
    this.mealsCompletedRate = 0,
    this.totalSpent = 0,
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return AppCard(
        child: SizedBox(
          height: 140,
          child: Center(
            child: Text(S.calculatingSavings, style: TextStyle(color: AppColors.text3, fontSize: 13)),
          ),
        ),
      );
    }

    final improved = savedGbp > previousWeekGbp;
    final totalItems = wasteItems + previousWasteItems; // simple derived total

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // Saved this week
          Text(S.savingsTitle, style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w500)),
          const SizedBox(height: 6),
          // £X.XX in large DM Serif Display
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.only(top: 10),
                child: Text('\u00a3', style: GoogleFonts.dmSans(fontSize: 22, color: AppColors.green1, fontWeight: FontWeight.w500)),
              ),
              Text(
                savedGbp.toStringAsFixed(2),
                style: GoogleFonts.dmSerifDisplay(fontSize: 48, color: AppColors.green1, height: 1),
              ),
            ],
          ),
          const SizedBox(height: 8),
          // X of Y items wasted this week
          Text(
            totalItems > 0
                ? '$wasteItems of $totalItems items wasted this week'
                : '$wasteItems items wasted this week',
            style: TextStyle(color: AppColors.text2, fontSize: 13),
          ),
          const SizedBox(height: 4),
          // Comparison line
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                improved ? Icons.trending_down : Icons.trending_up,
                size: 14,
                color: improved ? AppColors.green1 : AppColors.amber1,
              ),
              const SizedBox(width: 4),
              Text(
                improved ? 'better than last week' : 'more than last week',
                style: TextStyle(
                  color: improved ? AppColors.green1 : AppColors.amber1,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Divider(color: AppColors.surface2, height: 1),
          const SizedBox(height: 12),
          // £X spent this week
          Text(
            '\u00a3${totalSpent.toStringAsFixed(2)} spent this week',
            style: TextStyle(color: AppColors.text3, fontSize: 12, fontWeight: FontWeight.w500),
          ),
        ],
      ),
    );
  }
}
