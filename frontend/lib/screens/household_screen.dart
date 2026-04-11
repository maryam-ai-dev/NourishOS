import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/providers.dart';
import '../models/household.dart';
import '../services/authority_service.dart';
import '../services/service_exception.dart';
import '../config/app_theme.dart';

class HouseholdScreen extends ConsumerStatefulWidget {
  const HouseholdScreen({super.key});

  @override
  ConsumerState<HouseholdScreen> createState() => _HouseholdScreenState();
}

class _HouseholdScreenState extends ConsumerState<HouseholdScreen> {
  String? _householdId;

  @override
  Widget build(BuildContext context) {
    final String id = _householdId ?? ref.watch(householdIdProvider) ?? '';

    return Scaffold(
      appBar: AppBar(
        title: const Text('Household'),
      ),
      body: id.isEmpty
          ? const Center(child: Text('No household selected', style: TextStyle(color: AppColors.text3)))
          : _MembersList(householdId: id),
    );
  }
}

class _MembersList extends ConsumerWidget {
  final String householdId;
  const _MembersList({required this.householdId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final members = ref.watch(membersProvider(householdId));

    return members.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (err, _) => Center(
        child: Text('Error: $err', style: const TextStyle(color: AppColors.red1)),
      ),
      data: (memberList) => ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: memberList.length,
        itemBuilder: (context, index) => _MemberCard(
          member: memberList[index],
          householdId: householdId,
        ),
      ),
    );
  }
}

class _MemberCard extends StatelessWidget {
  final HouseholdMember member;
  final String householdId;

  const _MemberCard({required this.member, required this.householdId});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.surface2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                member.displayName,
                style: const TextStyle(color: AppColors.text1, fontSize: 16, fontWeight: FontWeight.w600),
              ),
              const SizedBox(width: 8),
              _Badge(label: member.ageGroup, color: member.ageGroup == 'CHILD' ? AppColors.green1 : AppColors.green2),
            ],
          ),
          const SizedBox(height: 8),
          if (member.effortSensitivity != null)
            Text('Effort: ${member.effortSensitivity}', style: const TextStyle(color: AppColors.text3, fontSize: 13)),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
              onPressed: () => _showPreferenceEditor(context),
              child: const Text('Edit Preferences', style: TextStyle(color: AppColors.green1)),
            ),
          ),
        ],
      ),
    );
  }

  void _showPreferenceEditor(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface2,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (ctx) => _PreferenceForm(householdId: householdId, memberId: member.id, memberName: member.displayName),
    );
  }
}

class _Badge extends StatelessWidget {
  final String label;
  final Color color;
  const _Badge({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600)),
    );
  }
}

class _PreferenceForm extends StatefulWidget {
  final String householdId;
  final String memberId;
  final String memberName;

  const _PreferenceForm({required this.householdId, required this.memberId, required this.memberName});

  @override
  State<_PreferenceForm> createState() => _PreferenceFormState();
}

class _PreferenceFormState extends State<_PreferenceForm> {
  final _proteinController = TextEditingController();
  String? _proteinError;
  bool _saving = false;

  @override
  void dispose() {
    _proteinController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 16, right: 16, top: 24,
        bottom: MediaQuery.of(context).viewInsets.bottom + 16,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Preferences — ${widget.memberName}',
            style: const TextStyle(color: AppColors.text1, fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _proteinController,
            keyboardType: TextInputType.number,
            style: const TextStyle(color: AppColors.text1),
            decoration: InputDecoration(
              labelText: 'Protein goal (g)',
              labelStyle: const TextStyle(color: AppColors.text3),
              errorText: _proteinError,
              enabledBorder: const UnderlineInputBorder(borderSide: BorderSide(color: AppColors.text4)),
              focusedBorder: const UnderlineInputBorder(borderSide: BorderSide(color: AppColors.green1)),
            ),
          ),
          const SizedBox(height: 24),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _saving ? null : _save,
              child: _saving
                  ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                  : const Text('Save'),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _save() async {
    final proteinText = _proteinController.text.trim();
    if (proteinText.isNotEmpty) {
      final val = double.tryParse(proteinText);
      if (val == null || val < 0) {
        setState(() => _proteinError = 'Must be a positive number');
        return;
      }
    }
    setState(() {
      _proteinError = null;
      _saving = true;
    });

    try {
      final prefs = <String, dynamic>{};
      if (proteinText.isNotEmpty) {
        prefs['proteinTarget'] = double.parse(proteinText);
      }
      await AuthorityService().updatePreferences(widget.householdId, widget.memberId, prefs);
      if (mounted) {
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Preferences saved')),
        );
      }
    } on ServiceException catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: ${e.message}')),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}
