function fn() {
  var config = {
    customerBaseUrl: karate.properties['customer.base.url'] || 'http://localhost:8081',
    accountBaseUrl: karate.properties['account.base.url'] || 'http://localhost:8082'
  };
  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 10000);
  return config;
}

