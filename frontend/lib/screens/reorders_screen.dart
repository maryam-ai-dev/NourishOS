import 'package:flutter/material.dart';

class ReordersScreen extends StatelessWidget {
  const ReordersScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFF0B0B0F),
      body: Center(
        child: Text(
          'Reorders',
          style: TextStyle(
            color: Colors.white,
            fontSize: 24,
          ),
        ),
      ),
    );
  }
}
