import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'screens/home_screen.dart';
import 'screens/planner_screen.dart';
import 'screens/pantry_screen.dart';
import 'screens/cooking_screen.dart';
import 'screens/reorders_screen.dart';
import 'screens/household_screen.dart';
import 'screens/insights_screen.dart';
import 'screens/member_onboarding_screen.dart';
import 'screens/receipt_confirm_screen.dart';
import 'screens/shopping_list_screen.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();
final _shellNavigatorKey = GlobalKey<NavigatorState>();

final router = GoRouter(
  navigatorKey: _rootNavigatorKey,
  initialLocation: '/',
  routes: [
    // Onboarding flow — outside shell (no bottom nav)
    GoRoute(
      path: '/household/onboarding',
      name: 'onboarding',
      builder: (context, state) => const MemberOnboardingScreen(),
    ),
    // Receipt confirm flow — outside shell
    GoRoute(
      path: '/pantry/receipt-confirm',
      name: 'receipt-confirm',
      builder: (context, state) => const ReceiptConfirmScreen(),
    ),
    // Reorders subroutes — outside shell (full screen)
    GoRoute(
      path: '/reorders/shopping-list',
      name: 'shopping-list',
      builder: (context, state) => const ShoppingListScreen(),
    ),
    GoRoute(
      path: '/reorders/supermarkets',
      name: 'supermarkets',
      builder: (context, state) => const SupermarketScreen(),
    ),
    GoRoute(
      path: '/reorders/basket',
      name: 'basket',
      builder: (context, state) => const BasketPreviewScreen(),
    ),
    ShellRoute(
      navigatorKey: _shellNavigatorKey,
      builder: (context, state, child) => ScaffoldWithNavBar(child: child),
      routes: [
        GoRoute(
          path: '/',
          name: 'home',
          builder: (context, state) => const HomeScreen(),
        ),
        GoRoute(
          path: '/planner',
          name: 'planner',
          builder: (context, state) => const WeeklyPlannerScreen(),
        ),
        GoRoute(
          path: '/pantry',
          name: 'pantry',
          builder: (context, state) => const PantryScreen(),
        ),
        GoRoute(
          path: '/cooking',
          name: 'cooking',
          builder: (context, state) => const CookingScreen(),
        ),
        GoRoute(
          path: '/reorders',
          name: 'reorders',
          builder: (context, state) => const ReordersScreen(),
        ),
        GoRoute(
          path: '/household',
          name: 'household',
          builder: (context, state) => const HouseholdScreen(),
        ),
        GoRoute(
          path: '/insights',
          name: 'insights',
          builder: (context, state) => const InsightsScreen(),
        ),
      ],
    ),
  ],
);

class ScaffoldWithNavBar extends StatelessWidget {
  final Widget child;
  const ScaffoldWithNavBar({super.key, required this.child});

  // 5 nav items per Sprint 23B.4 — Insights accessed via chip on home screen
  static const _tabs = [
    ('/', Icons.home_outlined, 'Home'),
    ('/planner', Icons.calendar_month_outlined, 'Planner'),
    ('/pantry', Icons.kitchen_outlined, 'Pantry'),
    ('/reorders', Icons.shopping_cart_outlined, 'Reorders'),
    ('/household', Icons.people_outlined, 'Household'),
  ];

  int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    for (var i = 0; i < _tabs.length; i++) {
      if (_tabs[i].$1 == location) return i;
    }
    return 0;
  }

  @override
  Widget build(BuildContext context) {
    final index = _currentIndex(context);
    return Scaffold(
      body: child,
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: index,
        onTap: (i) => GoRouter.of(context).go(_tabs[i].$1),
        type: BottomNavigationBarType.fixed,
        backgroundColor: Colors.white,
        selectedItemColor: const Color(0xFF2D6A4F),
        unselectedItemColor: const Color(0xFFA8BAAA),
        selectedFontSize: 11,
        unselectedFontSize: 10,
        showUnselectedLabels: false,
        items: _tabs
            .map((t) => BottomNavigationBarItem(icon: Icon(t.$2), label: t.$3))
            .toList(),
      ),
    );
  }
}
