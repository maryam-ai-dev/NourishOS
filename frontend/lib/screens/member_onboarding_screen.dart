import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:go_router/go_router.dart';
import '../config/app_theme.dart';

/// Shared state for the 7-step onboarding flow.
class MemberFlowState {
  String name = '';
  String ageGroup = ''; // ADULT or CHILD
  Set<String> dietaryPrefs = {};
  Set<String> allergies = {};
  List<String> favourites = [];
  List<String> dislikes = [];
  int? caloriesGoal;
  int? proteinGoal;
}

final memberFlowStateProvider = StateProvider<MemberFlowState>((ref) => MemberFlowState());

class MemberOnboardingScreen extends ConsumerStatefulWidget {
  const MemberOnboardingScreen({super.key});

  @override
  ConsumerState<MemberOnboardingScreen> createState() => _MemberOnboardingScreenState();
}

class _MemberOnboardingScreenState extends ConsumerState<MemberOnboardingScreen> {
  int _step = 0;
  final int _totalSteps = 7;

  void _next() {
    if (_step < _totalSteps - 1) {
      setState(() => _step++);
    } else {
      _save();
    }
  }

  void _back() {
    if (_step > 0) setState(() => _step--);
  }

  Future<void> _save() async {
    // Sprint 23B.18 — save member
    // In production: POST /households/{id}/members then PATCH /.../preferences
    final state = ref.read(memberFlowStateProvider);
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('${state.name} added to your household 🎉')),
      );
      context.go('/household');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: _step == 0 ? () => context.go('/household') : _back,
        ),
        title: _ProgressDots(current: _step, total: _totalSteps),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: _buildStep(),
        ),
      ),
    );
  }

  Widget _buildStep() {
    switch (_step) {
      case 0: return _NameStep(onContinue: _next);
      case 1: return _AgeGroupStep(onContinue: _next);
      case 2: return _DietaryStep(onContinue: _next, onSkip: _next);
      case 3: return _AllergyStep(onContinue: _next, onSkip: _next);
      case 4: return _FavouritesStep(onContinue: _next, onSkip: _next);
      case 5: return _DislikesStep(onContinue: _next, onSkip: _next);
      case 6: return _GoalsStep(onContinue: _next, onSkip: _next);
      default: return const SizedBox.shrink();
    }
  }
}

// --- Progress Dots ---
class _ProgressDots extends StatelessWidget {
  final int current;
  final int total;
  const _ProgressDots({required this.current, required this.total});

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(total, (i) => Container(
        margin: const EdgeInsets.symmetric(horizontal: 3),
        width: 6, height: 6,
        decoration: BoxDecoration(
          color: i <= current ? AppColors.green1 : AppColors.surface2,
          shape: BoxShape.circle,
        ),
      )),
    );
  }
}

// --- Sprint 23B.11: Name step ---
class _NameStep extends ConsumerStatefulWidget {
  final VoidCallback onContinue;
  const _NameStep({required this.onContinue});
  @override
  ConsumerState<_NameStep> createState() => _NameStepState();
}

class _NameStepState extends ConsumerState<_NameStep> {
  final _ctrl = TextEditingController();
  @override
  void dispose() { _ctrl.dispose(); super.dispose(); }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text("What's their name?", style: GoogleFonts.dmSerifDisplay(fontSize: 26, color: AppColors.text1)),
        const SizedBox(height: 16),
        TextField(
          controller: _ctrl,
          autofocus: true,
          onChanged: (_) => setState(() {}),
          style: TextStyle(color: AppColors.text1, fontSize: 16),
          decoration: InputDecoration(
            hintText: 'Name',
            hintStyle: TextStyle(color: AppColors.text4),
            enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
            focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
          ),
        ),
        const Spacer(),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: _ctrl.text.trim().isEmpty ? null : () {
              ref.read(memberFlowStateProvider).name = _ctrl.text.trim();
              widget.onContinue();
            },
            style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
            child: const Text('Continue →', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
          ),
        ),
      ],
    );
  }
}

// --- Sprint 23B.12: Age group step ---
class _AgeGroupStep extends ConsumerStatefulWidget {
  final VoidCallback onContinue;
  const _AgeGroupStep({required this.onContinue});
  @override
  ConsumerState<_AgeGroupStep> createState() => _AgeGroupStepState();
}

class _AgeGroupStepState extends ConsumerState<_AgeGroupStep> {
  String _selected = '';

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Are they an adult or a child?', style: GoogleFonts.dmSerifDisplay(fontSize: 26, color: AppColors.text1)),
        const SizedBox(height: 24),
        _ageCard('ADULT', '👨', 'Adult', '18 and over'),
        const SizedBox(height: 12),
        _ageCard('CHILD', '👶', 'Child', 'Under 18'),
        const Spacer(),
        SizedBox(
          width: double.infinity,
          child: ElevatedButton(
            onPressed: _selected.isEmpty ? null : () {
              ref.read(memberFlowStateProvider).ageGroup = _selected;
              widget.onContinue();
            },
            style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 14)),
            child: const Text('Continue →', style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600)),
          ),
        ),
      ],
    );
  }

  Widget _ageCard(String value, String emoji, String title, String subtitle) {
    final isSelected = _selected == value;
    return GestureDetector(
      onTap: () => setState(() => _selected = value),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(AppRadius.md),
          border: Border.all(color: isSelected ? AppColors.green1 : AppColors.surface2, width: 2),
        ),
        child: Row(
          children: [
            Text(emoji, style: const TextStyle(fontSize: 32)),
            const SizedBox(width: 16),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: TextStyle(color: AppColors.text1, fontSize: 17, fontWeight: FontWeight.w600)),
                Text(subtitle, style: TextStyle(color: AppColors.text3, fontSize: 12)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// --- Sprint 23B.13: Dietary step ---
class _DietaryStep extends ConsumerStatefulWidget {
  final VoidCallback onContinue;
  final VoidCallback onSkip;
  const _DietaryStep({required this.onContinue, required this.onSkip});
  @override
  ConsumerState<_DietaryStep> createState() => _DietaryStepState();
}

class _DietaryStepState extends ConsumerState<_DietaryStep> {
  final _tags = ['Vegetarian', 'Vegan', 'Gluten free', 'Dairy free', 'Pescatarian', 'High protein', 'Low sodium', 'Low sugar', 'None of these'];
  Set<String> _selected = {};

  void _toggle(String tag) {
    setState(() {
      if (tag == 'None of these') {
        _selected = _selected.contains(tag) ? {} : {tag};
      } else {
        _selected.remove('None of these');
        if (_selected.contains(tag)) {
          _selected.remove(tag);
        } else {
          _selected.add(tag);
        }
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Any dietary preferences?', style: GoogleFonts.dmSerifDisplay(fontSize: 26, color: AppColors.text1)),
        const SizedBox(height: 24),
        Wrap(
          spacing: 8, runSpacing: 8,
          children: _tags.map((t) => _chip(t, _selected.contains(t), () => _toggle(t))).toList(),
        ),
        const Spacer(),
        Row(children: [
          TextButton(onPressed: widget.onSkip, child: Text('Skip', style: TextStyle(color: AppColors.text3))),
          const Spacer(),
          ElevatedButton(
            onPressed: () {
              ref.read(memberFlowStateProvider).dietaryPrefs = _selected;
              widget.onContinue();
            },
            child: const Text('Continue →'),
          ),
        ]),
      ],
    );
  }
}

Widget _chip(String label, bool isSelected, VoidCallback onTap, {Color? selectedBg}) {
  return GestureDetector(
    onTap: onTap,
    child: Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: isSelected ? (selectedBg ?? AppColors.green4) : AppColors.surface,
        borderRadius: BorderRadius.circular(AppRadius.full),
        border: Border.all(color: isSelected ? AppColors.green1 : AppColors.surface2),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (isSelected) ...[
            Icon(Icons.check, size: 14, color: AppColors.green1),
            const SizedBox(width: 4),
          ],
          Text(label, style: TextStyle(
            color: isSelected ? AppColors.green1 : AppColors.text2,
            fontSize: 12, fontWeight: FontWeight.w500,
          )),
        ],
      ),
    ),
  );
}

// --- Sprint 23B.14: Allergy step ---
class _AllergyStep extends ConsumerStatefulWidget {
  final VoidCallback onContinue;
  final VoidCallback onSkip;
  const _AllergyStep({required this.onContinue, required this.onSkip});
  @override
  ConsumerState<_AllergyStep> createState() => _AllergyStepState();
}

class _AllergyStepState extends ConsumerState<_AllergyStep> {
  final _tags = ['Nuts', 'Dairy', 'Gluten', 'Eggs', 'Shellfish', 'Fish', 'Soy', 'Sesame', 'None'];
  Set<String> _selected = {};

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Any allergies we should know about?', style: GoogleFonts.dmSerifDisplay(fontSize: 26, color: AppColors.text1)),
        const SizedBox(height: 24),
        Wrap(
          spacing: 8, runSpacing: 8,
          children: _tags.map((t) {
            final isSel = _selected.contains(t);
            return GestureDetector(
              onTap: () => setState(() {
                if (t == 'None') {
                  _selected = isSel ? {} : {t};
                } else {
                  _selected.remove('None');
                  if (isSel) { _selected.remove(t); } else { _selected.add(t); }
                }
              }),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  color: isSel ? AppColors.red3 : AppColors.surface,
                  borderRadius: BorderRadius.circular(AppRadius.full),
                  border: Border.all(color: isSel ? AppColors.red1 : AppColors.surface2),
                ),
                child: Text(t, style: TextStyle(
                  color: isSel ? AppColors.red1 : AppColors.text2,
                  fontSize: 12, fontWeight: FontWeight.w500,
                )),
              ),
            );
          }).toList(),
        ),
        const Spacer(),
        Row(children: [
          TextButton(onPressed: widget.onSkip, child: Text('Skip', style: TextStyle(color: AppColors.text3))),
          const Spacer(),
          ElevatedButton(
            onPressed: () {
              ref.read(memberFlowStateProvider).allergies = _selected;
              widget.onContinue();
            },
            child: const Text('Continue →'),
          ),
        ]),
      ],
    );
  }
}

// --- Sprint 23B.15: Favourites step ---
class _FavouritesStep extends ConsumerWidget {
  final VoidCallback onContinue;
  final VoidCallback onSkip;
  const _FavouritesStep({required this.onContinue, required this.onSkip});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(memberFlowStateProvider);
    return _ChipsPickerStep(
      headline: 'What does ${state.name} love eating? 🍝',
      suggestions: const ['Pasta', 'Chicken', 'Pizza', 'Noodles', 'Mexican', 'Sushi', 'Stew', 'Curry', 'Sandwiches', 'Salads', 'Rice dishes', 'Burgers', 'Soup'],
      initialSelected: state.favourites,
      maxItems: 10,
      onContinue: (items) {
        ref.read(memberFlowStateProvider).favourites = items;
        onContinue();
      },
      onSkip: onSkip,
    );
  }
}

// --- Sprint 23B.16: Dislikes step ---
class _DislikesStep extends ConsumerWidget {
  final VoidCallback onContinue;
  final VoidCallback onSkip;
  const _DislikesStep({required this.onContinue, required this.onSkip});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(memberFlowStateProvider);
    return _ChipsPickerStep(
      headline: "Anything ${state.name} really doesn't like?",
      suggestions: const ['Mushrooms', 'Onions', 'Spicy food', 'Seafood', 'Liver', 'Sprouts', 'Aubergine', 'Blue cheese'],
      initialSelected: state.dislikes,
      maxItems: 20,
      onContinue: (items) {
        ref.read(memberFlowStateProvider).dislikes = items;
        onContinue();
      },
      onSkip: onSkip,
    );
  }
}

class _ChipsPickerStep extends StatefulWidget {
  final String headline;
  final List<String> suggestions;
  final List<String> initialSelected;
  final int maxItems;
  final Function(List<String>) onContinue;
  final VoidCallback onSkip;

  const _ChipsPickerStep({
    required this.headline, required this.suggestions, required this.initialSelected,
    required this.maxItems, required this.onContinue, required this.onSkip,
  });

  @override
  State<_ChipsPickerStep> createState() => _ChipsPickerStepState();
}

class _ChipsPickerStepState extends State<_ChipsPickerStep> {
  late List<String> _selected;
  final _searchCtrl = TextEditingController();

  @override
  void initState() {
    super.initState();
    _selected = [...widget.initialSelected];
  }

  @override
  void dispose() { _searchCtrl.dispose(); super.dispose(); }

  void _toggle(String item) {
    setState(() {
      if (_selected.contains(item)) {
        _selected.remove(item);
      } else {
        if (_selected.length >= widget.maxItems) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("That's plenty!")));
          return;
        }
        _selected.add(item);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final allChips = [..._selected, ...widget.suggestions.where((s) => !_selected.contains(s))];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(widget.headline, style: GoogleFonts.dmSerifDisplay(fontSize: 22, color: AppColors.text1)),
        const SizedBox(height: 16),
        TextField(
          controller: _searchCtrl,
          onSubmitted: (v) {
            if (v.trim().isNotEmpty) _toggle(v.trim());
            _searchCtrl.clear();
          },
          style: TextStyle(color: AppColors.text1, fontSize: 14),
          decoration: InputDecoration(
            hintText: 'Add your own...',
            hintStyle: TextStyle(color: AppColors.text4),
            prefixIcon: Icon(Icons.search, color: AppColors.text3, size: 18),
            enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
            focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
          ),
        ),
        const SizedBox(height: 16),
        Expanded(
          child: SingleChildScrollView(
            child: Wrap(
              spacing: 8, runSpacing: 8,
              children: allChips.map((c) => _chip(c, _selected.contains(c), () => _toggle(c))).toList(),
            ),
          ),
        ),
        Row(children: [
          TextButton(onPressed: widget.onSkip, child: Text('Skip', style: TextStyle(color: AppColors.text3))),
          const Spacer(),
          ElevatedButton(
            onPressed: () => widget.onContinue(_selected),
            child: const Text('Continue →'),
          ),
        ]),
      ],
    );
  }
}

// --- Sprint 23B.17: Goals step ---
class _GoalsStep extends ConsumerStatefulWidget {
  final VoidCallback onContinue;
  final VoidCallback onSkip;
  const _GoalsStep({required this.onContinue, required this.onSkip});
  @override
  ConsumerState<_GoalsStep> createState() => _GoalsStepState();
}

class _GoalsStepState extends ConsumerState<_GoalsStep> {
  final _calCtrl = TextEditingController();
  final _proteinCtrl = TextEditingController();
  String? _err;

  @override
  void dispose() { _calCtrl.dispose(); _proteinCtrl.dispose(); super.dispose(); }

  void _save() {
    final cal = int.tryParse(_calCtrl.text.trim());
    final prot = int.tryParse(_proteinCtrl.text.trim());
    if (_calCtrl.text.isNotEmpty && (cal == null || cal < 0)) {
      setState(() => _err = 'Calories must be a positive number');
      return;
    }
    if (_proteinCtrl.text.isNotEmpty && (prot == null || prot < 0)) {
      setState(() => _err = 'Protein must be a positive number');
      return;
    }
    ref.read(memberFlowStateProvider)
      ..caloriesGoal = cal
      ..proteinGoal = prot;
    widget.onContinue();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Any weekly food goals?', style: GoogleFonts.dmSerifDisplay(fontSize: 26, color: AppColors.text1)),
        const SizedBox(height: 6),
        Text('These help us balance nutrition across the week', style: TextStyle(color: AppColors.text3, fontSize: 12)),
        const SizedBox(height: 24),
        _numField(_calCtrl, 'Daily calories (kcal)'),
        const SizedBox(height: 16),
        _numField(_proteinCtrl, 'Daily protein (g)'),
        if (_err != null) ...[
          const SizedBox(height: 8),
          Text(_err!, style: TextStyle(color: AppColors.red1, fontSize: 11)),
        ],
        const Spacer(),
        Row(children: [
          TextButton(onPressed: widget.onSkip, child: Text('Skip', style: TextStyle(color: AppColors.text3))),
          const Spacer(),
          ElevatedButton(onPressed: _save, child: const Text('Finish →')),
        ]),
      ],
    );
  }

  Widget _numField(TextEditingController ctrl, String label) {
    return TextField(
      controller: ctrl,
      keyboardType: TextInputType.number,
      style: TextStyle(color: AppColors.text1),
      decoration: InputDecoration(
        labelText: label,
        labelStyle: TextStyle(color: AppColors.text3),
        enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
        focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
      ),
    );
  }
}
