# Health Insurance System - Concise Overview

This document provides a high-level overview of a simplified Health Insurance System, focusing on core concepts rather than a detailed microservice implementation.

## 1. Simplified Architecture

Instead of a complex microservices architecture, we can consider a **Modular Monolith** approach using Spring Boot. This keeps the system as a single deployable unit but organizes the code into distinct modules, making it easier to manage than a traditional monolith while avoiding the overhead of microservices for a smaller scope.

**Core Modules:**

*   **User Module:** Handles user registration, login, profile management, and authentication/authorization (e.g., using Spring Security).
*   **Plan Module:** Manages insurance plan details, including benefits, premiums, and provider networks. Provides APIs to list and view plans.
*   **Policy Module:** Handles policy creation based on user selection, manages policy lifecycle (effective dates, status), and potentially stores basic policyholder/dependent information.
*   **Payment Module (Simplified):** Integrates with a payment gateway (like Stripe) for processing premium payments. Could initially focus just on one-time payments.
*   **Claims Module (Simplified):** Allows users to submit basic claim information (date of service, amount) and tracks claim status (Submitted, Processing, Approved, Denied). Document upload could be simplified or omitted initially.

**Key Interactions:**

*   Users interact via a Web UI/API Gateway (though for simplicity, direct API access to the monolith is assumed here).
*   Policy Module interacts with User and Plan modules to create policies.
*   Payment Module interacts with Policy Module to trigger payments and update policy status.
*   Claims Module interacts with Policy Module to validate claims against active policies.

**Technology Stack:**

*   **Backend:** Java, Spring Boot
*   **Database:** PostgreSQL (or similar relational DB)
*   **Authentication:** Spring Security (JWT or session-based)
*   **Payment:** Stripe SDK (or similar)
*   **Build:** Maven or Gradle

This simplified structure reduces deployment complexity and inter-service communication overhead compared to the microservices approach.



## 2. Core Data Models (Simplified JPA Entities)

Here are simplified examples of JPA entities for the core modules. Relationships and fields are reduced for brevity.

```java
// --- User Module ---
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password; // Hashed
    @Column(nullable = false)
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    // Roles, Addresses etc. omitted for brevity
}

// --- Plan Module ---
@Entity
@Table(name = "plans")
public class Plan {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String planName;
    private String description;
    @Column(nullable = false)
    private BigDecimal monthlyPremium;
    // Benefits, Provider Networks omitted for brevity
}

// --- Policy Module ---
@Entity
@Table(name = "policies")
public class Policy {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String policyNumber;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false)
    private User user; // Link to the subscriber
    @ManyToOne @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    @Column(nullable = false)
    private LocalDate effectiveDate;
    @Column(nullable = false)
    private LocalDate expirationDate;
    @Column(nullable = false)
    private String status; // e.g., ACTIVE, PENDING, EXPIRED
    // Dependents, Policy Documents omitted for brevity
}

// --- Payment Module (Simplified) ---
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private String status; // e.g., PENDING, SUCCESS, FAILED
    private String gatewayTransactionId; // ID from Stripe/etc.
    @CreationTimestamp
    private LocalDateTime createdAt;
}

// --- Claims Module (Simplified) ---
@Entity
@Table(name = "claims")
public class Claim {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String claimNumber;
    @ManyToOne @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    @Column(nullable = false)
    private LocalDate dateOfService;
    @Column(nullable = false)
    private BigDecimal amountClaimed;
    @Column(nullable = false)
    private String status; // e.g., SUBMITTED, PROCESSING, APPROVED, DENIED
    @CreationTimestamp
    private LocalDateTime submissionDate;
    // Documents, Status History omitted for brevity
}
```



## 3. Essential Code Snippets

Below are simplified examples demonstrating core functionality within the modular monolith.

**Example 1: User Registration (UserService & UserController)**

```java
// --- UserService Interface ---
public interface UserService {
    User registerUser(String username, String password, String email);
}

// --- UserServiceImpl ---
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder; // From Spring Security

    @Override
    public User registerUser(String username, String password, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        // Set default roles, etc.
        return userRepository.save(user);
    }
}

// --- UserController ---
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // DTO for registration request
    public static class RegistrationRequest {
        public String username;
        public String password;
        public String email;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            User newUser = userService.registerUser(request.username, request.password, request.email);
            // Return simplified user info, not the full entity with password
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully: " + newUser.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

**Example 2: Policy Creation (PolicyService & PolicyController)**

```java
// --- PolicyService Interface ---
public interface PolicyService {
    Policy createPolicy(UUID userId, UUID planId, LocalDate effectiveDate);
}

// --- PolicyServiceImpl ---
@Service
public class PolicyServiceImpl implements PolicyService {
    @Autowired private PolicyRepository policyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlanRepository planRepository;

    @Override
    public Policy createPolicy(UUID userId, UUID planId, LocalDate effectiveDate) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));

        Policy policy = new Policy();
        policy.setUser(user);
        policy.setPlan(plan);
        policy.setEffectiveDate(effectiveDate);
        // Calculate expiration date (e.g., 1 year later)
        policy.setExpirationDate(effectiveDate.plusYears(1).minusDays(1));
        policy.setStatus("PENDING"); // Pending until payment
        policy.setPolicyNumber("POL-" + System.currentTimeMillis()); // Simple number generation

        return policyRepository.save(policy);
        // In a real app, trigger payment process here
    }
}

// --- PolicyController ---
@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    @Autowired
    private PolicyService policyService;

    // DTO for policy creation request
    public static class CreatePolicyRequest {
        public UUID userId;
        public UUID planId;
        public LocalDate effectiveDate;
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody CreatePolicyRequest request) {
        try {
            Policy newPolicy = policyService.createPolicy(request.userId, request.planId, request.effectiveDate);
            // Return relevant policy info (e.g., policy number, status)
            return ResponseEntity.status(HttpStatus.CREATED).body("Policy created: " + newPolicy.getPolicyNumber());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```



## 4. Deployment Options (Simplified)

For the modular monolith architecture, deployment can be simpler than managing multiple microservices.

*   **Executable JAR:** Spring Boot applications can be packaged as a single executable JAR file (using `mvn package` or `gradle bootJar`). This JAR includes the embedded server (like Tomcat) and can be run directly on a server with Java installed (`java -jar app.jar`). This is the simplest approach.
*   **Containerization (Docker):** The application can be packaged into a Docker container. A `Dockerfile` would define how to build the image, typically starting from a Java base image, copying the JAR file, and specifying the command to run it. This container can then be deployed on various platforms (Docker hosts, Kubernetes, cloud container services like AWS ECS/EKS, Google Cloud Run/GKE, Azure Container Instances/AKS).
*   **Platform as a Service (PaaS):** Cloud providers offer PaaS solutions (e.g., Heroku, AWS Elastic Beanstalk, Google App Engine, Azure App Service) that simplify deployment. You typically push your code or the packaged JAR, and the platform handles provisioning servers, deploying the application, scaling, and load balancing.

**Considerations:**

*   **Database:** A separate database instance (e.g., PostgreSQL) needs to be provisioned and managed, regardless of the application deployment method.
*   **Configuration:** Externalize configuration (database credentials, API keys) using Spring Cloud Config, environment variables, or platform-specific configuration services.
*   **Scalability:** While a monolith is simpler initially, scaling requires replicating the entire application instance. PaaS and container orchestration platforms help manage this.

