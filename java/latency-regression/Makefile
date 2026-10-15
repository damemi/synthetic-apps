K8S_NAMESPACE ?= java-latency-regression
IMAGE := ghcr.io/odigos-io/synthetic-apps/java-latency-regression:latest
DEPLOY_DIR := deployments/latency-regression

.PHONY: build load-to-kind apply rollout deploy loadgen loadgen-stop regression-on regression-off regression-status status clean

build:
	docker build -f $(DEPLOY_DIR)/Dockerfile -t $(IMAGE) .

load-to-kind:
	kind load docker-image $(IMAGE)

apply:
	kubectl create namespace $(K8S_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl apply -n $(K8S_NAMESPACE) -f $(DEPLOY_DIR)/k8s.yaml
	kubectl apply -n $(K8S_NAMESPACE) -f $(DEPLOY_DIR)/odigos-instrument.yaml

rollout:
	kubectl rollout restart deployment/latency-regression -n $(K8S_NAMESPACE)
	kubectl rollout status deployment/latency-regression -n $(K8S_NAMESPACE) --timeout=180s

deploy:
	$(MAKE) build
	$(MAKE) load-to-kind
	$(MAKE) apply
	$(MAKE) rollout

loadgen:
	kubectl apply -n $(K8S_NAMESPACE) -f $(DEPLOY_DIR)/loadgen.yaml

loadgen-stop:
	kubectl delete -n $(K8S_NAMESPACE) -f $(DEPLOY_DIR)/loadgen.yaml --ignore-not-found=true

# The toggle is in-process state: it resets to REGRESSION_ENABLED on restart.
regression-on:
	kubectl exec -n $(K8S_NAMESPACE) deploy/latency-regression-loadgen -- \
		curl -sS -X POST "http://latency-regression:8080/admin/regression?enabled=true"; echo

regression-off:
	kubectl exec -n $(K8S_NAMESPACE) deploy/latency-regression-loadgen -- \
		curl -sS -X POST "http://latency-regression:8080/admin/regression?enabled=false"; echo

regression-status:
	kubectl exec -n $(K8S_NAMESPACE) deploy/latency-regression-loadgen -- \
		curl -sS "http://latency-regression:8080/admin/regression"; echo

status:
	kubectl get pods -n $(K8S_NAMESPACE)
	kubectl get services -n $(K8S_NAMESPACE)

clean:
	kubectl delete namespace $(K8S_NAMESPACE) --ignore-not-found=true
