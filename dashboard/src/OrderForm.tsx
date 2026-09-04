import { useState } from "react";
import { placeOrder } from "./api";

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
      setError(e instanceof Error ? e.message : "something went wrong");
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="panel" onSubmit={submit}>
      <h2>place an order</h2>

      <label>
        customer
        <input value={customerId} onChange={(e) => setCustomerId(e.target.value)} />
      </label>

      <label>
        item
        <select value={item} onChange={(e) => setItem(e.target.value)}>
          {items.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </label>

      <label>
        quantity
        <input
          type="number"
          min={1}
          value={quantity}
          onChange={(e) => setQuantity(Number(e.target.value))}
        />
      </label>

      <label>
        amount
        <input
          type="number"
          step="0.01"
          min={0.01}
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
        />
      </label>

      <button type="submit" disabled={sending}>
        {sending ? "sending..." : "place order"}
      </button>

      <p className="hint">
        a customer id starting with <code>fail-</code> gets declined, and so does any
        amount over 500. either one makes the saga roll the stock back.
      </p>

      {error && <p className="error">{error}</p>}
    </form>
  );
}
