import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:google_fonts/google_fonts.dart';
import '../config/app_theme.dart';
import '../providers/providers.dart';
import '../widgets/app_card.dart';

class HouseholdScreen extends ConsumerWidget {
  const HouseholdScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final id = ref.watch(householdIdProvider);
    final hasHousehold = id.isNotEmpty;

    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(title: const Text('Household')),
      body: hasHousehold ? _MemberList(householdId: id) : const _EmptyState(),
    );
  }
}

// --- Sprint 23B.9: Empty state ---
class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 120, height: 120,
            decoration: const BoxDecoration(color: AppColors.green5, shape: BoxShape.circle),
            child: const Center(child: Text('👨‍👩‍👧', style: TextStyle(fontSize: 56))),
          ),
          const SizedBox(height: 24),
          Text('Set up your household', style: GoogleFonts.dmSerifDisplay(fontSize: 24, color: AppColors.text1), textAlign: TextAlign.center),
          const SizedBox(height: 12),
          Text(
            "We'll learn what each person loves to eat, what they can't, and plan meals everyone enjoys.",
            textAlign: TextAlign.center,
            style: TextStyle(color: AppColors.text3, fontSize: 13, height: 1.5),
          ),
          const SizedBox(height: 32),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: () => context.push('/household/onboarding'),
              style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
              child: const Text('Add your household', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
            ),
          ),
          const SizedBox(height: 12),
          TextButton(
            onPressed: () => context.go('/'),
            child: Text("I'll do this later", style: TextStyle(color: AppColors.text3, fontSize: 13)),
          ),
        ],
      ),
    );
  }
}

// --- Sprint 23B.10: Member list ---
class _MemberList extends ConsumerWidget {
  final String householdId;
  const _MemberList({required this.householdId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Demo members — in production from GET /households/{id}/members
    final members = [
      {'name': 'Maryam', 'role': 'Adult', 'tags': ['Vegetarian', 'High protein'], 'emoji': '👩'},
      {'name': 'Ahmed', 'role': 'Adult', 'tags': ['No seafood', 'Loves spicy'], 'emoji': '👨'},
      {'name': 'Zara', 'role': 'Child', 'tags': ['Nut allergy', 'Pasta fan'], 'emoji': '👧'},
    ];

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Household header
        Row(
          children: [
            Container(
              width: 48, height: 48,
              decoration: const BoxDecoration(color: AppColors.green4, shape: BoxShape.circle),
              child: const Center(child: Text('🏡', style: TextStyle(fontSize: 22))),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('The Yousuf Home', style: GoogleFonts.dmSerifDisplay(fontSize: 20, color: AppColors.text1)),
                  Text('${members.length} members', style: TextStyle(color: AppColors.text3, fontSize: 12)),
                ],
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),

        // Member cards
        ...members.map((m) => _MemberCard(
          name: m['name'] as String,
          role: m['role'] as String,
          tags: m['tags'] as List<String>,
          emoji: m['emoji'] as String,
        )),

        const SizedBox(height: 12),
        // Add another member button
        GestureDetector(
          onTap: () => context.push('/household/onboarding'),
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(AppRadius.md),
              border: Border.all(color: AppColors.green3, width: 1.5),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.add, color: AppColors.green1, size: 20),
                const SizedBox(width: 8),
                Text('Add another member', style: TextStyle(color: AppColors.green1, fontSize: 14, fontWeight: FontWeight.w600)),
              ],
            ),
          ),
        ),

        const SizedBox(height: 24),
        // Smart fridge coming soon (Sprint 23B.21 — appears here too)
        _SmartFridgePlaceholder(),
      ],
    );
  }
}

class _MemberCard extends StatelessWidget {
  final String name;
  final String role;
  final List<String> tags;
  final String emoji;

  const _MemberCard({required this.name, required this.role, required this.tags, required this.emoji});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      child: AppCard(
        child: Row(
          children: [
            Container(
              width: 48, height: 48,
              decoration: const BoxDecoration(color: AppColors.green5, shape: BoxShape.circle),
              child: Center(child: Text(emoji, style: const TextStyle(fontSize: 22))),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(children: [
                    Text(name, style: TextStyle(color: AppColors.text1, fontSize: 16, fontWeight: FontWeight.w600)),
                    const SizedBox(width: 8),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(color: AppColors.surface2, borderRadius: BorderRadius.circular(AppRadius.full)),
                      child: Text(role, style: TextStyle(color: AppColors.text3, fontSize: 10, fontWeight: FontWeight.w500)),
                    ),
                  ]),
                  const SizedBox(height: 6),
                  Wrap(
                    spacing: 4, runSpacing: 4,
                    children: tags.take(3).map((t) => Container(
                      padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                      decoration: BoxDecoration(color: AppColors.green5, borderRadius: BorderRadius.circular(AppRadius.full)),
                      child: Text(t, style: TextStyle(color: AppColors.green1, fontSize: 10, fontWeight: FontWeight.w500)),
                    )).toList(),
                  ),
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: AppColors.text4, size: 20),
          ],
        ),
      ),
    );
  }
}

// --- Sprint 23B.21: Smart fridge placeholder (used on pantry too) ---
class _SmartFridgePlaceholder extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface2,
        borderRadius: BorderRadius.circular(AppRadius.md),
      ),
      child: Row(
        children: [
          const Text('🧊', style: TextStyle(fontSize: 24)),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  Text('Pair a smart fridge', style: TextStyle(color: AppColors.text2, fontSize: 13, fontWeight: FontWeight.w600)),
                  const SizedBox(width: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(color: AppColors.surface, borderRadius: BorderRadius.circular(AppRadius.full)),
                    child: Text('Coming soon', style: TextStyle(color: AppColors.text3, fontSize: 9, fontWeight: FontWeight.w500)),
                  ),
                ]),
                const SizedBox(height: 2),
                Text('Auto-track your inventory without scanning', style: TextStyle(color: AppColors.text3, fontSize: 11)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
