# Running inside a Docker container

> Since 1.1.8

Testcontainers itself can be used from inside a container.
This is very useful for different CI scenarios like running everything in containers on Jenkins, or Docker-based CI tools such as Drone.

Testcontainers will automatically detect if it's inside a container and instead of "localhost" will use the default gateway's IP.

However, additional configuration is required if you use [volume mapping](options.md#volume-mapping). The following points need to be considered:

* The docker socket must be available via a volume mount
* The 'local' source code directory must be volume mounted *at the same path* inside the container that Testcontainers runs in, so that Testcontainers is able to set up the correct volume mounts for the containers it spawns.

## Docker-only example
If you run the tests with just `docker run ...` then make sure you add `-v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock` to the command, so it will look like this:
```bash
$ tree .
.
├── pom.xml
└── src
    └── test
        └── java
            └── MyTestWithTestcontainers.java

$ docker run -it --rm -v $PWD:$PWD -w $PWD -v /var/run/docker.sock:/var/run/docker.sock maven:3 mvn test
```

Where:
* `-v $PWD:$PWD` will add your current directory as a volume inside the container
* `-w $PWD` will set the current directory to this volume
* `-v /var/run/docker.sock:/var/run/docker.sock` will map the Docker socket

## Docker Compose example
The same can be achived with Docker Compose:
```yaml
tests:
  image: maven:3
  stop_signal: SIGKILL
  stdin_open: true
  tty: true
  working_dir: $PWD
  volumes:
    - $PWD:$PWD
    - /var/run/docker.sock:/var/run/docker.sock
    # Maven cache (optional)
    - ~/.m2:/root/.m2
  command: mvn test
```
