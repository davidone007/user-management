/**
 * Login page component for user authentication.
 * 
 * This component provides a form for users to log in to the system.
 * Upon successful login, it extracts the JWT token, user role, and forcePasswordReset
 * flag from the response and calls the onLogin callback.
 * 
 * Features:
 * 
 *   Username and password input fields
 *   Form validation (required fields)
 *   Loading state during authentication
 *   Error message display
 *   JWT token parsing to extract role
 * 
 * 
 * The JWT token is decoded client-side to extract the role claim, which is
 * then passed to the parent component for routing decisions.
 * 
 * @param {Function} onLogin - Callback function called on successful login.
 *                            Receives (token, role, forcePasswordReset) as parameters.
 * @module Login
 * @component
 */
import React, { useState } from "react";
import Input from "../components/Input";
import Button from "../components/Button";
import Notification from "../components/Notification";
import { getErrorMessage } from "../utils/fetchHelpers";

export default function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      setLoading(false);
      if (!res.ok) {
        const msg = await getErrorMessage(res);
        throw new Error(msg || "Error al iniciar sesión");
      }
  const data = await res.json();
  const token = data.token;
  const payload = JSON.parse(atob(token.split(".")[1]));
  const forceReset = !!data.forcePasswordReset;
  onLogin(token, payload.role, forceReset);
    } catch (err) {
      setLoading(false);
      setError(err.message || "Error al iniciar sesión");
    }
  }

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Acceder</h2>
      {error && <Notification type="error">{error}</Notification>}
      <form onSubmit={submit} className="flex flex-col gap-3">
        <Input
          label="Usuario"
          placeholder="tu usuario"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <Input
          label="Contraseña"
          placeholder="tu contraseña"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button
          className="bg-blue-600 text-white"
          type="submit"
          disabled={loading}
        >
          {loading ? "Entrando…" : "Ingresar"}
        </Button>
      </form>
    </div>
  );
}
