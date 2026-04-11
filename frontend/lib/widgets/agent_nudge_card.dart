import 'package:flutter/material.dart';
import '../config/app_theme.dart';
import 'app_card.dart';

class AgentNudgeCard extends StatelessWidget {
  final String? nudgeType;
  final String? message;
  final VoidCallback? onTap;

  const AgentNudgeCard({super.key, this.nudgeType, this.message, this.onTap});

  @override
  Widget build(BuildContext context) {
    if (message == null || message!.isEmpty) return const SizedBox.shrink();

    return GestureDetector(
      onTap: onTap,
      child: AppCard(
        borderColor: AppColors.green4,
        child: Row(
          children: [
            Container(
              width: 36, height: 36,
              decoration: BoxDecoration(color: AppColors.green5, borderRadius: BorderRadius.circular(AppRadius.sm)),
              child: const Center(child: Text('💡', style: TextStyle(fontSize: 18))),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(message!, style: TextStyle(color: AppColors.text1, fontSize: 13, fontWeight: FontWeight.w600)),
            ),
            Icon(Icons.chevron_right, color: AppColors.text4, size: 20),
          ],
        ),
      ),
    );
  }
}
