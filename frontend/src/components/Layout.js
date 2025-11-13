import React from "react";

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-white text-slate-900">
      <header className="bg-white/60 backdrop-blur sticky top-0 z-10 border-b">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold">
              GU
            </div>
            <h1 className="text-lg font-semibold">Gestión de usuarios</h1>
          </div>
          <nav className="text-sm text-slate-700">Ciberseguridad</nav>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">{children}</main>

      <footer className="max-w-4xl mx-auto px-4 py-6 text-sm text-slate-500">
        <div>Proyecto demo — Backend/Frontend ejemplo</div>
      </footer>
    </div>
  );
}
