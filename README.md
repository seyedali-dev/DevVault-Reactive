## DevVault - Spring Boot Project Manager 🚀

A powerful project management application called DevVault, built with Spring Boot and inspired by Trello. Manage your projects, tasks, and team members with ease, while enjoying role-based functionalities and secure authentication.

### Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Setup](#setup)
- [Contributing](#contributing)
- [License](#license)
- [Future Plans](#future-plans)

## Features

- Create and manage projects like Trello
- Role-based functionalities:
  - `PROJECT_LEADER`: Manage projects, assign tasks, and oversee team members
  - `PROJECT_ADMIN`: Manage tasks, assign team members, and moderate chats
  - `TEAM_MEMBER`: Collaborate on tasks and communicate with other team members
- JWT token security for authentication and authorization
- Mark tasks as done with a simple click (to be implemented)
- Group and private chatting for seamless communication (to be implemented)
- Real-time updates with WebSockets (to be implemented)
- Reactive programming with Spring WebFlux (to be implemented)
- Front-end (to be implemented)

## Technologies

- Spring Boot: A powerful framework for building Java-based applications
- JWT: Secure authentication and authorization with JSON Web Tokens
- Spring Data JPA: Simplify database access and management
- MySQL: A reliable and widely-used relational database
- Spring WebFlux (to be implemented): Reactive programming with Spring
- WebSocket (to be implemented): Real-time communication between clients and servers
- Role-based authentication and authorization: Manage user permissions based on their roles

## Setup

1. Clone the repository:

```bash
git clone https://github.com/sayedxali/DevVault.git
```

2. Navigate to the project directory:

```bash
cd DevVault
```

3. Build the project with Maven:

```bash
mvn clean install
```

4. Run the application:

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

## Contributing

As a beginner in Git and GitHub, I'm learning alongside the development of this project. Contributions are welcome! If you have ideas or suggestions, feel free to open an issue or submit a pull request.

## License

This project is licensed under myself 😁.

## Future Plans

After completing the development of DevVault as a _monolithic application_, I plan to transition it to a **microservices architecture**. This will allow for better scalability and maintainability, as well as provide an opportunity to learn more about microservices and their implementation.
