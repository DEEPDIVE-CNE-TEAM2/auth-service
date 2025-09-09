#!/bin/bash
# ==============================
# deploy-service.sh
# Gradle build + Docker build + ECR push 자동화
# 서비스별 변수 사용 가능
# ==============================

# --------- 사용자 정의 변수 ----------
NAME=${1:-auth}
SERVICE_NAME=${2:-auth-service}      # 첫 번째 인자: 서비스명, 기본값 auth-service
ECR_URI=${3:-004407157704.dkr.ecr.ap-northeast-2.amazonaws.com/$SERVICE_NAME} # 두 번째 인자: ECR URI
GRADLE_TASK=${4:-build}             # 세 번째 인자: gradle task, 기본값 build
AWS_REGION=${5:-ap-northeast-2}     # AWS 리전
K8S_NAMESPACE=${6:-production}        # Kubernetes namespace, 기본값 production
K8S_MANIFEST_PATH=${7:-/root/k8s-resource/$NAME/$NAME-all.yaml} # 매니페스트 경로
# ------------------------------------

echo "=== 1️⃣ Gradle Build ==="
./gradlew $GRADLE_TASK || { echo "Gradle build failed"; exit 1; }

echo "=== 2️⃣ Docker Build ==="
docker build -t $SERVICE_NAME . || { echo "Docker build failed"; exit 1; }

echo "=== 3️⃣ ECR 로그인 ==="
aws ecr get-login-password --region $AWS_REGION | \
docker login --username AWS --password-stdin $ECR_URI || { echo "ECR login failed"; exit 1; }

echo "=== 4️⃣ Docker Tag ==="
docker tag $SERVICE_NAME:latest $ECR_URI:latest || { echo "Docker tag failed"; exit 1; }

echo "=== 5️⃣ Docker Push ==="
docker push $ECR_URI:latest || { echo "Docker push failed"; exit 1; }

echo "=== 6️⃣ Kubernetes Deployment ==="

# 기존 Deployment 삭제
echo "Deleting existing deployment $SERVICE_NAME in namespace $K8S_NAMESPACE..."
kubectl -n $K8S_NAMESPACE delete deployment $SERVICE_NAME --ignore-not-found

# 매니페스트 적용
echo "Applying Kubernetes manifest: $K8S_MANIFEST_PATH"
kubectl apply -f $K8S_MANIFEST_PATH || { echo "kubectl apply failed"; exit 1; }

echo "✅  Deployment completed for $SERVICE_NAME in namespace $K8S_NAMESPACE"
