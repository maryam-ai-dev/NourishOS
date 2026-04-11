import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';

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
      return Container(
        height: 180,
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            begin: Alignment.topLeft, end: Alignment.bottomRight,
            colors: [Color(0xFF1A4731), Color(0xFF2D6A4F), Color(0xFF52B788)],
          ),
          borderRadius: BorderRadius.circular(AppRadius.lg),
        ),
        child: Center(child: Text(S.calculatingSavings, style: TextStyle(color: Colors.white.withValues(alpha: 0.7), fontSize: 14))),
      );
    }

    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft, end: Alignment.bottomRight,
          colors: [Color(0xFF1A4731), Color(0xFF2D6A4F), Color(0xFF52B788)],
        ),
        borderRadius: BorderRadius.circular(AppRadius.lg),
      ),
      child: Stack(
        children: [
          // Decorative circles
          Positioned(top: -20, right: -20, child: Container(width: 80, height: 80, decoration: BoxDecoration(shape: BoxShape.circle, color: Colors.white.withValues(alpha: 0.06)))),
          Positioned(bottom: -30, left: -10, child: Container(width: 60, height: 60, decoration: BoxDecoration(shape: BoxShape.circle, color: Colors.white.withValues(alpha: 0.04)))),
          // Content
          Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(S.savingsTitle, style: TextStyle(color: Colors.white.withValues(alpha: 0.75), fontSize: 12, fontWeight: FontWeight.w500)),
                const SizedBox(height: 4),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('\u00a3', style: GoogleFonts.dmSans(fontSize: 20, color: Colors.white.withValues(alpha: 0.75))),
                    Text(savedGbp.toStringAsFixed(2), style: GoogleFonts.dmSerifDisplay(fontSize: 42, color: Colors.white, height: 1)),
                  ],
                ),
                const SizedBox(height: 4),
                Text('${S.vsLastWeek} \u00a3${previousWeekGbp.toStringAsFixed(2)}',
                  style: TextStyle(color: Colors.white.withValues(alpha: 0.6), fontSize: 12)),
                const SizedBox(height: 12),
                Divider(color: Colors.white.withValues(alpha: 0.15), height: 1),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    _Stat(label: S.itemsWasted, value: '$wasteItems', prev: '$previousWasteItems'),
                    _Stat(label: S.mealsCompleted, value: '${(mealsCompletedRate * 100).toInt()}%'),
                    _Stat(label: S.totalSpent, value: '\u00a3${totalSpent.toStringAsFixed(0)}'),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _Stat extends StatelessWidget {
  final String label;
  final String value;
  final String? prev;
  const _Stat({required this.label, required this.value, this.prev});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(value, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w700)),
        if (prev != null) Text('from $prev', style: TextStyle(color: Colors.white.withValues(alpha: 0.5), fontSize: 10)),
        Text(label, style: TextStyle(color: Colors.white.withValues(alpha: 0.6), fontSize: 10)),
      ],
    );
  }
}
