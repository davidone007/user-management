/**
 * Utility function to extract error messages from HTTP responses.
 * 
 * This function attempts to parse error messages from various response formats:
 * 
 *   JSON responses with message, error, or errors fields
 *   Plain text responses
 *   Fallback to generic error message with status code
 * 
 * 
 * This is useful for displaying user-friendly error messages in the UI
 * regardless of the response format returned by the backend.
 * 
 * @param {Response} response - The fetch Response object
 * @returns {Promise<string>} A promise that resolves to the error message string
 * @module fetchHelpers
 */
export async function getErrorMessage(response) {
  try {
    const ct = response.headers.get("content-type") || "";
    if (ct.includes("application/json")) {
      const data = await response.json();
      if (!data) return `Error del servidor (${response.status})`;
      if (typeof data === "string") return data;
      if (data.message) return data.message;
      if (data.error) return data.error;
      if (data.errors) return JSON.stringify(data.errors);
      // Fallback to JSON string
      return JSON.stringify(data);
    } else {
      const text = await response.text();
      return text || `Error del servidor (${response.status})`;
    }
  } catch (e) {
    return `Error del servidor (${response.status})`;
  }
}
