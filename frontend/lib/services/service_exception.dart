class ServiceException implements Exception {
  final int? statusCode;
  final String message;

  ServiceException(this.message, {this.statusCode});

  @override
  String toString() => 'ServiceException($statusCode): $message';
}
