import React, { useEffect, useState } from "react";
import Button from "../components/Button";
import Notification from "../components/Notification";
import DeleteIcon from "../components/icons/DeleteIcon";
import ResetIcon from "../components/icons/ResetIcon";
import AuditIcon from "../components/icons/AuditIcon";
import ConfirmDialog from "../components/ConfirmDialog";
import { getErrorMessage } from "../utils/fetchHelpers";

export default function AdminPanel({ token, onLogout }) {
  const [users, setUsers] = useState([]);
  const [msg, setMsg] = useState(null);
  const [audit, setAudit] = useState([]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [deletingUser, setDeletingUser] = useState(null);

  async function load() {
    const res = await fetch("/api/admin/users", {
      headers: { Authorization: "Bearer " + token },
    });
    if (res.ok) {
      const data = await res.json();
      setUsers(data);
    }
  }
  useEffect(() => {
    load();
  }, []);
  useEffect(() => {
    const es = new EventSource("/api/admin/events");
    es.addEventListener("users-changed", () => load());
    return () => es.close();
  }, []);

  async function remove(id) {
    const res = await fetch("/api/admin/users/" + id, {
      method: "DELETE",
      headers: { Authorization: "Bearer " + token },
    });
    if (res.ok) {
      setMsg("Usuario eliminado");
      load();
    } else {
      const err = await getErrorMessage(res);
      setMsg(err || "Error al eliminar usuario");
    }
  }

  function confirmDelete(user) {
    setDeletingUser(user);
    setConfirmOpen(true);
  }

  function onCancelDelete() {
    setDeletingUser(null);
    setConfirmOpen(false);
  }

  async function onConfirmDelete() {
    if (!deletingUser) return;
    await remove(deletingUser.id);
    onCancelDelete();
  }

  async function reset(id) {
    const res = await fetch("/api/admin/users/" + id + "/reset-password", {
      method: "POST",
      headers: { Authorization: "Bearer " + token },
    });
    if (res.ok) {
      const data = await res.json();
      setMsg("Contraseña temporal: " + (data.tempPassword || ""));
      load();
    } else {
      const err = await getErrorMessage(res);
      setMsg(err || "Error al restablecer contraseña");
    }
  }

  async function fetchAudit(username) {
    const res = await fetch(
      "/api/admin/audit?username=" + encodeURIComponent(username),
      { headers: { Authorization: "Bearer " + token } }
    );
    if (res.ok) {
      const data = await res.json();
      setAudit(data);
    } else {
      const err = await getErrorMessage(res);
      setMsg(err || "Error al obtener auditoría");
    }
  }

  async function doLogout() {
    await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
    onLogout();
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold">Panel de administración</h2>
        <Button className="bg-red-600 text-white" onClick={doLogout}>
          Cerrar sesión
        </Button>
      </div>

      {msg && <Notification>{msg}</Notification>}

      <div className="grid gap-3">
        {users.map((u) => (
          <div
            key={u.id}
            className="flex items-center justify-between border p-3 rounded-md"
          >
            <div>
              <div className="font-medium">{u.username}</div>
              <div className="text-xs text-slate-500">{u.role}</div>
            </div>
            <div className="flex items-center gap-2">
              <Button
                className="bg-red-500 text-white flex items-center gap-2"
                onClick={() => confirmDelete(u)}
              >
                <DeleteIcon />
                <span>Eliminar</span>
              </Button>
              <Button
                className="bg-gray-600 text-white flex items-center gap-2"
                onClick={() => reset(u.id)}
              >
                <ResetIcon />
                <span>Restablecer</span>
              </Button>
              <Button
                className="bg-blue-600 text-white flex items-center gap-2"
                onClick={() => fetchAudit(u.username)}
              >
                <AuditIcon />
                <span>Auditoría</span>
              </Button>
            </div>
          </div>
        ))}
      </div>

      {audit.length > 0 && (
        <div className="mt-6">
          <h3 className="font-semibold">Auditoría</h3>
          <ul className="mt-2 space-y-1">
            {audit.map((a, idx) => (
              <li key={idx} className="text-sm text-slate-700">
                {new Date(a.timestamp).toLocaleString()} — {a.ip}
              </li>
            ))}
          </ul>
        </div>
      )}
      <ConfirmDialog
        open={confirmOpen}
        title="Confirmar eliminación"
        message={
          deletingUser
            ? `¿Eliminar usuario '${deletingUser.username}'? Esta acción no puede deshacerse.`
            : ""
        }
        onCancel={onCancelDelete}
        onConfirm={onConfirmDelete}
      />
    </div>
  );
}
