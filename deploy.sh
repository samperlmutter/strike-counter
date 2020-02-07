#!/usr/bin/env bash

./gradlew bootJar
gcloud compute scp --zone us-east1-b build/libs/strike-counter-0.0.1-SNAPSHOT.jar strikes-api:~/strike-counter.jar
gcloud compute ssh --zone us-east1-b strikes-api --command="sudo systemctl restart strike-counter"