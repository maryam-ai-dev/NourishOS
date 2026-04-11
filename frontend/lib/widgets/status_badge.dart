import 'package:flutter/material.dart';
import '../config/app_theme.dart';

enum BadgeVariant { fresh, near, expired, urgent, low, recurringWaste }

class StatusBadge extends StatelessWidget {
  final String label;
  final BadgeVariant variant;

  const StatusBadge({super.key, required this.label, required this.variant});

  @override
  Widget build(BuildContext context) {
    final (bg, fg) = _colors;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.full),
      ),
      child: Text(
        label.toUpperCase(),
        style: TextStyle(color: fg, fontSize: 10, fontWeight: FontWeight.w600, letterSpacing: 0.5),
      ),
    );
  }

  (Color, Color) get _colors => switch (variant) {
    BadgeVariant.fresh => (AppColors.green4, AppColors.green1),
    BadgeVariant.near => (AppColors.amber3, AppColors.amber1),
    BadgeVariant.expired => (AppColors.red3, AppColors.red1),
    BadgeVariant.urgent => (AppColors.red3, AppColors.red1),
    BadgeVariant.low => (AppColors.amber3, AppColors.amber1),
    BadgeVariant.recurringWaste => (AppColors.red3, AppColors.red1),
  };
}
