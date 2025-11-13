import React from "react";

export default function Button({ children, className = "", ...props }) {
  return (
    <button
      className={
        "inline-flex items-center justify-center rounded-md px-4 py-2 shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 " +
        className
      }
      {...props}
    >
      {children}
    </button>
  );
}
