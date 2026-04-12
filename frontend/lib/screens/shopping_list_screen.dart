import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../widgets/app_card.dart';

class ShoppingListItem {
  String name;
  String quantity;
  String category;
  bool isChecked;

  ShoppingListItem({required this.name, required this.quantity, required this.category, this.isChecked = false});
}

final shoppingListProvider = StateProvider<List<ShoppingListItem>>((ref) => [
  ShoppingListItem(name: 'Chicken Breast', quantity: '500g', category: 'Meat'),
  ShoppingListItem(name: 'Spinach', quantity: '200g', category: 'Produce'),
  ShoppingListItem(name: 'Tomatoes', quantity: '6 units', category: 'Produce'),
  ShoppingListItem(name: 'Milk', quantity: '2L', category: 'Dairy'),
  ShoppingListItem(name: 'Rice', quantity: '1kg', category: 'Grains'),
  ShoppingListItem(name: 'Cheese', quantity: '200g', category: 'Dairy'),
]);

class ShoppingListScreen extends ConsumerWidget {
  const ShoppingListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final items = ref.watch(shoppingListProvider);
    final checked = items.where((i) => i.isChecked).length;
    final total = items.length;
    final progress = total > 0 ? checked / total : 0.0;

    // Group by category
    final byCategory = <String, List<ShoppingListItem>>{};
    for (final item in items) {
      byCategory.putIfAbsent(item.category, () => []).add(item);
    }

    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(title: const Text('Shopping list')),
      body: Column(
        children: [
          // Progress bar
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('$checked of $total items found', style: TextStyle(color: AppColors.text2, fontSize: 13, fontWeight: FontWeight.w600)),
                const SizedBox(height: 6),
                Container(
                  height: 6,
                  decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.full)),
                  child: FractionallySizedBox(
                    alignment: Alignment.centerLeft,
                    widthFactor: progress,
                    child: Container(
                      decoration: BoxDecoration(
                        color: AppColors.green1,
                        borderRadius: BorderRadius.circular(AppRadius.full),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          // List
          Expanded(
            child: ListView(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              children: byCategory.entries.map((e) => Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Text(e.key.toUpperCase(),
                      style: TextStyle(color: AppColors.text3, fontSize: 11, fontWeight: FontWeight.w600, letterSpacing: 0.5)),
                  ),
                  ...e.value.map((item) => GestureDetector(
                    onTap: () {
                      ref.read(shoppingListProvider.notifier).state = [
                        for (final it in items)
                          if (it == item) (ShoppingListItem(
                            name: it.name, quantity: it.quantity, category: it.category,
                            isChecked: !it.isChecked,
                          )) else it,
                      ];
                    },
                    child: Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: AppCard(
                        child: Row(
                          children: [
                            Container(
                              width: 22, height: 22,
                              decoration: BoxDecoration(
                                color: item.isChecked ? AppColors.green1 : Colors.transparent,
                                border: Border.all(color: item.isChecked ? AppColors.green1 : AppColors.text4, width: 1.5),
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: item.isChecked ? const Icon(Icons.check, color: Colors.white, size: 14) : null,
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(item.name, style: TextStyle(
                                    color: item.isChecked ? AppColors.text3 : AppColors.text1,
                                    fontSize: 14,
                                    fontWeight: FontWeight.w600,
                                    decoration: item.isChecked ? TextDecoration.lineThrough : null,
                                  )),
                                  Text(item.quantity, style: TextStyle(color: AppColors.text3, fontSize: 12)),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  )),
                  const SizedBox(height: 4),
                ],
              )).toList(),
            ),
          ),
          // Upload receipt button
          Padding(
            padding: const EdgeInsets.all(16),
            child: SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: () {},
                style: OutlinedButton.styleFrom(
                  side: BorderSide(color: AppColors.green1),
                  foregroundColor: AppColors.green1,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                ),
                child: const Text('📷 Upload receipt when done', style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600)),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

// Supermarket connection screen (Sprint 23B.24)
class SupermarketScreen extends ConsumerWidget {
  const SupermarketScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final supermarkets = [
      {'name': 'Tesco', 'emoji': '🛒', 'connected': true},
      {'name': 'Sainsburys', 'emoji': '🛒', 'connected': false},
      {'name': 'Ocado', 'emoji': '🛒', 'connected': false},
      {'name': 'Asda', 'emoji': '🛒', 'connected': false},
      {'name': 'Waitrose', 'emoji': '🛒', 'connected': false},
    ];

    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(title: const Text('Shopping agent')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // Agent card with dark green gradient
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [Color(0xFF1A4731), Color(0xFF2D6A4F), Color(0xFF52B788)],
                begin: Alignment.topLeft, end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(AppRadius.md),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('🤖', style: TextStyle(fontSize: 28)),
                const SizedBox(height: 8),
                Text('Shopping agent', style: GoogleFonts.dmSerifDisplay(fontSize: 20, color: Colors.white)),
                const SizedBox(height: 6),
                Text(
                  'Connect your supermarket and the agent will find the best-value items matching your reorder list.',
                  style: TextStyle(color: Colors.white.withValues(alpha: 0.85), fontSize: 12, height: 1.5),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Text('Your supermarkets', style: TextStyle(color: AppColors.text3, fontSize: 11, fontWeight: FontWeight.w600, letterSpacing: 0.5)),
          const SizedBox(height: 8),
          ...supermarkets.map((s) {
            final isConnected = s['connected'] as bool;
            return Container(
              margin: const EdgeInsets.only(bottom: 10),
              child: AppCard(
                borderColor: isConnected ? AppColors.green2 : null,
                child: Row(
                  children: [
                    Text(s['emoji'] as String, style: const TextStyle(fontSize: 28)),
                    const SizedBox(width: 14),
                    Expanded(
                      child: Text(s['name'] as String, style: TextStyle(color: AppColors.text1, fontSize: 15, fontWeight: FontWeight.w600)),
                    ),
                    if (isConnected)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(color: AppColors.green4, borderRadius: BorderRadius.circular(AppRadius.full)),
                        child: Text('Connected', style: TextStyle(color: AppColors.green1, fontSize: 10, fontWeight: FontWeight.w700)),
                      )
                    else
                      TextButton(
                        onPressed: () {
                          showModalBottomSheet(
                            context: context,
                            backgroundColor: AppColors.surface,
                            builder: (_) => Padding(
                              padding: const EdgeInsets.all(24),
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text('Coming soon', style: GoogleFonts.dmSerifDisplay(fontSize: 18, color: AppColors.text1)),
                                  const SizedBox(height: 8),
                                  Text("We're working on ${s['name']} integration",
                                    style: TextStyle(color: AppColors.text3, fontSize: 13)),
                                ],
                              ),
                            ),
                          );
                        },
                        child: Text('Connect →', style: TextStyle(color: AppColors.green1, fontSize: 13, fontWeight: FontWeight.w600)),
                      ),
                  ],
                ),
              ),
            );
          }),
        ],
      ),
    );
  }
}

// Basket preview (Sprint 23B.25)
class BasketPreviewScreen extends StatelessWidget {
  const BasketPreviewScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final items = [
      {'name': 'Chicken Breast 500g', 'desc': 'Tesco Own Brand', 'price': 3.50},
      {'name': 'Extra Virgin Olive Oil 500ml', 'desc': 'Tesco Italian', 'price': 4.20},
      {'name': 'Fresh Spinach 200g', 'desc': 'Tesco Growers', 'price': 1.20},
    ];
    final total = items.fold<double>(0, (s, i) => s + (i['price'] as double));

    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(title: const Text('Your basket')),
      body: Column(
        children: [
          Expanded(
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                // Agent notes card
                AppCard(
                  borderColor: AppColors.green3,
                  child: Row(
                    children: [
                      const Text('🤖', style: TextStyle(fontSize: 24)),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Agent notes', style: TextStyle(color: AppColors.text3, fontSize: 10, fontWeight: FontWeight.w600)),
                            const SizedBox(height: 2),
                            Text('Selected own-brand where possible to save 25%. Found in-season spinach at the best price.',
                              style: TextStyle(color: AppColors.text1, fontSize: 12, height: 1.5)),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                ...items.map((item) => Container(
                  margin: const EdgeInsets.only(bottom: 10),
                  child: AppCard(
                    child: Row(
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(item['name'] as String, style: TextStyle(color: AppColors.text1, fontSize: 13, fontWeight: FontWeight.w600)),
                              Text(item['desc'] as String, style: TextStyle(color: AppColors.text3, fontSize: 11)),
                            ],
                          ),
                        ),
                        Text('\u00a3${(item['price'] as double).toStringAsFixed(2)}',
                          style: TextStyle(color: AppColors.text1, fontSize: 14, fontWeight: FontWeight.w700)),
                      ],
                    ),
                  ),
                )),
                const SizedBox(height: 8),
                AppCard(
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('Total', style: TextStyle(color: AppColors.text1, fontSize: 15, fontWeight: FontWeight.w600)),
                      Text('\u00a3${total.toStringAsFixed(2)}',
                        style: GoogleFonts.dmSerifDisplay(fontSize: 22, color: AppColors.green1)),
                    ],
                  ),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text("Order placed — we'll update your pantry when it arrives 📦")),
                  );
                  Navigator.of(context).pop();
                },
                style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
                child: const Text('Confirm & place order →', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
