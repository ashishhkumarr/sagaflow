import { useEffect, useState } from "react";
import { listSteps, type Step } from "./api";

// the compensating step is the interesting one, it is where the order stops going
// forward and starts undoing what it already did
function marker(status: Step["status"]) {
  if (status === "CONFIRMED") return "dot done";
  if (status === "CANCELLED") return "dot failed";
  if (status === "COMPENSATING") return "dot undoing";
  return "dot waiting";
}

function clock(iso: string) {
  return new Date(iso).toLocaleTimeString();
}

export function Timeline({ orderId }: { orderId: string }) {
  const [steps, setSteps] = useState<Step[]>([]);

  useEffect(() => {
    let live = true;
    const load = () => listSteps(orderId).then((s) => live && setSteps(s)).catch(() => {});
    load();
    const timer = setInterval(load, 1000);
    return () => {
      live = false;
      clearInterval(timer);
    };
  }, [orderId]);

  return (
    <ol className="timeline">
      {steps.map((step, index) => {
        const previous = steps[index - 1];
        const gap = previous
          ? `+${Math.max(0, new Date(step.at).getTime() - new Date(previous.at).getTime())}ms`
          : clock(step.at);

        return (
          <li key={`${step.status}-${step.at}-${index}`}>
            <span className={marker(step.status)} />
            <span className="step-name">{step.status}</span>
            {step.detail && <span className="step-detail">{step.detail}</span>}
            <span className="step-gap">{gap}</span>
          </li>
        );
      })}
      {steps.length === 0 && <li className="dim">nothing recorded yet</li>}
    </ol>
  );
}
