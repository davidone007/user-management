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
