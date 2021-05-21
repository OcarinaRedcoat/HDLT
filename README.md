# HDLT
Highly Dependable Location Tracker

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```


### Running

To run the Client and HACLient:

```
mvn exec:java 
```

To run the Server:

```
mvn exec:java -Dexec.args="<server_id> <server password>"
By default the <server password> is server_<server_id>
```

### Run Tests

```
mvn verify 
```

The integration tests are skipped because they require the servers to be running.



### Note:

The client 13 has the wrong key, because of tests porpuses: 

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework

