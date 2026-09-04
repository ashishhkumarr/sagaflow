import { useCallback, useEffect, useState } from "react";
import { listOrders, type Order } from "./api";
import { OrderForm } from "./OrderForm";
import { OrderTable } from "./OrderTable";
import "./App.css";

export default function App() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [offline, setOffline] = useState(false);

  const refresh = useCallback(async () => {
    try {
      setOrders(await listOrders());
      setOffline(false);
    } catch {
      setOffline(true);
    }
  }, []);

  // orders move on their own once the saga picks them up, so keep asking
  useEffect(() => {
    refresh();
    const timer = setInterval(refresh, 1000);
    return () => clearInterval(timer);
  }, [refresh]);

  return (
    <div className="page">
      <header>
        <h1>order saga</h1>
        <p>
          four services, four databases, talking over kafka. place an order and watch it
          move.
        </p>
        {offline && <p className="error">cannot reach the order service on 8081</p>}
      </header>

      <main>
        <OrderForm onPlaced={refresh} />
        <OrderTable orders={orders} />
      </main>
    </div>
  );
}
