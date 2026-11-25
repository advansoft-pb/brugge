# brugge

A small Java/Spring Boot example of an **orchestrated saga**. The `order-service` coordinates inventory reservation and payment, then releases the reservation if payment fails.

> **Demo scope:** service state is held in memory. Restarting a container loses orders, reservations, and charges. Do not use this sample for real payments or production workloads. Cloud Run deployment is intentionally opt-in and the deployment script asks before making the demo APIs public.

## Services and flow

- `order-service` — public-facing API and saga orchestrator.
- `inventory-service` — idempotently reserves/releases inventory by order ID.
- `payment-service` — idempotently charges by order ID; can simulate a payment failure.

```text
POST /orders -> reserve inventory -> charge payment -> CONFIRMED
                                      |
                                      +-- failure -> release reservation -> FAILED
```

## Run locally

Requirements: Java 21, Maven 3.9+, Docker, and Docker Compose.

```bash
docker compose up --build
```

Create a successful order:

```bash
curl -sS -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"customer-1","itemId":"book-1","quantity":2,"amount":19.98}'
```

Exercise compensation (payment fails after the inventory reservation):

```bash
curl -sS -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"customer-1","itemId":"book-1","quantity":1,"amount":12.50,"simulatePaymentFailure":true}'
```

The response includes the order state and any failure/compensation details. Fetch an order using the returned ID:

```bash
curl http://localhost:8080/orders/ORDER_ID
```

Service health endpoints are `/actuator/health` on ports 8080, 8081, and 8082. Stop the local stack with `docker compose down`.

## Build and test

```bash
mvn test
```

## Deploy to Cloud Run (manual, opt-in)

The repository includes `cloudbuild.yaml` and `scripts/deploy-cloud-run.sh`. The script deploys to `project-15526ede-427c-402c-8db` in `europe-central2` by default, but **does not run automatically**. Before using it:

1. Install and authenticate the Google Cloud CLI (`gcloud auth login` and `gcloud auth configure-docker` are not needed; Cloud Build performs the image build).
2. Select/verify the project and make sure billing is enabled.
3. Enable the Cloud Run, Cloud Build, and Artifact Registry APIs, or grant the script's identity permission to enable them.
4. Review the script. It prompts for confirmation because, for this self-contained demo, it exposes all three Cloud Run services without authentication. Type `yes` to continue; any other response exits without deployment.
5. Run `bash scripts/deploy-cloud-run.sh`. Override defaults if needed, e.g. `PROJECT_ID=your-project REGION=europe-central2 bash scripts/deploy-cloud-run.sh`.

The script creates an Artifact Registry repository if missing, builds each service, and deploys inventory and payment before the orchestrator. It makes no GCP changes unless you run it and confirm. Public unauthenticated deployment is for demonstration only; use authenticated service-to-service calls and durable storage before adapting this design for production.

## Saga limitations and next steps

This intentionally uses synchronous HTTP calls and in-memory state to keep the saga mechanics visible. A production version should persist saga/order state, use durable messaging or a workflow engine for retries and recovery, add timeouts/idempotency storage, secure service-to-service traffic, and use real inventory/payment integrations with appropriate reconciliation.