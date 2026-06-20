import http from "k6/http";
import { check } from "k6";

// ---------------------------------------------------------------------------
// CONFIGURATION — tweak everything here
// ---------------------------------------------------------------------------

const BASE_URL = __ENV.BASE_URL || "http://localhost:8082";

// Fraction (0..1) of candidate responses that should diverge from the original.
// Forwarded as a query param to demo-original, which ignores it, but Sombra replays
// it to demo-candidate, which honors it. 0 => no discrepancies, 1 => every response diverges.
const DISCREPANCY_RATE = Number(__ENV.DISCREPANCY_RATE || 0);

// Requests draw a random id from [1, MAX_USER_ID] each iteration so the traffic varies.
const MAX_USER_ID = Number(__ENV.MAX_USER_ID || 10000);

export const options = {
  scenarios: {
    load: {
      executor: "constant-arrival-rate",
      rate: 5,
      timeUnit: "1s",
      duration: "30s",
      preAllocatedVUs: 10,
    },
  },
  thresholds: {
    http_req_duration: ["p(95)<500"],
  },
};

// ---------------------------------------------------------------------------
// TEST
// ---------------------------------------------------------------------------

export default function () {
  const id = 1 + Math.floor(Math.random() * MAX_USER_ID);
  const res = http.get(
    `${BASE_URL}/api/users/${id}?divergence=${DISCREPANCY_RATE}`,
  );

  check(res, {
    "status is 200 or 404": (r) => r.status === 200 || r.status === 404,
  });
}
