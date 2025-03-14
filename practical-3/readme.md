# Calculator Server with History Logging

This project implements a web-based calculator system with two servers:

1. A calculator server on port 55554 that processes calculations
2. A logging server on port 55556 that stores and displays calculation history

This demonstrates HTTP protocol features including server-to-server communication, proper request/response handling, and content display.

## Prerequisites

- Java Development Kit (JDK) 8 or higher
- A web browser

## Instructions

1. **Clone the repository**:

   ```sh
   git clone https://github.com/your-username/network-practicals.git
   cd network-practicals/practical-3
   ```

2. **Compile the Java files**:

   Open a terminal or command prompt and navigate to the `practical-3` directory. Then run the following command to compile the Java files:

   ```sh
   javac Calculation.java CalculatorState.java CalculatorServer.java LoggingServer.java
   ```

3. **Run the server**:

   After compiling the Java files, run the server using the following command:

   ```sh
   java CalculatorServer
   ```

   Open another terminal window and run
   java LoggingServer

4. **Access the calculator**:

   Open a web browser and navigate to `http://localhost:55554`. You should see the calculator interface.
   Open a web browser and navigate to `http://localhost:55556`. You should see the calculator history.

5. **Using the calculator**:

   - Click the digit buttons (0-9) to enter numbers.
   - Click the operator buttons (+, -, \*, /) to perform operations.
   - Click the "=" button to evaluate the expression.
   - Click the "C" button to clear the expression.

## Troubleshooting

- If you encounter any issues, make sure that all the necessary files are in the same directory and that there are no compilation errors.
- Ensure that the JDK is properly installed and configured on your system.

## License

This project is licensed under the MIT License. See the [LICENSE](../LICENSE) file for details.
