// small inline svgs instead of an icon package. there are only four of them and
// they all take their colour from the text around them

// the same mark as the favicon, so the tab and the page agree. an s that runs
// forward in the blue of an order in flight and comes back in the amber of one
// being undone
export function BrandMark() {
  return (
    <svg className="brand-mark" viewBox="0 0 32 32" aria-hidden="true">
      <rect width="32" height="32" rx="7" fill="var(--surface-2)" />
      <rect x="0.5" y="0.5" width="31" height="31" rx="6.5" fill="none" stroke="var(--line)" />
      <g fill="none" strokeWidth="3.6" strokeLinecap="round">
        <path d="M22 11.2C22 8.6 19.2 7.2 16 7.2S10 8.9 10 11.8c0 3 3 4.1 6 4.8" stroke="var(--wait)" />
        <path d="M16 16.6c3 .7 6 1.6 6 4s-2.8 4.4-6 4.4-6-1.4-6-4" stroke="var(--warn)" />
      </g>
    </svg>
  );
}

export function GithubMark() {
  return (
    <svg viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
      <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-2.92-.88-2.92-2.84 0-.56.2-1.02.53-1.38-.05-.13-.23-.66.05-1.37 0 0 .55-.17 1.8.66a6.6 6.6 0 0 1 1.64-.22c.56 0 1.12.07 1.64.22 1.25-.84 1.8-.66 1.8-.66.28.71.1 1.24.05 1.37.33.36.53.82.53 1.38 0 1.97-1.15 2.64-2.93 2.84.3.26.56.76.56 1.54 0 1.11-.01 2.02-.01 2.3 0 .21.15.46.55.38A7.99 7.99 0 0 0 16 8c0-4.42-3.58-8-8-8Z" />
    </svg>
  );
}

export function Chevron() {
  return (
    <svg className="chevron" viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <path d="m6 4 4 4-4 4" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function WarningMark() {
  return (
    <svg viewBox="0 0 16 16" fill="none" aria-hidden="true">
      <circle cx="8" cy="8" r="6.9" stroke="currentColor" strokeWidth="1.4" />
      <path d="M8 4.8v4" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
      <circle cx="8" cy="11" r="0.85" fill="currentColor" />
    </svg>
  );
}
