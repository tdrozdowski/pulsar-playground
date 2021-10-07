# pulsar-playground
Various Scala Musings with Apache Pulsar

Just a collection of various Scala/ZIO code exploring Apache Pulsar. Feel free to poke around to learn as well.

This assumes you have a Pulsar cluster setup with Minikube or in Docker's built in Kubernetes server.  The examples here currently have local IPs hardcoded - you'll need to change those yourself to get things running.

The docker-compose file can be used for a Standalone instance - however it does NOT work with M1 based Macbooks as of Pulsar 2.8.1
