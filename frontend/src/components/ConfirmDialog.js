/**
 * Modal confirmation dialog component.
 * 
 * This component displays a modal dialog for confirming destructive actions
 * (e.g., deleting a user). It includes a backdrop overlay and a centered dialog box.
 * 
 * Features:
 * 
 *   Conditional rendering based on open prop
 *   Backdrop click to cancel
 *   Title and message display
 *   Cancel and confirm buttons
 * 
 * 
 * Usage: Typically used before performing irreversible operations like deletion.
 * 
 * @param {boolean} open - Whether the dialog should be visible
 * @param {string} title - The dialog title text
 * @param {string} message - The confirmation message to display
 * @param {Function} onConfirm - Callback function called when user confirms
 * @param {Function} onCancel - Callback function called when user cancels or clicks backdrop
 * @module ConfirmDialog
 * @component
 */
import React from "react";

export default function ConfirmDialog({
  open,
  title,
  message,
  onConfirm,
  onCancel,
}) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onCancel} />
      <div className="bg-white rounded-lg shadow-lg p-6 z-10 w-full max-w-md">
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        <p className="text-sm text-slate-600 mb-4">{message}</p>
        <div className="flex justify-end gap-3">
          <button className="px-4 py-2 rounded-md border" onClick={onCancel}>
            Cancelar
          </button>
          <button
            className="px-4 py-2 rounded-md bg-red-600 text-white"
            onClick={onConfirm}
          >
            Eliminar
          </button>
        </div>
      </div>
    </div>
  );
}
