import type { ServiceState } from "./api";
import { capitalize } from "./format";

// the order service answers this page, so if it were down nothing here would be
// loading in the first place
const orderService: ServiceState = { name: "order", up: true };

export function Services({ services }: { services: ServiceState[] }) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Services</h2>
      </div>

      <div className="panel-body">
        <div className="services">
          {[orderService, ...services].map((service) => (
            <div key={service.name} className="service">
              <span className={service.up ? "step-dot done" : "step-dot failed"} />
              {capitalize(service.name)}
              <span className={service.up ? "service-state ok" : "service-state bad"}>
                {service.up ? "Up" : "Down"}
              </span>
            </div>
          ))}
        </div>

        <p className="hint">
          Stop a service and place an order. The order waits instead of failing, and finishes on
          its own once the service is back.
        </p>
        <pre className="snippet">docker compose stop inventory-db{"\n"}pkill -f inventory-service</pre>
      </div>
    </section>
  );
}
