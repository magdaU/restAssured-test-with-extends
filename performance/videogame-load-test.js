import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'https://videogamedb.uk/api/v2';

export const options = {
  stages: [
    { duration: '30s', target: 10 }, // ramp up
    { duration: '30s', target: 10 }, // hold
    { duration: '10s', target: 0 },  // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // same SLA as VideoGameTests#assertOnResponseTime
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const listRes = http.get(`${BASE_URL}/videogame`);
  check(listRes, {
    'GET /videogame status is 200': (r) => r.status === 200,
  });

  const gameId = Math.floor(Math.random() * 5) + 1; // matches VideoGameParameterizedTests IDs 1-5
  const singleRes = http.get(`${BASE_URL}/videogame/${gameId}`);
  check(singleRes, {
    'GET /videogame/{id} status is 200': (r) => r.status === 200,
  });
}
