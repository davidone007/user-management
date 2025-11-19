/**
 * Notification component for displaying messages to users.
 * 
 * This component displays styled notification messages with different
 * color schemes based on the notification type.
 * 
 * Available types:
 * 
 *   info - Blue styling for informational messages
 *   success - Green styling for success messages
 *   error - Red styling for error messages
 * 
 * 
 * @param {React.ReactNode} children - The message content to display
 * @param {string} type - The notification type: "info", "success", or "error" (default: "info")
 * @module Notification
 * @component
 */
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
