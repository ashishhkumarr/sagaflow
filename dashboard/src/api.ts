export type OrderStatus =
  | "NEW"
  | "AWAITING_STOCK"
  | "AWAITING_PAYMENT"
  | "COMPENSATING"
  | "CONFIRMED"
  | "CANCELLED";

export type Order = {
  id: string;
  customerId: string;
  item: string;
  quantity: number;
  amount: number;
  status: OrderStatus;
  cancelReason: string | null;
  createdAt: string;
  updatedAt: string;
};

export type NewOrder = {
  customerId: string;
  item: string;
  quantity: number;
  amount: number;
};

const base = import.meta.env.VITE_API ?? "http://localhost:8081";

export async function listOrders(): Promise<Order[]> {
  const response = await fetch(`${base}/orders`);
  if (!response.ok) throw new Error(`could not load orders (${response.status})`);
  return response.json();
}

export async function placeOrder(order: NewOrder): Promise<Order> {
  const response = await fetch(`${base}/orders`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(order),
  });
  if (!response.ok) throw new Error(`order was rejected (${response.status})`);
  return response.json();
}

export type Step = {
  status: OrderStatus;
  detail: string | null;
  at: string;
};

export type ServiceState = {
  name: string;
  up: boolean;
};

export async function listSteps(orderId: string): Promise<Step[]> {
  const response = await fetch(`${base}/orders/${orderId}/steps`);
  if (!response.ok) throw new Error(`could not load the timeline (${response.status})`);
  return response.json();
}

export async function listServices(): Promise<ServiceState[]> {
  const response = await fetch(`${base}/services`);
  if (!response.ok) throw new Error(`could not check the services (${response.status})`);
  return response.json();
}
