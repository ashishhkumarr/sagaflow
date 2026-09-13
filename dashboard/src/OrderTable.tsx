import { Fragment, useState } from "react";
import type { Order, OrderStatus } from "./api";
import { capitalize, statusLabel } from "./format";
import { Timeline } from "./Timeline";

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
  const [open, setOpen] = useState<string | null>(null);
  const working = orders.filter((order) => inFlight.includes(order.status)).length;

  return (
    <div className="panel">
      <h2>
        Orders{" "}
        <span className="count">
          {orders.length} shown, {working} in progress
        </span>
      </h2>

      {orders.length === 0 && <p className="hint">No orders yet. Place one using the form.</p>}

      <table>
        <tbody>
          {orders.map((order) => (
            <Fragment key={order.id}>
              <tr
                className="clickable"
                onClick={() => setOpen(open === order.id ? null : order.id)}
              >
                <td className="mono">{order.id.slice(0, 8)}</td>
                <td>{order.customerId}</td>
                <td>
                  {capitalize(order.item)} <span className="dim">×{order.quantity}</span>
                </td>
                <td className="mono">{order.amount.toFixed(2)}</td>
                <td>
                  <span className={statusClass(order.status)}>{statusLabel(order.status)}</span>
                  {order.cancelReason && (
                    <div className="reason">{capitalize(order.cancelReason)}</div>
                  )}
                </td>
                <td className="dim">{age(order.updatedAt)}</td>
              </tr>
              {open === order.id && (
                <tr>
                  <td colSpan={6} className="timeline-cell">
                    <Timeline orderId={order.id} />
                  </td>
                </tr>
              )}
            </Fragment>
          ))}
        </tbody>
      </table>
    </div>
  );
}
