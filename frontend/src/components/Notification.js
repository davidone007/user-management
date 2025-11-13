import React from "react";

export default function Notification({ children, type = "info" }) {
  const base = "rounded-md p-3 text-sm";
  const styles = {
    info: "bg-blue-50 text-blue-700 border border-blue-100",
    success: "bg-green-50 text-green-700 border border-green-100",
    error: "bg-red-50 text-red-700 border border-red-100",
  };
  return (
    <div className={`${base} ${styles[type] || styles.info}`}>{children}</div>
  );
}
