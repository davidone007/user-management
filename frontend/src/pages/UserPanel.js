/**
 * User panel component for standard user account management.
 * 
 * This component provides functionality for regular users:
 * 
 *   View last login timestamp
 *   Change password
 *   Logout
 * 
 * 
 * Password reset enforcement:
 * 
 *   If requirePasswordChange is true, the last login display is hidden
 *   User must change password before accessing other features
 *   After successful password change, onPasswordChanged callback is called
 * 
 * 
 * All API requests include the JWT token in the Authorization header.
 * 
 * @param {string} token - JWT access token for authenticated requests
 * @param {Function} onLogout - Callback function called when user logs out
 * @param {boolean} requirePasswordChange - Whether the user must change their password
 * @param {Function} onPasswordChanged - Callback function called after successful password change
 * @module UserPanel
 * @component
 */
import React, { useEffect, useState } from "react";
import Input from "../components/Input";
import Button from "../components/Button";
import Notification from "../components/Notification";
import { getErrorMessage } from "../utils/fetchHelpers";

export default function UserPanel({ token, onLogout, requirePasswordChange = false, onPasswordChanged }) {
  const [lastLogin, setLastLogin] = useState(null);
  const [oldPass, setOldPass] = useState("");
  const [newPass, setNewPass] = useState("");
  const [msg, setMsg] = useState(null);

  useEffect(() => {
    fetch("/api/auth/me/last-login", {
      headers: { Authorization: "Bearer " + token },
    })
      .then((r) => r.json())
      .then((d) => setLastLogin(d))
      .catch(() => {});
  }, []);

  async function change() {
    const res = await fetch("/api/auth/me/change-password", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: "Bearer " + token,
      },
      body: JSON.stringify({ oldPassword: oldPass, newPassword: newPass }),
    });
    if (res.ok) setMsg("Contraseña cambiada");
    else {
      const err = await getErrorMessage(res);
      setMsg(err || "Error al cambiar contraseña");
    }
    if (res.ok && onPasswordChanged) {
      // Notify parent that password change completed so it can clear the forced flag
      onPasswordChanged();
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Panel de usuario</h2>
        <Button
          className="bg-red-600 text-white"
          onClick={async () => {
            await fetch("/api/auth/logout", {
              method: "POST",
              credentials: "include",
            });
            onLogout();
          }}
        >
          Cerrar sesión
        </Button>
      </div>

      {!requirePasswordChange && (
        <div className="mt-3 mb-6">
          <div className="text-sm text-slate-600">Último acceso:</div>
          <div className="font-medium">
            {lastLogin ? new Date(lastLogin).toLocaleString() : "Nunca"}
          </div>
        </div>
      )}

      <div>
        <h3 className="font-semibold mb-2">Cambiar contraseña</h3>
        {msg && <Notification>{msg}</Notification>}
        <div className="flex flex-col gap-3 max-w-md">
          <Input
            label="Contraseña actual"
            type="password"
            value={oldPass}
            onChange={(e) => setOldPass(e.target.value)}
          />
          <Input
            label="Nueva contraseña"
            type="password"
            value={newPass}
            onChange={(e) => setNewPass(e.target.value)}
          />
          <Button className="bg-yellow-600" onClick={change}>
            Cambiar
          </Button>
        </div>
      </div>
    </div>
  );
}
