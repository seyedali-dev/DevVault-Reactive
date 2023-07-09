# 🚀 DevVault

DevVault is a project management application built with Spring Boot. It allows users to create projects, add members to projects, create tasks within projects, and assign tasks to project members. The application also supports user authentication and authorization using JWT tokens.

## Table of Contents

- [Features](#-features)
- [Technologies Used](#-technologies-used)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Authentication](#-authentication)
- [Projects](#-projects)
- [Tasks](#-tasks)
- [Comments](#-comments)
- [Contributing](#-contributing)
- [License](#-license)

## 🛠️ Technologies Used

- Java 11
- Spring Boot
- Spring Security
- JWT
- Hibernate
- Maven

## 🎉 Features

- User Registration and Login with JWT
- Project Creation
- Adding Members to Project
- Task Creation in Project
- Assigning Task to Members of Project
- Project Roles: Project Leader, Project Admin, Team Member
- CRUD Operations on Projects
- Commenting on Projects and Tasks
- Task Completion Tracking

--> More features like Real-time notification, Reactive WebFlux, Integrating external tools like Jira, etc ... are for future plans and will be implemented as I learn them!

## 🚀 Getting Started

These instructions will get you a copy of the project up and running on your local machine.

### Prerequisites

To run this project, you will need to have the following installed:

- Java 11 or higher
- Maven

### Installation

1. Clone the repository:

```
git clone https://github.com/your-username/devvault.git
```

2. Build the application:

```
cd devvault
mvn clean install
```

3. Run the application:

```
java -jar target/devvault-0.0.1-SNAPSHOT.jar
```

The application will be available at `http://localhost:8080`.

## 🔒 Authentication

Users can register and login to the application using JWT tokens. To register, a user must provide a username, email, and password. To login, the user must provide their email and password. The application will respond with a JWT token that the user can use to authenticate subsequent requests.

## 🚧 Projects

Users can create projects and become project leaders. They can also add other users to the project and specify their roles as project admins or team members. CRUD operations are available for projects, allowing project leaders to update or delete projects as needed.

## 📝 Tasks

Within a project, users can create tasks and assign them to project members. Tasks can be marked as completed when they are finished. Users can also comment on tasks, which will only be visible to other users who have been assigned the same task.

## 💬 Comments

Users can also leave comments on projects, which will be visible to all members of the project. This can be used to discuss project-related issues or provide updates on the project's progress.

## 🤝 Contributing

Contributions to DevVault are welcome! If you find a bug or have a feature request, please open an issue on the GitHub repository. If you'd like to contribute code, please fork the repository and submit a pull request with your changes.

## 📄 License

DevVault is licensed under the MIT License. See the `LICENSE` file for more information.
