#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${PROJECT_ID:-project-15526ede-427c-402c-8db}"
REGION="${REGION:-europe-central2}"
AR_REPOSITORY="${AR_REPOSITORY:-saga-demo}"

cat <<WARNING
This script will create/update resources in Google Cloud project: ${PROJECT_ID}
Region: ${REGION}
It will deploy all three demo services with unauthenticated public access.
This is only suitable for a disposable demo; do not use with real customer/payment data.
WARNING
read -r -p 'To continue, type yes: ' confirmation
if [[ "$confirmation" != "yes" ]]; then
  echo 'Cancelled; no GCP changes were made.'
  exit 0
fi

gcloud config set project "$PROJECT_ID"
gcloud services enable run.googleapis.com cloudbuild.googleapis.com artifactregistry.googleapis.com --project "$PROJECT_ID"
if ! gcloud artifacts repositories describe "$AR_REPOSITORY" --location "$REGION" --project "$PROJECT_ID" >/dev/null 2>&1; then
  gcloud artifacts repositories create "$AR_REPOSITORY" \
    --repository-format=docker \
    --location="$REGION" \
    --description='Docker images for the Brugge saga demo' \
    --project="$PROJECT_ID"
fi

build_and_deploy() {
  local service="$1"
  shift
  local image="${REGION}-docker.pkg.dev/${PROJECT_ID}/${AR_REPOSITORY}/${service}:latest"
  gcloud builds submit . \
    --project="$PROJECT_ID" \
    --config=cloudbuild.yaml \
    --substitutions="_SERVICE=${service},_IMAGE=${image}"
  gcloud run deploy "$service" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --image="$image" \
    --platform=managed \
    --allow-unauthenticated \
    --port=8080 \
    --set-env-vars="$*" \
    --quiet
  gcloud run services describe "$service" \
    --project="$PROJECT_ID" \
    --region="$REGION" \
    --format='value(status.url)'
}

INVENTORY_URL="$(build_and_deploy inventory-service 'SERVER_PORT=8080')"
PAYMENT_URL="$(build_and_deploy payment-service 'SERVER_PORT=8080')"
build_and_deploy order-service "INVENTORY_SERVICE_URL=${INVENTORY_URL},PAYMENT_SERVICE_URL=${PAYMENT_URL},SERVER_PORT=8080" >/dev/null

echo 'Deployment complete. Order API URL:'
gcloud run services describe order-service --project="$PROJECT_ID" --region="$REGION" --format='value(status.url)'
