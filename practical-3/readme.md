# Calculator Server

This project is a simple calculator server implemented in Java. The server listens on port 55555 and provides a web-based calculator interface.

## Prerequisites

- Java Development Kit (JDK) installed
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
   javac CalculatorServer.java CalculatorState.java
   ```

3. **Run the server**:

   After compiling the Java files, run the server using the following command:

   ```sh
   java CalculatorServer
   ```

4. **Access the calculator**:

   Open a web browser and navigate to `http://localhost:55555`. You should see the calculator interface.

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
