## Building the app

```bash
./mvnw clean package
```
will compile the app, run the integration tests and produce jar files in the target sub-folders.

## Running from jar files
You will need a running kafka instance that you can spin up with docker-compose.

```bash
sh docker/docker-start.sh
```

You may stop the instance once you're finished.
```bash
sh docker/docker-stop.sh
```

Once kafka is up, run both apps in separate terminals:

```bash
java -jar order-status-service/target/order-status-service-0.0.1-SNAPSHOT.jar
```

```bash
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar
```

## Running with maven

The following command will run the app from the compiled sources.
You'll need a running postgres db as described in the next section.
```bash
./mvnw spring-boot:run
```

## Available commands

When you send an order event to the order service, it forwards it to the order status service via a kafka topic.
The latter responds to with a status data via another topic. The former service then prints out the received status.

You can post the json payload via postman or via curl in the command line:

```bash
curl --request POST -sL \
     -H "Content-Type: application/json" \
     --data '{"product":"Eiderdown","quantity":13}' \
     http://localhost:8080/api/v1/orders
```

Observe the log message from the terminal where the order service is running:

```text
Received message: OrderStatus[status=PROCESS, date=2024-02-28T13:44:01.849155811Z]
Key: null; Partition: 0; Topic: order-status-topic, Timestamp: 1709127841961
```

## Configuration

The app reads its configuration from the `src/main/resources/application.yml` files.
You may override any of the parameters from the command line using the `-D` flag,
