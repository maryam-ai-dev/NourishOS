import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:nourishos_frontend/main.dart';

void main() {
  testWidgets('App renders NourishOS title', (WidgetTester tester) async {
    await tester.pumpWidget(const ProviderScope(child: NourishOSApp()));
    await tester.pump();
    expect(find.text('NourishOS'), findsOneWidget);
  });
}
