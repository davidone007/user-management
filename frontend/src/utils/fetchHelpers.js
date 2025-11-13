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
