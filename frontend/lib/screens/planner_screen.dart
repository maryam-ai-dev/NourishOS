import 'package:flutter/material.dart';

class WeeklyPlannerScreen extends StatelessWidget {
  const WeeklyPlannerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFF0B0B0F),
      body: Center(
        child: Text(
          'Weekly Planner',
          style: TextStyle(
            color: Colors.white,
            fontSize: 24,
          ),
        ),
      ),
    );
  }
}
