/**
 * Reusable input field component with label.
 * 
 * This component provides a styled input field with an optional label.
 * It accepts all standard input props (type, value, onChange, placeholder, etc.)
 * and applies Tailwind CSS classes for consistent appearance.
 * 
 * Features:
 * 
 *   Optional label displayed above the input
 *   Consistent border and focus styling
 *   Supports all HTML input types (text, password, email, etc.)
 * 
 * 
 * @param {string} label - Optional label text to display above the input
 * @param {string} id - Optional HTML id attribute for the input element
 * @param {...Object} props - All other standard input props (type, value, onChange, placeholder, required, etc.)
 * @module Input
 * @component
 */
import React from "react";

export default function Input({ label, id, ...props }) {
  return (
    <label className="flex flex-col text-sm gap-1">
      {label && <span className="font-medium text-slate-700">{label}</span>}
      <input
        id={id}
        className="border border-slate-200 rounded-md p-2 focus:ring-2 focus:ring-blue-300"
        {...props}
      />
    </label>
  );
}
