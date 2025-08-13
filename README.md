# CloudBasedVPN – Full Stack VPN as a Service

Welcome to **CloudBasedVPN**, a full-stack subscription-based VPN service built on top of **AWS Cloud**, **Spring Boot**, and **React**. This project dynamically creates VPN instances on demand and delivers secure, browser-based VPN access to users, depending on their active subscription plans.

**🌐 Live Demo:**  
🔗 [https://cloudbasedvpn.netlify.app](https://cloudbasedvpn.netlify.app)

---

## Project Overview

CloudBasedVPN is designed to provide scalable and secure VPN access to users based on their chosen subscription plans (Free Trial, Monthly, Yearly). The backend leverages AWS EC2 instances to dynamically spin up WireGuard VPN servers, while the frontend offers a smooth and responsive user experience.

Key features include:

- User authentication with JWT and role-based access
- VPN provisioning via AWS EC2
- QR code and config-based VPN connection
- Razorpay payment integration
- Admin and User dashboards
- Subscription management and security policies

---

## Tech Stack

**Frontend:**
- Vite
- React
- TypeScript
- Tailwind CSS
- shadcn/ui

**Backend:**
- Spring Boot
- MySQL
- Spring Security + JWT
- AWS SDK for EC2
- Razorpay SDK
- SSHJ for SSH connections
- WireGuard VPN

---

## Getting Started – Clone and Run Locally

### Clone the Repository

```bash
# Step 1: Clone the repository
git clone https://github.com/jaidev32228/CloudBasedVPN.git

# Step 2: Navigate to the frontend project
cd Frontend

# Step 3: Install dependencies
npm install

# Step 4: Start the development server
npm run dev

# Step 5: Navigate to the backend project
cd ../Backend

#Step 6: Adding the resources directory
Create a new resource directory in src folder and add applications.properties

#step 7: Adding details to applications.properties
spring.application.name={YOUR_PROJECT_NAME}
spring.datasource.url={YOUR_DB_URL}
spring.datasource.username={YOUR_DB_USERNAME}
spring.datasource.password={YOUR_DB_PASSWORD]
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
jwt.secret={YOUR_JWT_SECRET_KEY}

jwt.expiration={JWT_DURATION_IN_SEC}

razorpay.key.id={YOUR_RAZORPAY_ID}
razorpay.key.secret={YOUR_RAZORPAY_SECRET_KEY}
EMAIL_ADDRESS_PASSWORD={YOUR_EMAIL_APP_PASSCODE}

aws.access-key-id={YOUR_AWS_ACCOUNT_KEY_ID}
aws.secret-access-key={YOUR_AWS_ACCOUNT_SECRET_ACCESS_KEY}
aws.region={YOUR_AWS_REGION}
aws.security-group={YOUR_AWS_INSTANCE_SECURITY_GROUP_ID}
aws.ami-id={YOUR_AWS_INSTANCE_AMI_ID}
aws.instance-type={YOUR_AWS_INSTANCE_TYPE}
aws.key-pair-name={YOUR_AWS_SECRET_KEY_PAIR}

ssh.username={YOUR_SSH_USERNAME}
ssh.private-key-path={YOUR_PRIVATE_KEY_PATH}


server.port={YOUR_SERVER_PORT}

#Step 7: Add an extension to run the spring boot application
https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack

#Step 8: Run the Application
Run it by the clicking the spring icon in the left icon and select the these application and click on the run button
