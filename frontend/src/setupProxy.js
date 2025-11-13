const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // Ajusta los paths según tu API real; '/api' es típico.
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'https://localhost:8080',
      changeOrigin: true,
      secure: false, // acepta certificados autofirmados en desarrollo
      logLevel: 'debug',
    })
  );

  // Proxy para H2 Console (opcional)
  app.use(
    '/h2-console',
    createProxyMiddleware({
      target: 'https://localhost:8080',
      changeOrigin: true,
      secure: false,
      logLevel: 'debug',
      pathRewrite: { '^/h2-console': '/h2-console' },
    })
  );

  // Si quieres proxyear todo lo demás (no recomendado), descomenta:
  // app.use(createProxyMiddleware({ target: 'https://localhost:8080', changeOrigin: true, secure: false }));
};
