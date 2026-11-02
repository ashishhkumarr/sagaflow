import type { Order } from "./api";
import { isInFlight } from "./format";

export function Stats({ orders }: { orders: Order[] }) {
  const inProgress = orders.filter((order) => isInFlight(order.status)).length;
  const confirmed = orders.filter((order) => order.status === "CONFIRMED").length;
  const cancelled = orders.filter((order) => order.status === "CANCELLED").length;

  const tiles = [
    { label: "Orders placed", value: orders.length, tint: "" },
    { label: "In progress", value: inProgress, tint: "wait" },
    { label: "Confirmed", value: confirmed, tint: "ok" },
    { label: "Rolled back", value: cancelled, tint: "bad" },
  ];

  return (
    <section className="stats">
      {tiles.map((tile) => (
        <div key={tile.label} className={tile.tint ? `stat ${tile.tint}` : "stat"}>
          <div className="stat-value">{tile.value}</div>
          <div className="stat-label">{tile.label}</div>
        </div>
      ))}
    </section>
  );
}
