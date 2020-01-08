# foosball-slack-integration

![Build status](https://github.com/maniekq/foosball-slack-integration/workflows/Java%20CI/badge.svg)


## Development

### Building

```bash
./gradlew clean build
```

### Releasing

1. Change version number in build.gradle
2. Run command to push new Docker image
    ```bash
    ./gradlew jib -PdockerHubPassword=***********
    ```