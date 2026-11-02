import { useEffect, useState, type CSSProperties } from "react";
import { listSteps, type OrderStatus, type Step } from "./api";
import { capitalize, clockTime, duration, statusLabel, tone } from "./format";

const colours: Record<string, string> = {
  done: "var(--ok)",
  failed: "var(--bad)",
  undoing: "var(--warn)",
  waiting: "var(--wait)",
};

function spineColour(status: OrderStatus) {
  return colours[tone(status)];
}

export function Timeline({ orderId }: { orderId: string }) {
  const [steps, setSteps] = useState<Step[]>([]);

  useEffect(() => {
    let live = true;
    const load = () =>
      listSteps(orderId)
        .then((s) => live && setSteps(s))
        .catch(() => {});
    load();
    const timer = setInterval(load, 1000);
    return () => {
      live = false;
      clearInterval(timer);
    };
  }, [orderId]);

  const gaps = steps.map((step, index) => {
    const previous = steps[index - 1];
    if (!previous) return null;
    return Math.max(0, new Date(step.at).getTime() - new Date(previous.at).getTime());
  });

  // the bars are drawn against the slowest step of this order, so a step that
  // waited on a retry or a stuck sweep stands out next to the ones that took
  // milliseconds
  const slowest = Math.max(1, ...gaps.map((gap) => gap ?? 0));

  if (steps.length === 0) {
    return (
      <div className="timeline-wrap">
        <p className="empty">Nothing recorded for this order yet.</p>
      </div>
    );
  }

  return (
    <div className="timeline-wrap">
      <ol className="timeline">
        {steps.map((step, index) => {
          const gap = gaps[index];
          const next = steps[index + 1];

          return (
            <li
              key={`${step.status}-${step.at}-${index}`}
              className={`step ${tone(step.status)}`}
              // the line down to the next step takes that step's colour, so the
              // turn from going forward to undoing is visible on its own
              style={
                next ? ({ "--spine": spineColour(next.status) } as CSSProperties) : undefined
              }
            >
              <span className="step-dot" />
              <span className="step-name">{statusLabel(step.status)}</span>
              <span className="step-detail">{step.detail ? capitalize(step.detail) : ""}</span>
              <span className="step-time">
                {gap === null ? (
                  <span className="step-gap">{clockTime(step.at)}</span>
                ) : (
                  <>
                    <span className="step-bar">
                      <span style={{ width: `${Math.round((gap / slowest) * 100)}%` }} />
                    </span>
                    <span className="step-gap">{duration(gap)}</span>
                  </>
                )}
              </span>
            </li>
          );
        })}
      </ol>
    </div>
  );
}
