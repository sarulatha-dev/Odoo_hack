# SubscriptIQ – Subscription Management System

**A Unified Platform for Subscription Lifecycle and Revenue Management**

## 1. Project Overview

SubscriptIQ is a web-based Subscription Management System designed to streamline subscription operations and centralize business processes within a single platform.

The system aims to simplify the management of products, subscription plans, customers, quotations, invoices, and payments. It provides role-based access for administrators, staff, and customers, supporting efficient operations, improved visibility, and better management of subscription-related activities.

## 2. Objectives

- Centralize subscription-related business operations.
- Simplify product, plan, and customer management.
- Streamline quotation, invoicing, and payment workflows.
- Provide role-specific access to system functionalities.
- Improve visibility into subscription performance and revenue.
- Establish a scalable architecture for future enhancements.

## 3. Core Functionalities

| Module | Description |
|---|---|
| Dashboard | Provides a consolidated overview of key business metrics and activities. |
| Product Management | Supports the creation, maintenance, and management of products. |
| Subscription Plan Management | Enables configuration and management of subscription plans and pricing. |
| Customer Management | Maintains customer profiles and related subscription information. |
| Quotation Management | Supports quotation creation, pricing calculations, tax calculations, and status tracking. |
| Invoice Management | Supports customer billing and invoice management. |
| Payment Management | Provides payment tracking and transaction status management. |
| Role-Based Access Control | Organizes system access according to user roles and permissions. |
| Revenue Analytics | Provides insights into subscription performance and revenue-related activities. |

## 4. User Roles and Access

The application is designed to support the following user roles:

**Administrator**
- Manage overall system operations.
- Manage users, products, and subscription plans.
- Monitor customers, quotations, invoices, and payments.
- Access administrative dashboards and reports.

**Staff**
- Perform assigned operational activities.
- Manage permitted customer and subscription-related tasks.
- Access modules according to assigned permissions.

**Customer**
- Access personal account information.
- View subscription and plan details.
- Access quotations and invoices.
- View payment information.

## 5. Technology Stack

| Layer | Technology |
|---|---|
| Frontend | React.js |
| Build Tool | Vite |
| UI Styling | Tailwind CSS |
| Backend | Java Spring Boot |
| Database | MySQL |
| Authentication and Authorization | Spring Security and JWT |
| API Architecture | RESTful APIs |
| Version Control | Git and GitHub |
| Deployment | Cloud platform (planned) |

## 6. Repository Structure

```text
Odoo_hack/
│
├── backend/
│   └── Spring Boot backend application
│
├── frontend/
│   └── React frontend application
│
├── CHECKLIST.md
│
├── PRD Subscription Management System
│
├── .gitattributes
├── .gitignore
│
└── README.md
```

## 7. System Architecture

SubscriptIQ follows a frontend-backend architecture that separates the presentation layer, business logic, and data persistence.

```text
              Users
                |
                v
        React Frontend
                |
                v
          RESTful APIs
                |
                v
       Java Spring Boot
                |
        Business Logic
                |
                v
          MySQL Database
```

The frontend is responsible for user interaction and presentation. The backend manages business logic, API processing, authentication, authorization, and database operations.

## 8. Installation and Execution

### Prerequisites

Ensure the following software is installed:

- Node.js and npm
- Java Development Kit (JDK)
- Apache Maven
- MySQL Server
- Git

### Step 1: Clone the Repository

```bash
git clone https://github.com/sarulatha-dev/Odoo_hack.git
```

### Step 2: Navigate to the Repository

```bash
cd Odoo_hack
```

### Step 3: Configure and Run the Backend

```bash
cd backend
```

Configure the MySQL connection and other application properties according to the backend configuration.

Run the Spring Boot application:

```bash
mvn spring-boot:run
```

### Step 4: Install and Run the Frontend

Open a separate terminal:

```bash
cd Odoo_hack/frontend
npm install
npm run dev
```

Access the application using the local development URL displayed in the terminal. The default Vite URL is:

```text
http://localhost:5173/
```

## 9. Security and Access Management

The system is designed with role-based access management to ensure users can access only the functionalities permitted by their assigned roles.

Planned security capabilities include:

- JWT-based authentication.
- Spring Security integration.
- Backend-enforced role and permission validation.
- Protected API endpoints.
- Input validation and secure data handling.

Backend authorization is the authoritative security control; frontend access restrictions are intended for user-interface management only.

## 10. Planned Enhancements

- Automated subscription renewals and lifecycle management.
- Payment gateway integration.
- Automated invoice generation.
- Subscription churn analysis and prediction.
- Revenue leakage detection.
- Advanced financial dashboards and reporting.
- Automated email notifications and reminders.
- Cloud deployment and production scalability.

## 11. Project Status

SubscriptIQ is under active development as a hackathon project. The repository maintains separate frontend and backend components to support modular development and integration.

Feature availability and integration status may evolve as development progresses.

## 12. Contributors

Developed collaboratively as part of a hackathon initiative.

## 13. License

This project is currently intended for hackathon and educational purposes. Licensing terms will be defined by the project contributors.
