import { useCallback, useEffect, useState } from "react";
import { listOrders, listServices, type Order, type ServiceState } from "./api";
import { OrderForm } from "./OrderForm";
import { OrderTable } from "./OrderTable";
import { Services } from "./Services";
import { Stats } from "./Stats";
import { BrandMark, GithubMark } from "./icons";
import "./App.css";

const repo = "https://github.com/ashishhkumarr/sagaflow";

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
      <header className="topbar">
        <div className="brand">
          <BrandMark />
          <div>
            <h1>Sagaflow</h1>
            <p className="tagline">
              An order across four services on Kafka, undone step by step when one fails
            </p>
          </div>
        </div>

        <div className="topbar-right">
          <span className={offline ? "pulse down" : "pulse"} role="status">
            <span className="pulse-dot" />
            {offline ? "Order service unreachable" : "Live"}
          </span>
          <a className="ghost-link" href={repo} target="_blank" rel="noreferrer">
            <GithubMark />
            Source
          </a>
        </div>
      </header>

      <Stats orders={orders} />

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
