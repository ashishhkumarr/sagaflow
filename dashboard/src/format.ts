import type { OrderStatus } from "./api";

const labels: Record<OrderStatus, string> = {
  NEW: "New",
  AWAITING_STOCK: "Awaiting stock",
  AWAITING_PAYMENT: "Awaiting payment",
  COMPENSATING: "Compensating",
  CONFIRMED: "Confirmed",
  CANCELLED: "Cancelled",
};

export function statusLabel(status: OrderStatus) {
  return labels[status];
}

// the pills, the timeline dots and the tiles all read a status the same way, so the
// mapping lives here once. compensating gets its own colour because it is the one
// state that means the order is going backwards
export function tone(status: OrderStatus) {
  if (status === "CONFIRMED") return "done";
  if (status === "CANCELLED") return "failed";
  if (status === "COMPENSATING") return "undoing";
  return "waiting";
}

export function isInFlight(status: OrderStatus) {
  return status !== "CONFIRMED" && status !== "CANCELLED";
}

// the backend sends reasons, item names and step details in lower case and the tests
// check for them that way, so only the display gets the capital letter
export function capitalize(text: string) {
  return text.charAt(0).toUpperCase() + text.slice(1);
}

// four digits of milliseconds is hard to read at a glance, so anything past a
// second is shown in seconds instead
export function duration(ms: number) {
  if (ms < 1000) return `${ms} ms`;
  if (ms < 10000) return `${(ms / 1000).toFixed(2)} s`;
  return `${(ms / 1000).toFixed(1)} s`;
}

export function age(iso: string) {
  const seconds = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return `${seconds}s ago`;
  if (seconds < 3600) return `${Math.round(seconds / 60)}m ago`;
  return `${Math.round(seconds / 3600)}h ago`;
}

export function clockTime(iso: string) {
  return new Date(iso).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}
