import 'package:flutter/material.dart';
import '../config/app_theme.dart';

class AppCard extends StatelessWidget {
  final Widget child;
  final Color? borderColor;
  final EdgeInsetsGeometry? padding;

  const AppCard({super.key, required this.child, this.borderColor, this.padding});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding ?? const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.md),
        border: borderColor != null ? Border.all(color: borderColor!, width: 1) : null,
        boxShadow: AppShadows.xs,
      ),
      child: child,
    );
  }
}
