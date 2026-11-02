import { useState } from "react";
import { placeOrder } from "./api";
import { capitalize } from "./format";
import { WarningMark } from "./icons";

const items = ["red shoe", "green hat", "blue shirt", "black jacket"];

export function OrderForm({ onPlaced }: { onPlaced: () => void }) {
  const [customerId, setCustomerId] = useState("cust-1");
  const [item, setItem] = useState(items[0]);
  const [quantity, setQuantity] = useState(1);
  const [amount, setAmount] = useState(49.99);
  const [error, setError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setSending(true);
    setError(null);
    try {
      await placeOrder({ customerId, item, quantity, amount });
      onPlaced();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Something went wrong.");
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="panel" onSubmit={submit}>
      <div className="panel-head">
        <h2>Place an Order</h2>
      </div>

      <div className="panel-body">
        <label className="field">
          <span className="field-label">Customer ID</span>
          <input
            className="mono-input"
            value={customerId}
            onChange={(e) => setCustomerId(e.target.value)}
          />
        </label>

        <label className="field">
          <span className="field-label">Item</span>
          <select value={item} onChange={(e) => setItem(e.target.value)}>
            {items.map((option) => (
              <option key={option} value={option}>
                {capitalize(option)}
              </option>
            ))}
          </select>
        </label>

        <div className="row-2">
          <label className="field">
            <span className="field-label">Quantity</span>
            <input
              className="mono-input"
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
            />
          </label>

          <label className="field">
            <span className="field-label">Amount</span>
            <input
              className="mono-input"
              type="number"
              step="0.01"
              min={0.01}
              value={amount}
              onChange={(e) => setAmount(Number(e.target.value))}
            />
          </label>
        </div>

        <button type="submit" disabled={sending}>
          {sending ? "Sending…" : "Place Order"}
        </button>

        {error && (
          <p className="error" role="alert">
            <WarningMark />
            {error}
          </p>
        )}

        <p className="hint">
          A customer ID starting with <code>fail-</code> is declined, as is any amount over 500.
          Either one makes the saga return the reserved stock.
        </p>
      </div>
    </form>
  );
}
