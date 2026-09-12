import type { ServiceState } from "./api";

export function Services({ services }: { services: ServiceState[] }) {
  return (
    <div className="panel">
      <h2>services</h2>

      <div className="services">
        <span className="service">
          <span className="dot done" />
          order
        </span>
        {services.map((service) => (
          <span key={service.name} className="service">
            <span className={service.up ? "dot done" : "dot failed"} />
            {service.name}
          </span>
        ))}
      </div>

      <p className="hint">
        stop one and place an order. it will sit there waiting instead of failing, and
        finish on its own once the service is back.
      </p>
      <pre className="snippet">docker compose stop inventory-db{"\n"}pkill -f inventory-service</pre>
    </div>
  );
}
