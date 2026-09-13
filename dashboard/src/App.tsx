import { useCallback, useEffect, useState } from "react";
import { listOrders, listServices, type Order, type ServiceState } from "./api";
import { OrderForm } from "./OrderForm";
import { OrderTable } from "./OrderTable";
import { Services } from "./Services";
import "./App.css";

export default function App() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [services, setServices] = useState<ServiceState[]>([]);
  const [offline, setOffline] = useState(false);

  const refresh = useCallback(async () => {
    try {
      setOrders(await listOrders());
      setOffline(false);
    } catch {
      setOffline(true);
    }
    try {
      setServices(await listServices());
    } catch {
      setServices([]);
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
        <h1>Order Saga</h1>
        <p>
          Four services and four databases, communicating over Kafka. Place an order, then
          click it to see the path it took.
        </p>
        {offline && <p className="error">Cannot reach the order service on port 8081.</p>}
      </header>

      <main>
        <div className="column">
          <OrderForm onPlaced={refresh} />
          <Services services={services} />
        </div>
        <OrderTable orders={orders} />
      </main>
    </div>
  );
}
