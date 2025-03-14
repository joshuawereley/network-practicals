import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Calculation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String expression;
    private String result;
    private LocalDateTime timestamp;
    
    public Calculation(String expression, String result) {
        this.expression = expression;
        this.result = result;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getExpression() {
        return expression;
    }
    
    public String getResult() {
        return result;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter) + " - " + expression + " = " + result;
    }
}