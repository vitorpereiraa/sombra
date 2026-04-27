import http from "k6/http";
import { check } from "k6";
import exec from "k6/execution";

// ---------------------------------------------------------------------------
// CONFIGURATION — tweak everything here
// ---------------------------------------------------------------------------

const BASE_URL = __ENV.BASE_URL || "http://localhost:8082";

const USER_IDS = [
  1,   // match    — same data on both versions
  2,   // mismatch — different name, email, extra field
  999, // mismatch — 404 on original, 200 on candidate
];

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
  const id = USER_IDS[exec.scenario.iterationInTest % USER_IDS.length];
  const res = http.get(`${BASE_URL}/api/users/${id}`);

  check(res, {
    "status is 200 or 404": (r) => r.status === 200 || r.status === 404,
  });
}
