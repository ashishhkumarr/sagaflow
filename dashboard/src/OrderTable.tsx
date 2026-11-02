import { Fragment, useState } from "react";
import type { Order } from "./api";
import { age, capitalize, isInFlight, statusLabel, tone } from "./format";
import { Chevron } from "./icons";
import { Timeline } from "./Timeline";

function pillClass(order: Order) {
  const base = `pill ${tone(order.status)}`;
  return isInFlight(order.status) ? `${base} working` : base;
}

export function OrderTable({ orders }: { orders: Order[] }) {
  const [open, setOpen] = useState<string | null>(null);
  const working = orders.filter((order) => isInFlight(order.status)).length;

  function toggle(id: string) {
    setOpen(open === id ? null : id);
  }

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Orders</h2>
        <span className="count">
          {orders.length} placed · {working} in progress
        </span>
      </div>

      <div className="panel-body">
        {orders.length === 0 ? (
          <p className="empty">
            <span className="empty-strong">Nothing has been ordered yet.</span>
            Place an order on the left, then click its row to follow the steps.
          </p>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>Order</th>
                  <th className="hide-sm">Customer</th>
                  <th>Item</th>
                  <th className="right">Amount</th>
                  <th>Status</th>
                  <th className="right hide-sm">Updated</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => {
                  const isOpen = open === order.id;

                  return (
                    <Fragment key={order.id}>
                      <tr
                        className={isOpen ? "order-row open" : "order-row"}
                        tabIndex={0}
                        aria-expanded={isOpen}
                        onClick={() => toggle(order.id)}
                        onKeyDown={(event) => {
                          if (event.key === "Enter" || event.key === " ") {
                            event.preventDefault();
                            toggle(order.id);
                          }
                        }}
                      >
                        <td className="cell-id">
                          <Chevron />
                          {order.id.slice(0, 8)}
                        </td>
                        <td className="hide-sm">{order.customerId}</td>
                        <td className="cell-item">
                          {capitalize(order.item)} <span className="qty">×{order.quantity}</span>
                        </td>
                        <td className="cell-amount">{order.amount.toFixed(2)}</td>
                        <td>
                          <span className={pillClass(order)}>{statusLabel(order.status)}</span>
                          {order.cancelReason && (
                            <div className="reason">{capitalize(order.cancelReason)}</div>
                          )}
                        </td>
                        <td className="cell-age hide-sm">{age(order.updatedAt)}</td>
                      </tr>

                      {isOpen && (
                        <tr>
                          <td colSpan={6} className="timeline-cell">
                            <Timeline orderId={order.id} />
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
}
