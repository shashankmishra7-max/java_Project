# CodeArena - student login system

A full-stack web application for compiling and running code in multiple programming languages with Docker sandbox execution.

## Features

✅ **Multiple Languages** - Java, Python, C++  
✅ **Docker Sandbox Execution** - Secure code execution in isolated containers  
✅ **Code Editor** - Monaco Editor (VSCode's editor) with syntax highlighting  
✅ **Save User Programs** - Save, load, and manage your code snippets  
✅ **Authentication System** - JWT-based user authentication  
✅ **Execution Time Limit** - Configurable timeout (default: 10 seconds)  
✅ **Memory Limit** - Memory limit for execution (default: 256MB)

## Technology Stack

### Backend
- Spring Boot 3.x
- Java 17
- MySQL Database
- JWT Authentication
- Maven

### Frontend
- React 18
- Monaco Editor
- Vite
- Axios

## Prerequisites

1. **Java 17** or higher
2. **Node.js 18** or higher
3. **MySQL Server** (or use default configuration)
4. **Maven**
5. **Python, Java, C++ compilers** installed on the system

## Project Structure

```
d:/JAVA_PROJECT/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/codearena/
│   │   ├── config/          # Security configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # JWT security
│   │   └── service/         # Business logic
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                # React frontend
│   ├── src/
│   │   ├── App.jsx          # Main application
│   │   ├── main.jsx        # Entry point
│   │   └── index.css       # Styles
│   ├── package.json
│   └── vite.config.js
└── SPEC.md                 # Project specification
```

## Setup & Installation

### Backend Setup

1. Navigate to the backend directory:
```bash
cd d:/JAVA_PROJECT/backend
```

2. Update `application.properties` with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/codearena
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Build and run the backend:
```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd d:/JAVA_PROJECT/frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT

### Programs (Protected)
- `GET /api/programs` - Get all user's saved programs
- `POST /api/programs` - Save a new program
- `GET /api/programs/{id}` - Get specific program
- `PUT /api/programs/{id}` - Update program
- `DELETE /api/programs/{id}` - Delete program

### Execution (Public)
- `POST /api/execute` - Execute code
  - Request: `{ "language": "java|python|cpp", "code": "..." }`
  - Response: `{ "output": "...", "executionTime": 123, "status": "SUCCESS|ERROR|TIMEOUT", "memoryUsed": "..." }`

## Usage

1. Open `http://localhost:5173` in your browser
2. Click "Sign In" to register or login
3. Select a programming language from the dropdown
4. Write your code in the Monaco Editor
5. Click "Run" to execute your code
6. View the output in the console below
7. Click "Save" to save your program for later

## Security Considerations

- Passwords are hashed using BCrypt
- JWT tokens are used for authentication
- Code execution is sandboxed using process isolation
- Time and memory limits prevent infinite loops

## License

MIT License

