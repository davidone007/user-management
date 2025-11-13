import React, { useState } from "react";
import Input from "../components/Input";
import Button from "../components/Button";
import Notification from "../components/Notification";
import { getErrorMessage } from "../utils/fetchHelpers";

export default function Register() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setMsg(null);
    setLoading(true);
    const res = await fetch("/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    setLoading(false);
    if (res.ok) setMsg("Usuario registrado correctamente");
    else {
      const msg = await getErrorMessage(res);
      setMsg(msg || "Error al registrar");
    }
  }

  return (
    <div>
      <h2 className="text-xl font-semibold mb-4">Registro</h2>
      {msg && <Notification>{msg}</Notification>}
      <form onSubmit={submit} className="flex flex-col gap-3">
        <Input
          label="Usuario"
          placeholder="elige un usuario"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <Input
          label="Contraseña"
          placeholder="elige una contraseña segura"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button
          className="bg-green-600 text-white"
          type="submit"
          disabled={loading}
        >
          {loading ? "Enviando…" : "Registrarse"}
        </Button>
      </form>
    </div>
  );
}
