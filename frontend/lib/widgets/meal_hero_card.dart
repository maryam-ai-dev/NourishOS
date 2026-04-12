import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';

class MealHeroCard extends StatelessWidget {
  final String mealName;
  final int matchScore;
  final int memberCount;
  final String emoji;
  final bool hasExpiringIngredient;
  final String? expiringNote;
  final VoidCallback? onStartCooking;

  const MealHeroCard({
    super.key,
    required this.mealName,
    this.matchScore = 85,
    this.memberCount = 2,
    this.emoji = '🍽️',
    this.hasExpiringIngredient = false,
    this.expiringNote,
    this.onStartCooking,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.md),
        boxShadow: AppShadows.xs,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 120px image area with gradient
          Stack(
            children: [
              Container(
                height: 120,
                decoration: BoxDecoration(
                  gradient: const LinearGradient(
                    colors: [AppColors.green3, AppColors.green2],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: const BorderRadius.vertical(top: Radius.circular(AppRadius.md)),
                ),
                child: Center(child: Text(emoji, style: const TextStyle(fontSize: 48))),
              ),
              // Score bubble top-right
              Positioned(
                top: 10, right: 10,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.surface,
                    borderRadius: BorderRadius.circular(AppRadius.full),
                  ),
                  child: Text(
                    '$matchScore% match',
                    style: TextStyle(color: AppColors.green1, fontSize: 11, fontWeight: FontWeight.w700),
                  ),
                ),
              ),
              // Member count badge top-left
              Positioned(
                top: 10, left: 10,
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppColors.surface.withValues(alpha: 0.9),
                    borderRadius: BorderRadius.circular(AppRadius.full),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('👥', style: TextStyle(fontSize: 11)),
                      const SizedBox(width: 4),
                      Text('$memberCount', style: TextStyle(color: AppColors.text1, fontSize: 11, fontWeight: FontWeight.w600)),
                    ],
                  ),
                ),
              ),
            ],
          ),
          // Amber alert strip when expiring ingredient
          if (hasExpiringIngredient && expiringNote != null)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
              color: AppColors.amber3,
              child: Row(
                children: [
                  const Text('⚠️', style: TextStyle(fontSize: 12)),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      expiringNote!,
                      style: TextStyle(color: AppColors.amber1, fontSize: 11, fontWeight: FontWeight.w600),
                    ),
                  ),
                ],
              ),
            ),
          // Meal name + CTA
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Tonight',
                  style: TextStyle(color: AppColors.text3, fontSize: 11, fontWeight: FontWeight.w500),
                ),
                const SizedBox(height: 4),
                Text(
                  mealName,
                  style: GoogleFonts.dmSerifDisplay(fontSize: 20, color: AppColors.text1, letterSpacing: -0.2),
                ),
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: onStartCooking,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    child: const Text('Start cooking →', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
