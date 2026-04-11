import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

// --- Colour tokens ---
class AppColors {
  static const bg = Color(0xFFF5F7F2);
  static const surface = Color(0xFFFFFFFF);
  static const surface2 = Color(0xFFEEF2EB);

  static const green1 = Color(0xFF2D6A4F);
  static const green2 = Color(0xFF52B788);
  static const green3 = Color(0xFF95D5B2);
  static const green4 = Color(0xFFD8F3DC);
  static const green5 = Color(0xFFF0FAF3);

  static const amber1 = Color(0xFFD4811A);
  static const amber2 = Color(0xFFF4A261);
  static const amber3 = Color(0xFFFDEBD0);

  static const red1 = Color(0xFFC1121F);
  static const red2 = Color(0xFFE07C7C);
  static const red3 = Color(0xFFFDEAEA);

  static const text1 = Color(0xFF1A2E1C);
  static const text2 = Color(0xFF4A5E4C);
  static const text3 = Color(0xFF7A8F7C);
  static const text4 = Color(0xFFA8BAAA);
}

// --- Border radius tokens ---
class AppRadius {
  static const double xs = 8;
  static const double sm = 12;
  static const double md = 16;
  static const double lg = 20;
  static const double xl = 28;
  static const double full = 999;
}

// --- Shadow tokens ---
class AppShadows {
  static List<BoxShadow> get xs => [
    BoxShadow(color: Colors.black.withValues(alpha: 0.04), blurRadius: 4, offset: const Offset(0, 1)),
  ];
  static List<BoxShadow> get sm => [
    BoxShadow(color: Colors.black.withValues(alpha: 0.06), blurRadius: 8, offset: const Offset(0, 2)),
  ];
}

// --- Theme ---
ThemeData appTheme() {
  final bodyFont = GoogleFonts.dmSansTextTheme();
  final displayFont = GoogleFonts.dmSerifDisplayTextTheme();

  return ThemeData(
    brightness: Brightness.light,
    scaffoldBackgroundColor: AppColors.bg,
    colorScheme: ColorScheme.fromSeed(
      seedColor: AppColors.green1,
      brightness: Brightness.light,
      surface: AppColors.surface,
    ),
    textTheme: bodyFont.copyWith(
      displayLarge: displayFont.displayLarge?.copyWith(color: AppColors.text1),
      displayMedium: displayFont.displayMedium?.copyWith(color: AppColors.text1),
      displaySmall: displayFont.displaySmall?.copyWith(color: AppColors.text1),
      headlineLarge: displayFont.headlineLarge?.copyWith(color: AppColors.text1),
      headlineMedium: displayFont.headlineMedium?.copyWith(color: AppColors.text1),
      headlineSmall: displayFont.headlineSmall?.copyWith(color: AppColors.text1),
      bodyLarge: bodyFont.bodyLarge?.copyWith(color: AppColors.text1),
      bodyMedium: bodyFont.bodyMedium?.copyWith(color: AppColors.text2),
      bodySmall: bodyFont.bodySmall?.copyWith(color: AppColors.text3),
      labelLarge: bodyFont.labelLarge?.copyWith(color: AppColors.text2),
      labelMedium: bodyFont.labelMedium?.copyWith(color: AppColors.text3),
      labelSmall: bodyFont.labelSmall?.copyWith(color: AppColors.text4),
    ),
    appBarTheme: AppBarTheme(
      backgroundColor: AppColors.surface,
      foregroundColor: AppColors.text1,
      elevation: 0,
      titleTextStyle: GoogleFonts.dmSerifDisplay(fontSize: 22, color: AppColors.text1),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: AppColors.surface,
      selectedItemColor: AppColors.green1,
      unselectedItemColor: AppColors.text4,
      selectedLabelStyle: TextStyle(fontSize: 11, fontWeight: FontWeight.w600),
      unselectedLabelStyle: TextStyle(fontSize: 10),
    ),
    elevatedButtonTheme: ElevatedButtonThemeData(
      style: ElevatedButton.styleFrom(
        backgroundColor: AppColors.green1,
        foregroundColor: Colors.white,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(AppRadius.sm)),
      ),
    ),
  );
}
