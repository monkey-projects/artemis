# ActiveMQ Artemis with Metrics

This repository builds a container image for [ActiveMQ Artemis](https://activemq.apache.org/components/artemis/)
that also exposes [Prometheus](https://prometheus.io) metrics endpoint.  To this end
we include [this plugin](https://github.com/rh-messaging/artemis-prometheus-metrics-plugin)
that provides a library that hooks in to Artemis' metrics system, and servlet that exposes
them as a http endpoint.

## Usage

The image can be found on [DockerHub](https://hub.docker.com/repository/docker/monkeyci/artemis).

Just use this image instead of the default one.  But you will also need to enable the
plugin explicitly by overriding the default `broker.xml`, as described in [step 2 of
the plugin documentation](https://github.com/rh-messaging/artemis-prometheus-metrics-plugin#installing-in-activemq-artemis).

## Building

Builds are executed on [MonkeyCI](https://monkeyci.com).

It executes these steps:
 1. Check out the plugin code from Github
 2. Compile the plugin library and servlet
 3. Download the Artemis source files
 4. Prepare the files for a docker image
 5. Copy the plugin lib and war to their appropriate locations
 6. Build and push the image (for ARM and AMD platforms).

## License

[MIT License](LICENSE)

Copyright (c) 2025 by [Monkey Projects](https://www.monkey-projects.be).