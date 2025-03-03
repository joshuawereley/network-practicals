# Timezone Display and Switching Application

This project is a web-based application that demonstrates server-side processing using CGI (Common Gateway Interface) programs. The application displays the current time in either Ghana Time (GMT+0) or South African Time (GMT+2) and allows users to switch between these timezones dynamically. The project is implemented in Java and adheres to the requirements outlined in **Practical Assignment 1** of the module.

## Features

- **Display Current Time**: Shows the current time in the selected timezone (Ghana or South Africa).
- **Switch Timezones**: Allows users to switch between Ghana Time and South African Time.
- **Server-Side Execution**: All processing is done on the server side, with no client-side scripting (e.g., JavaScript) used.
- **HTML 5 Compliance**: The generated HTML output adheres to the HTML 5 standard.
- **Error Handling**: Provides basic error handling and displays error messages if something goes wrong.

## Background

This assignment focuses on server-side execution using CGI programs. CGI programs generate dynamic HTML content, which is then sent to the browser for rendering. The project demonstrates the following:
- Installation and configuration of a web server (e.g., Apache).
- Creation and deployment of static HTML pages.
- Development and execution of CGI programs to generate dynamic content.
- Use of a back-end file to store and retrieve timezone offsets.

The project uses Java for the CGI programs, but other server-side languages could also be used. The back-end file (`timezone.txt`) stores the current timezone offset, which is used to calculate and display the current time.

---

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java Development Kit (JDK)**:
   - Install OpenJDK on your system:
   - Ensure `JAVA_HOME` and `PATH` are set correctly in your environment.

2. **Web Server with CGI Support**:
   - Install and configure Apache (or another web server) to support CGI scripts.
   - Ensure the CGI directory is configured to execute scripts.

## Files

- **DisplayTime.java**: The main Java class that displays the current time based on the timezone offset stored in `timezone.txt`.
- **SwitchToGhanaTime.java**: A Java class that sets the timezone offset to GMT+0 (Ghana Time).
- **SwitchToSATime.java**: A Java class that sets the timezone offset to GMT+2 (South African Time).
- **display_time.cgi**: A shell script that runs the `DisplayTime` Java class.
- **switch_to_ghana_time.cgi**: A shell script that runs the `SwitchToGhanaTime` Java class.
- **switch_to_sa_time.cgi**: A shell script that runs the `SwitchToSATime` Java class.
