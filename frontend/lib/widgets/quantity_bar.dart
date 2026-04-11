import 'package:flutter/material.dart';
import '../config/app_theme.dart';

class QuantityBar extends StatelessWidget {
  final double fillRatio; // [0, 1]

  const QuantityBar({super.key, required this.fillRatio});

  Color get _fillColor {
    if (fillRatio >= 0.5) return AppColors.green2;
    if (fillRatio >= 0.2) return AppColors.amber2;
    return AppColors.red2;
  }

  @override
  Widget build(BuildContext context) {
    final clamped = fillRatio.clamp(0.0, 1.0);
    return Container(
      height: 4,
      decoration: BoxDecoration(
        color: AppColors.surface2,
        borderRadius: BorderRadius.circular(AppRadius.full),
      ),
      child: FractionallySizedBox(
        alignment: Alignment.centerLeft,
        widthFactor: clamped,
        child: Container(
          decoration: BoxDecoration(
            color: _fillColor,
            borderRadius: BorderRadius.circular(AppRadius.full),
          ),
        ),
      ),
    );
  }
}
