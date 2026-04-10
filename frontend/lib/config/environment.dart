class Environment {
  static const authorityBaseUrl = String.fromEnvironment(
    'AUTHORITY_URL',
    defaultValue: 'http://localhost:8080',
  );

  static const intelligenceBaseUrl = String.fromEnvironment(
    'INTELLIGENCE_URL',
    defaultValue: 'http://localhost:8000',
  );
}
