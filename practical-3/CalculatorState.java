import java.util.Stack;

public class CalculatorState {

    private StringBuilder expression;
    private String result;

    public CalculatorState() {
        expression = new StringBuilder();
        result = "";
    }

    public void addDigit(String digit) {
        expression.append(digit);
    }

    public void addOperator(String operator) {
        expression.append(" ").append(operator).append(" ");
    }

    public void clear() {
        expression.setLength(0);
        result = "";
    }

    public String getExpression() {
        return expression.toString();
    }

    public String getResult() {
        return result;
    }

    public void evaluate() {
        try {
            result = evaluateExpression(expression.toString());
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }
    }

    private String evaluateExpression(String expr) {
        String[] tokens = expr.split(" ");
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;
            
            if (token.matches("\\d+")) {
                values.push(Double.parseDouble(token));
            } else if (token.matches("[+\\-*/]")) {
                while (!operators.isEmpty() && hasPrecedence(token, operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(token);
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        if (values.isEmpty()) {
            return "";
        }
        
        double result = values.pop();
        
        // If result is a whole number, remove the decimal point
        if (result == Math.floor(result)) {
            return Integer.toString((int) result);
        }
        
        return Double.toString(result);
    }

    private boolean hasPrecedence(String op1, String op2) {
        if ((op1.equals("*") || op1.equals("/")) && (op2.equals("+") || op2.equals("-"))) {
            return false;
        }
        return true;
    }

    private double applyOperator(String operator, double b, double a) {
        switch (operator) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }
}