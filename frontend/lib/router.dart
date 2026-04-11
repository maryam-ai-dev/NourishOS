import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import 'screens/home_screen.dart';
import 'screens/planner_screen.dart';
import 'screens/pantry_screen.dart';
import 'screens/cooking_screen.dart';
import 'screens/reorders_screen.dart';
import 'screens/household_screen.dart';
import 'screens/insights_screen.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();
final _shellNavigatorKey = GlobalKey<NavigatorState>();

final router = GoRouter(
  navigatorKey: _rootNavigatorKey,
  initialLocation: '/',
  routes: [
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

  static const _tabs = [
    ('/', Icons.home_outlined, 'Home'),
    ('/planner', Icons.calendar_month_outlined, 'Planner'),
    ('/pantry', Icons.kitchen_outlined, 'Pantry'),
    ('/cooking', Icons.soup_kitchen_outlined, 'Cooking'),
    ('/reorders', Icons.shopping_cart_outlined, 'Reorders'),
    ('/household', Icons.people_outlined, 'Household'),
    ('/insights', Icons.insights_outlined, 'Insights'),
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
