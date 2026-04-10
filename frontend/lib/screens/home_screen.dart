import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/providers.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final expiring = ref.watch(inventoryExpiringProvider);
    final lowStock = ref.watch(inventoryLowStockProvider);

    return Scaffold(
      backgroundColor: const Color(0xFF0B0B0F),
      appBar: AppBar(
        title: const Text('NourishOS'),
        backgroundColor: const Color(0xFF121218),
        foregroundColor: Colors.white,
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(inventoryExpiringProvider);
          ref.invalidate(inventoryLowStockProvider);
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Food Health Summary
            _SummaryCard(
              title: 'Food Health',
              children: [
                _SummaryRow(
                  icon: Icons.warning_amber,
                  label: 'Expiring soon',
                  value: expiring.when(
                    data: (items) => '${items.length}',
                    loading: () => '...',
                    error: (_, _) => '--',
                  ),
                  color: Colors.amber,
                ),
                _SummaryRow(
                  icon: Icons.inventory_2_outlined,
                  label: 'Low stock (ParLevel)',
                  value: lowStock.when(
                    data: (items) => '${items.length}',
                    loading: () => '...',
                    error: (_, _) => '--',
                  ),
                  color: Colors.redAccent,
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Tonight's Recommendation
            _SummaryCard(
              title: "Tonight's Recommendation",
              children: [
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Text(
                    'Connect to Spring Boot to see recommendations',
                    style: TextStyle(color: Colors.white54, fontSize: 14),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Today's Schedule
            _SummaryCard(
              title: "Today's Plan",
              children: [
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 8),
                  child: Text(
                    'No meal planned yet',
                    style: TextStyle(color: Colors.white54, fontSize: 14),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _SummaryCard extends StatelessWidget {
  final String title;
  final List<Widget> children;

  const _SummaryCard({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF121218),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withValues(alpha: 0.08)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600)),
          const SizedBox(height: 12),
          ...children,
        ],
      ),
    );
  }
}

class _SummaryRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final Color color;

  const _SummaryRow({required this.icon, required this.label, required this.value, required this.color});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 8),
          Text(label, style: const TextStyle(color: Colors.white70, fontSize: 14)),
          const Spacer(),
          Text(value, style: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}
