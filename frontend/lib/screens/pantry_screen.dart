import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';

/// Lot model for pantry display.
class _Lot {
  final String id;
  final String ingredientName;
  final double quantity;
  final String unit;
  final String location; // FRIDGE, FREEZER, PANTRY, COUNTER, OPENED
  final String freshness; // FRESH, NEAR_EXPIRY, EXPIRED
  final bool isOpen;
  final bool isDepleted;
  final String? purchasedAt;
  final bool isRecurringWaste;
  final bool belowParLevel;

  _Lot({
    required this.id, required this.ingredientName, required this.quantity,
    required this.unit, required this.location, required this.freshness,
    this.isOpen = false, this.isDepleted = false, this.purchasedAt,
    this.isRecurringWaste = false, this.belowParLevel = false,
  });
}

/// Demo data provider (in production, fetched from Spring Boot).
final pantryLotsProvider = FutureProvider<List<_Lot>>((ref) async {
  // In production: call AuthorityService().getInventorySnapshot()
  // For now, return demo data to prove the UI structure works
  return [
    _Lot(id: '1', ingredientName: 'Chicken Breast', quantity: 400, unit: 'g',
         location: 'FRIDGE', freshness: 'NEAR_EXPIRY', purchasedAt: '2026-04-07'),
    _Lot(id: '2', ingredientName: 'Spinach', quantity: 100, unit: 'g',
         location: 'FRIDGE', freshness: 'FRESH', isRecurringWaste: true),
    _Lot(id: '3', ingredientName: 'Frozen Peas', quantity: 500, unit: 'g',
         location: 'FREEZER', freshness: 'FRESH'),
    _Lot(id: '4', ingredientName: 'Rice', quantity: 200, unit: 'g',
         location: 'PANTRY', freshness: 'FRESH', belowParLevel: true),
    _Lot(id: '5', ingredientName: 'Olive Oil', quantity: 150, unit: 'ml',
         location: 'PANTRY', freshness: 'FRESH', isOpen: true),
    _Lot(id: '6', ingredientName: 'Expired Yogurt', quantity: 0, unit: 'g',
         location: 'FRIDGE', freshness: 'EXPIRED', isDepleted: true),
  ];
});

class PantryScreen extends ConsumerWidget {
  const PantryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final lotsAsync = ref.watch(pantryLotsProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text(S.pantryTitle),
      ),
      body: lotsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(child: Text('Error: $err', style: const TextStyle(color: AppColors.red1))),
        data: (lots) {
          final fridge = lots.where((l) => l.location == 'FRIDGE' && !l.isDepleted).toList();
          final freezer = lots.where((l) => l.location == 'FREEZER' && !l.isDepleted).toList();
          final pantry = lots.where((l) => l.location == 'PANTRY' && !l.isDepleted).toList();
          final opened = lots.where((l) => l.isOpen && !l.isDepleted).toList();

          // Sort near-expiry first
          int freshSort(_Lot a, _Lot b) {
            const order = {'EXPIRED': 0, 'NEAR_EXPIRY': 1, 'FRESH': 2};
            return (order[a.freshness] ?? 3).compareTo(order[b.freshness] ?? 3);
          }
          fridge.sort(freshSort);
          freezer.sort(freshSort);

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // Alert banner
              _AlertBanner(lots: lots),
              const SizedBox(height: 12),
              _ZoneSection(title: S.fridge, icon: Icons.kitchen, lots: fridge),
              _ZoneSection(title: S.freezer, icon: Icons.ac_unit, lots: freezer),
              _ZoneSection(title: S.pantry, icon: Icons.shelves, lots: pantry),
              _ZoneSection(title: S.opened, icon: Icons.lock_open, lots: opened),
              if (lots.isEmpty)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.all(32),
                    child: Text(S.noPantryItems, style: TextStyle(color: AppColors.text4, fontSize: 16)),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}

class _AlertBanner extends StatelessWidget {
  final List<_Lot> lots;
  const _AlertBanner({required this.lots});

  @override
  Widget build(BuildContext context) {
    final lowStock = lots.where((l) => l.belowParLevel && !l.isDepleted).length;
    final nearExpiry = lots.where((l) => l.freshness == 'NEAR_EXPIRY' || l.freshness == 'EXPIRED').length;

    if (lowStock == 0 && nearExpiry == 0) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.amber1.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.amber1.withValues(alpha: 0.3)),
      ),
      child: Row(
        children: [
          const Icon(Icons.warning_amber, color: AppColors.amber1, size: 20),
          const SizedBox(width: 8),
          if (lowStock > 0)
            Text('$lowStock ${S.lowStock}', style: const TextStyle(color: AppColors.amber1, fontSize: 13)),
          if (lowStock > 0 && nearExpiry > 0)
            const Text('  ·  ', style: TextStyle(color: AppColors.text4)),
          if (nearExpiry > 0)
            Text('$nearExpiry ${S.expiringSoon}', style: const TextStyle(color: AppColors.red1, fontSize: 13)),
        ],
      ),
    );
  }
}

class _ZoneSection extends StatelessWidget {
  final String title;
  final IconData icon;
  final List<_Lot> lots;
  const _ZoneSection({required this.title, required this.icon, required this.lots});

  @override
  Widget build(BuildContext context) {
    if (lots.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: Row(
            children: [
              Icon(icon, color: AppColors.text4, size: 18),
              const SizedBox(width: 6),
              Text(title, style: const TextStyle(color: AppColors.text3, fontSize: 13, fontWeight: FontWeight.w600)),
              const SizedBox(width: 6),
              Text('(${lots.length})', style: const TextStyle(color: AppColors.text4, fontSize: 12)),
            ],
          ),
        ),
        ...lots.map((lot) => _LotCard(lot: lot)),
        const SizedBox(height: 8),
      ],
    );
  }
}

class _LotCard extends StatelessWidget {
  final _Lot lot;
  const _LotCard({required this.lot});

  Color get _freshnessColor {
    switch (lot.freshness) {
      case 'EXPIRED': return AppColors.red1;
      case 'NEAR_EXPIRY': return AppColors.amber1;
      default: return AppColors.green2;
    }
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onLongPress: () => _showParLevelEditor(context),
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: AppColors.surface2),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(lot.ingredientName, style: const TextStyle(color: AppColors.text1, fontSize: 14)),
                  const SizedBox(height: 4),
                  Text('${lot.quantity} ${lot.unit}', style: const TextStyle(color: AppColors.text3, fontSize: 12)),
                ],
              ),
            ),
            // Badges
            Wrap(
              spacing: 4,
              children: [
                _FreshBadge(label: lot.freshness, color: _freshnessColor),
                if (lot.isOpen) const _FreshBadge(label: 'OPENED', color: AppColors.green2),
                if (lot.isRecurringWaste) const _FreshBadge(label: 'WASTE', color: AppColors.red1),
                if (lot.belowParLevel) const _FreshBadge(label: 'LOW', color: AppColors.amber1),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showParLevelEditor(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface2,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (_) => _ParLevelForm(ingredientName: lot.ingredientName, lotId: lot.id),
    );
  }
}

class _FreshBadge extends StatelessWidget {
  final String label;
  final Color color;
  const _FreshBadge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.15), borderRadius: BorderRadius.circular(6)),
      child: Text(label, style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w600)),
    );
  }
}

class _ParLevelForm extends StatefulWidget {
  final String ingredientName;
  final String lotId;
  const _ParLevelForm({required this.ingredientName, required this.lotId});

  @override
  State<_ParLevelForm> createState() => _ParLevelFormState();
}

class _ParLevelFormState extends State<_ParLevelForm> {
  final _minCtrl = TextEditingController();
  final _prefCtrl = TextEditingController();
  String? _error;

  @override
  void dispose() { _minCtrl.dispose(); _prefCtrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(left: 16, right: 16, top: 24, bottom: MediaQuery.of(context).viewInsets.bottom + 16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('${S.yourUsualAmount} — ${widget.ingredientName}',
            style: const TextStyle(color: AppColors.text1, fontSize: 16, fontWeight: FontWeight.w600)),
          const SizedBox(height: 16),
          TextField(
            controller: _minCtrl,
            keyboardType: TextInputType.number,
            style: const TextStyle(color: AppColors.text1),
            decoration: const InputDecoration(
              labelText: 'Minimum Quantity',
              labelStyle: TextStyle(color: AppColors.text3),
              enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
              focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _prefCtrl,
            keyboardType: TextInputType.number,
            style: const TextStyle(color: AppColors.text1),
            decoration: const InputDecoration(
              labelText: 'Preferred Quantity',
              labelStyle: TextStyle(color: AppColors.text3),
              enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
              focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
            ),
          ),
          if (_error != null) ...[
            const SizedBox(height: 8),
            Text(_error!, style: const TextStyle(color: AppColors.red1, fontSize: 12)),
          ],
          const SizedBox(height: 20),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _save,
              child: const Text('Save Par Level'),
            ),
          ),
        ],
      ),
    );
  }

  void _save() {
    final minVal = double.tryParse(_minCtrl.text.trim());
    final prefVal = double.tryParse(_prefCtrl.text.trim());

    if (minVal == null || prefVal == null) {
      setState(() => _error = 'Both values must be numbers');
      return;
    }
    if (minVal > prefVal) {
      setState(() => _error = 'Minimum must be \u2264 preferred');
      return;
    }
    setState(() => _error = null);

    // In production: call AuthorityService PUT /households/{id}/par-levels/{ingredientId}
    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Par level saved')));
  }
}
