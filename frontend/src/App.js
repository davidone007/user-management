import React, { useState } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import UserPanel from "./pages/UserPanel";
import AdminPanel from "./pages/AdminPanel";
import Layout from "./components/Layout";

function App() {
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [forcePasswordReset, setForcePasswordReset] = useState(false);

  return (
    <Layout>
      {!token ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="card">
            <Login
              onLogin={(t, r, f) => {
                setToken(t);
                setRole(r);
                setForcePasswordReset(!!f);
              }}
            />
          </div>
          <div className="card">
            <Register />
          </div>
        </div>
      ) : role === "ADMIN" ? (
        <AdminPanel token={token} onLogout={() => setToken(null)} />
      ) : (
        <UserPanel
          token={token}
          onLogout={() => setToken(null)}
          requirePasswordChange={forcePasswordReset}
          onPasswordChanged={() => setForcePasswordReset(false)}
        />
      )}
    </Layout>
  );
}

export default App;
