import type { Order, OrderStatus } from "./api";

// the middle three mean the order is still being worked on
const inFlight: OrderStatus[] = ["NEW", "AWAITING_STOCK", "AWAITING_PAYMENT", "COMPENSATING"];

function statusClass(status: OrderStatus) {
  if (status === "CONFIRMED") return "pill done";
  if (status === "CANCELLED") return "pill failed";
  if (status === "COMPENSATING") return "pill undoing";
  return "pill waiting";
}

function age(iso: string) {
  const seconds = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 1000));
  if (seconds < 60) return `${seconds}s ago`;
  return `${Math.round(seconds / 60)}m ago`;
}

export function OrderTable({ orders }: { orders: Order[] }) {
  const working = orders.filter((order) => inFlight.includes(order.status)).length;

  return (
    <div className="panel">
      <h2>
        orders <span className="count">{orders.length} shown, {working} still going</span>
      </h2>

      {orders.length === 0 && <p className="hint">nothing yet, place one on the left</p>}

      <table>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td className="mono">{order.id.slice(0, 8)}</td>
              <td>{order.customerId}</td>
              <td>
                {order.item} <span className="dim">x{order.quantity}</span>
              </td>
              <td className="mono">{order.amount.toFixed(2)}</td>
              <td>
                <span className={statusClass(order.status)}>{order.status}</span>
                {order.cancelReason && <div className="reason">{order.cancelReason}</div>}
              </td>
              <td className="dim">{age(order.updatedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
