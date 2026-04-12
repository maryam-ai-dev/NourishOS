import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../config/strings.dart';
import '../services/intelligence_service.dart';
import '../services/service_exception.dart';
import '../providers/providers.dart';

const _mealTypes = ['Breakfast', 'Lunch', 'Dinner'];
const _mealEmojis = {'Breakfast': '🍳', 'Lunch': '🥗', 'Dinner': '🍽️'};

final weeklyPlanProposalProvider = StateProvider<Map<String, dynamic>?>((ref) => null);
final selectedMemberProvider = StateProvider<String?>((ref) => null);

class WeeklyPlannerScreen extends ConsumerStatefulWidget {
  const WeeklyPlannerScreen({super.key});

  @override
  ConsumerState<WeeklyPlannerScreen> createState() => _WeeklyPlannerScreenState();
}

class _WeeklyPlannerScreenState extends ConsumerState<WeeklyPlannerScreen> {
  bool _generating = false;

  @override
  Widget build(BuildContext context) {
    final proposal = ref.watch(weeklyPlanProposalProvider);
    final slots = (proposal?['slots'] as List?) ?? [];
    final hasPlan = slots.isNotEmpty;

    return Scaffold(
      appBar: AppBar(title: Text(S.plannerTitle)),
      body: Column(
        children: [
          // Member filter chips (Sprint 23B.6)
          _MemberFilterChips(),
          // Horizontal scrolling day grid (Sprint 23B.5)
          Expanded(child: _HorizontalPlannerGrid(slots: slots)),
          // Generate / Regenerate button (Sprint 23B.7)
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: _generating ? null : _generatePlan,
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 14),
                    ),
                    child: _generating
                        ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                        : Text(
                            hasPlan ? '🔄 Regenerate whole week' : '✨ Generate my week\'s plan',
                            style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
                          ),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  "Based on your household's preferences, allergies, and what's in your fridge",
                  textAlign: TextAlign.center,
                  style: TextStyle(color: AppColors.text3, fontSize: 11),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _generatePlan() async {
    setState(() => _generating = true);
    try {
      final householdId = ref.read(householdIdProvider);
      final now = DateTime.now();
      final monday = now.subtract(Duration(days: now.weekday - 1));
      final result = await IntelligenceService().planWeekly({
        'householdId': householdId,
        'weekStartDate': '${monday.year}-${monday.month.toString().padLeft(2, '0')}-${monday.day.toString().padLeft(2, '0')}',
      });
      ref.read(weeklyPlanProposalProvider.notifier).state = result;
    } on ServiceException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('${S.error}: ${e.message}')));
      }
    } finally {
      if (mounted) setState(() => _generating = false);
    }
  }
}

// --- Sprint 23B.6: Member filter chips ---
class _MemberFilterChips extends ConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selected = ref.watch(selectedMemberProvider);
    final chips = ['Everyone', 'Maryam', 'Ahmed', 'Zara'];

    return Container(
      height: 48,
      padding: const EdgeInsets.symmetric(horizontal: 12),
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: chips.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, i) {
          final name = chips[i];
          final isSelected = (selected == null && i == 0) || selected == name;
          return GestureDetector(
            onTap: () {
              ref.read(selectedMemberProvider.notifier).state = i == 0 ? null : name;
            },
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
              decoration: BoxDecoration(
                color: isSelected ? AppColors.green1 : AppColors.surface,
                borderRadius: BorderRadius.circular(AppRadius.full),
                border: Border.all(color: isSelected ? AppColors.green1 : AppColors.surface2),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (i > 0) const Text('👤', style: TextStyle(fontSize: 11)),
                  if (i > 0) const SizedBox(width: 4),
                  Text(name, style: TextStyle(
                    color: isSelected ? Colors.white : AppColors.text2,
                    fontSize: 12, fontWeight: FontWeight.w600,
                  )),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}

// --- Sprint 23B.5: Horizontal scrolling planner grid ---
class _HorizontalPlannerGrid extends StatelessWidget {
  final List<dynamic> slots;
  const _HorizontalPlannerGrid({required this.slots});

  Map<String, dynamic>? _findSlot(int day, String mealType) {
    final mt = mealType.toUpperCase();
    for (final s in slots) {
      if (s['day'] == day && s['mealType'] == mt) return s as Map<String, dynamic>;
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    final monday = now.subtract(Duration(days: now.weekday - 1));
    final todayIdx = now.weekday;

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12),
      child: Row(
        children: List.generate(7, (i) {
          final day = i + 1;
          final date = monday.add(Duration(days: i));
          final dayName = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'][i];
          final isToday = day == todayIdx;

          return Container(
            width: 130,
            margin: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Day header
                Container(
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 10),
                  child: Row(
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(dayName, style: TextStyle(color: AppColors.text3, fontSize: 11, fontWeight: FontWeight.w600)),
                          Text('${date.day}', style: GoogleFonts.dmSerifDisplay(fontSize: 18, color: AppColors.text1)),
                        ],
                      ),
                      if (isToday) ...[
                        const SizedBox(width: 6),
                        Container(width: 6, height: 6, decoration: const BoxDecoration(color: AppColors.green2, shape: BoxShape.circle)),
                      ],
                    ],
                  ),
                ),
                const SizedBox(height: 4),
                // 3 meal slots
                for (final mealType in _mealTypes)
                  _DaySlot(slot: _findSlot(day, mealType), mealType: mealType),
              ],
            ),
          );
        }),
      ),
    );
  }
}

class _DaySlot extends StatelessWidget {
  final Map<String, dynamic>? slot;
  final String mealType;
  const _DaySlot({this.slot, required this.mealType});

  @override
  Widget build(BuildContext context) {
    final emoji = _mealEmojis[mealType] ?? '🍽️';
    final isEmpty = slot == null;

    if (isEmpty) {
      return GestureDetector(
        onTap: () {},
        child: Container(
          margin: const EdgeInsets.only(bottom: 8),
          height: 70,
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(AppRadius.sm),
            border: Border.all(color: AppColors.surface2, style: BorderStyle.solid, width: 1.5),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.add, color: AppColors.text4, size: 20),
              const SizedBox(height: 2),
              Text(mealType, style: TextStyle(color: AppColors.text4, fontSize: 9)),
            ],
          ),
        ),
      );
    }

    final mealName = (slot!['mealName'] as String?) ?? '';

    return GestureDetector(
      onTap: () => _showSwapPanel(context),
      child: Container(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(8),
        height: 70,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(AppRadius.sm),
          boxShadow: AppShadows.xs,
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(emoji, style: const TextStyle(fontSize: 14)),
            const SizedBox(height: 2),
            Expanded(
              child: Text(
                mealName,
                style: TextStyle(color: AppColors.text1, fontSize: 10, fontWeight: FontWeight.w500),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // Sprint 23B.8 — slide-up swap panel
  void _showSwapPanel(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(AppRadius.lg))),
      isScrollControlled: true,
      builder: (_) => _SwapPanel(currentMealName: slot?['mealName'] as String? ?? ''),
    );
  }
}

class _SwapPanel extends StatelessWidget {
  final String currentMealName;
  const _SwapPanel({required this.currentMealName});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 20, right: 20, top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Swap this meal', style: GoogleFonts.dmSerifDisplay(fontSize: 20, color: AppColors.text1)),
          const SizedBox(height: 4),
          Text('Currently: $currentMealName', style: TextStyle(color: AppColors.text3, fontSize: 12)),
          const SizedBox(height: 16),
          Text('Alternatives', style: TextStyle(color: AppColors.text3, fontSize: 11, fontWeight: FontWeight.w600)),
          const SizedBox(height: 8),
          _AlternativeCard(
            emoji: '🥘', name: 'Chicken Biryani', score: 88,
            reason: 'Uses spinach expiring today',
          ),
          const SizedBox(height: 8),
          _AlternativeCard(
            emoji: '🍝', name: 'Pasta Primavera', score: 82,
            reason: "Aisha's favourite",
          ),
          const SizedBox(height: 8),
          _AlternativeCard(
            emoji: '🥗', name: 'Greek Salad', score: 76,
            reason: 'Quick — ready in 15 minutes',
          ),
          const SizedBox(height: 16),
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: Text('Choose my own meal instead', style: TextStyle(color: AppColors.green1, fontSize: 12)),
          ),
        ],
      ),
    );
  }
}

class _AlternativeCard extends StatelessWidget {
  final String emoji;
  final String name;
  final int score;
  final String reason;
  const _AlternativeCard({required this.emoji, required this.name, required this.score, required this.reason});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => Navigator.of(context).pop(),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(AppRadius.md),
          border: Border.all(color: AppColors.surface2),
        ),
        child: Row(
          children: [
            Text(emoji, style: const TextStyle(fontSize: 24)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(child: Text(name, style: TextStyle(color: AppColors.text1, fontSize: 14, fontWeight: FontWeight.w600))),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(color: AppColors.green4, borderRadius: BorderRadius.circular(AppRadius.full)),
                        child: Text('$score%', style: TextStyle(color: AppColors.green1, fontSize: 10, fontWeight: FontWeight.w700)),
                      ),
                    ],
                  ),
                  const SizedBox(height: 2),
                  Text(reason, style: TextStyle(color: AppColors.text3, fontSize: 11)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
