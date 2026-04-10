import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/providers.dart';

class InventoryAlertBanner extends ConsumerWidget {
  final VoidCallback? onTapLowStock;
  final VoidCallback? onTapExpiring;

  const InventoryAlertBanner({super.key, this.onTapLowStock, this.onTapExpiring});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final lowStock = ref.watch(inventoryLowStockProvider);
    final expiring = ref.watch(inventoryExpiringProvider);

    final lowCount = lowStock.whenOrNull(data: (items) => items.length) ?? 0;
    final expiringCount = expiring.whenOrNull(data: (items) => items.length) ?? 0;

    if (lowCount == 0 && expiringCount == 0) return const SizedBox.shrink();

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.amber.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.amber.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          const Icon(Icons.warning_amber, color: Colors.amber, size: 20),
          const SizedBox(width: 8),
          if (lowCount > 0)
            GestureDetector(
              onTap: onTapLowStock,
              child: Text('$lowCount low stock', style: const TextStyle(color: Colors.amber, fontSize: 13)),
            ),
          if (lowCount > 0 && expiringCount > 0)
            const Text('  ·  ', style: TextStyle(color: Colors.white24)),
          if (expiringCount > 0)
            GestureDetector(
              onTap: onTapExpiring,
              child: Text('$expiringCount expiring', style: const TextStyle(color: Colors.redAccent, fontSize: 13)),
            ),
        ],
      ),
    );
  }
}
