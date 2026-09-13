import type { ServiceState } from "./api";
import { capitalize } from "./format";

export function Services({ services }: { services: ServiceState[] }) {
  return (
    <div className="panel">
      <h2>Services</h2>

      <div className="services">
        <span className="service">
          <span className="dot done" />
          Order
        </span>
        {services.map((service) => (
          <span key={service.name} className="service">
            <span className={service.up ? "dot done" : "dot failed"} />
            {capitalize(service.name)}
          </span>
        ))}
      </div>

      <p className="hint">
        Stop a service and place an order. The order waits instead of failing, and finishes
        on its own once the service is back.
      </p>
      <pre className="snippet">docker compose stop inventory-db{"\n"}pkill -f inventory-service</pre>
    </div>
  );
}
