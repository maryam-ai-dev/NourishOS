import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../config/app_theme.dart';
import '../widgets/app_card.dart';

/// Sprint 23B.20: Receipt confirm screen
class ReceiptItemState {
  String name;
  double quantity;
  String unit;
  bool requiresReview;
  bool included;

  ReceiptItemState({
    required this.name, required this.quantity, required this.unit,
    this.requiresReview = false, this.included = true,
  });
}

final scannedItemsProvider = StateProvider<List<ReceiptItemState>>((ref) => [
  ReceiptItemState(name: 'Chicken Breast', quantity: 500, unit: 'g'),
  ReceiptItemState(name: 'Olive Oil', quantity: 500, unit: 'ml'),
  ReceiptItemState(name: 'Mystery Item', quantity: 1, unit: 'unit', requiresReview: true),
]);

class ReceiptConfirmScreen extends ConsumerWidget {
  const ReceiptConfirmScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final items = ref.watch(scannedItemsProvider);
    final includedItems = items.where((i) => i.included).toList();
    final canConfirm = includedItems.isNotEmpty && includedItems.every((i) => i.name.trim().isNotEmpty);

    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        title: const Text('Confirm receipt items'),
      ),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: items.length,
              itemBuilder: (context, i) {
                final item = items[i];
                return Container(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: AppCard(
                    borderColor: item.requiresReview ? AppColors.amber1 : null,
                    child: Row(
                      children: [
                        Checkbox(
                          value: item.included,
                          activeColor: AppColors.green1,
                          onChanged: (v) {
                            ref.read(scannedItemsProvider.notifier).state = [
                              for (final it in items)
                                if (it == item) (ReceiptItemState(
                                  name: it.name, quantity: it.quantity, unit: it.unit,
                                  requiresReview: it.requiresReview, included: v ?? false,
                                )) else it,
                            ];
                          },
                        ),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(item.name, style: TextStyle(color: AppColors.text1, fontSize: 14, fontWeight: FontWeight.w600)),
                              Text('${item.quantity} ${item.unit}', style: TextStyle(color: AppColors.text3, fontSize: 12)),
                              if (item.requiresReview)
                                Padding(
                                  padding: const EdgeInsets.only(top: 4),
                                  child: Text('⚠️ Please review', style: TextStyle(color: AppColors.amber1, fontSize: 10, fontWeight: FontWeight.w600)),
                                ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: canConfirm ? () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(content: Text('${includedItems.length} items added to pantry 🎉')),
                  );
                  context.go('/pantry');
                } : null,
                style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
                child: Text(
                  'Add ${includedItems.length} item${includedItems.length == 1 ? "" : "s"} to pantry',
                  style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
