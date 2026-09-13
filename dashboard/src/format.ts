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

// the backend sends reasons, item names and step details in lower case and the tests
// check for them that way, so only the display gets the capital letter
export function capitalize(text: string) {
  return text.charAt(0).toUpperCase() + text.slice(1);
}
