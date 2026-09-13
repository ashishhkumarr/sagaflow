import { useState } from "react";
import { placeOrder } from "./api";
import { capitalize } from "./format";

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
      <h2>Place an Order</h2>

      <label>
        Customer ID
        <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} />
      </label>

      <label>
        Item
        <select value={item} onChange={(e) => setItem(e.target.value)}>
          {items.map((option) => (
            <option key={option} value={option}>
              {capitalize(option)}
            </option>
          ))}
        </select>
      </label>

      <label>
        Quantity
        <input
          type="number"
          min={1}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
        />
      </label>

      <label>
        Amount
        <input
          type="number"
          step="0.01"
          min={0.01}
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
        />
      </label>

      <button type="submit" disabled={sending}>
        {sending ? "Sending…" : "Place Order"}
      </button>

      <p className="hint">
        A customer ID starting with <code>fail-</code> is declined, as is any amount over
        500. Either one makes the saga return the reserved stock.
      </p>

      {error && <p className="error">{error}</p>}
    </form>
  );
}
