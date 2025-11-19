/**
 * Reusable button component with consistent styling.
 * 
 * This component provides a styled button that accepts all standard
 * button props (onClick, disabled, type, etc.) and applies Tailwind CSS
 * classes for consistent appearance across the application.
 * 
 * Styling:
 * 
 *   Base styles: rounded corners, padding, shadow, focus ring
 *   Custom className can be passed to override or extend styles
 *   Disabled state automatically applies opacity
 * 
 * 
 * @param {React.ReactNode} children - The content to display inside the button
 * @param {string} className - Additional CSS classes to apply
 * @param {...Object} props - All other standard button props (onClick, disabled, type, etc.)
 * @module Button
 * @component
 */
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
