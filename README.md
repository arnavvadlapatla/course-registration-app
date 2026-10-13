# Course Registration: Maven, Jenkins and Kubernetes

Multi-module Maven project for a course registration app, with a Jenkins pipeline and a Minikube deployment of Nginx.

- Feature branch: `branch-fix` (merged into `main`)
- Remote: https://github.com/arnavvadlapatla/course-registration-app.git

## 1. Setup and execution

Run from the repository root:

```powershell
mvn clean test        # build and run all tests
mvn package           # build the JAR
kubectl apply -f k8s\deployment.yaml   # deploy to Minikube
kubectl apply -f k8s\service.yaml
minikube service nginx-service --url   # get the URL
```

Requirements: JDK 21, Maven, Docker Desktop, Minikube, kubectl, Jenkins.

## 2. Git evidence

Branch: `branch-fix`, merged into `main` with `git merge --no-ff`.

| Commit | Purpose |
|---|---|
| `48f844a` | Defect fix: capacity check changed from `>` to `>=` |
| `08992e9` | Multi-module Maven refactor |
| `805dbc3` | Renamed child module to `course-registration-app` |
| `e48b6b7` | Capacity boundary tests |
| `660a247` | Prerequisite equivalence rule (CS3A1 satisfies CS301) and its tests |
| `<<< hash >>>` | Jenkinsfile |
| `<<< hash >>>` | Kubernetes manifests and evidence |
| `<<< hash >>>` | Merge into `main` |

History:

```
<<< paste output of: git log --oneline --graph --all >>>
```

## 3. Maven configuration and build evidence

```
course-registration/
├── pom.xml                      (parent, packaging pom)
└── course-registration-app/
    ├── pom.xml                  (child)
    └── src/main/java, src/test/java
```

- **Parent POM:** defines `java.version` (17) and `junit.version` (5.11.0), declares the `course-registration-app` module, manages the JUnit dependency version in `dependencyManagement`, and sets the Compiler and Surefire plugin versions in `pluginManagement`.
- **Child POM:** inherits from the parent through `<parent>` with `<relativePath>../pom.xml</relativePath>`. It declares JUnit without a version, since the version comes from the parent.
- **Reactor:** running Maven at the root builds the parent, then the child.

Results:

| Command | Result |
|---|---|
| Baseline (before changes) | 9 tests, 0 failures, 0 errors |
| `mvn clean test` (final) | 19 tests, 0 failures, 0 errors |
| `mvn package` | BUILD SUCCESS, `course-registration-app/target/course-registration-app-1.0-SNAPSHOT.jar` |

The 19 tests are 3 in `CourseTest` and 16 in `RegistrationServiceTest` (6 original, 4 capacity, 6 equivalence).

## 4. Jenkins pipeline

Stages: Checkout, Test, Package, Archive Artifact. Maven runs from the repository root, and the pipeline uses `bat` because Jenkins runs on Windows.

- **Publishing test reports:** the `junit` step is in `post { always }` of the Test stage, so reports from `course-registration-app/target/surefire-reports/TEST-*.xml` are published whether tests pass or fail.
- **Blocking packaging on failure:** if any test fails, `mvn clean test` exits with a non-zero code and the Test stage fails. A declarative pipeline stops at the first failed stage, so Package and Archive Artifact are skipped and no JAR is archived.
- **Archived artifact:** `course-registration-app/target/*.jar`.

Evidence:
- Successful run: `<<< screenshots: stage view, console with Tests run: 19, test result page, archived JAR >>>`
- Failing-test run (capacity bug temporarily reintroduced on a throwaway branch): `<<< screenshots: Test failed, Package and Archive skipped >>>`

Note: the first build failed with `'mvn' is not recognized` because Maven was not on the Jenkins service's system PATH. It was fixed by adding Maven to the system PATH and restarting Jenkins.

## 5. Kubernetes deployment evidence (Minikube)

`k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name: nginx
          image: nginx:1.27
          ports:
            - containerPort: 80
```

`k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
  labels:
    app: nginx
spec:
  type: NodePort
  selector:
    app: nginx
  ports:
    - port: 80
      targetPort: 80
      nodePort: 30080
```

Outputs (full transcript in `k8s/kubectl-evidence.txt`):

```
<<< kubectl get deployments / kubectl get pods -o wide >>>
<<< kubectl get endpoints nginx-service (IPs match the Pod IPs) >>>
<<< curl.exe result showing "Welcome to nginx!" >>>
<<< pods after scaling to 3 >>>
<<< pods after deleting one (replacement created) >>>
```

**How the Deployment keeps the replica count:** a Deployment creates a ReplicaSet, and the ReplicaSet controller keeps comparing the desired number of Pods (`replicas`) with the actual number. When I scaled from 2 to 3, it created one more Pod. When I deleted a Pod, the actual count dropped below the desired count, so the controller created a replacement. The Service does not count Pods; it routes traffic to every Pod whose labels match its selector.

## 6. Change summary and limitations

**Defect fix:** the capacity check used `>`, so a full course (for example 30 of 30) still accepted a new student. It now uses `>=`. A full course rejects registration, the last seat is accepted, and a rejected attempt changes nothing, because all checks run before any state is modified.

**Change request (Question Set G):** a course requiring CS301 is also satisfied by completing CS3A1. This is a narrow rule in `RegistrationService`. The codes are not treated as identical elsewhere, so duplicate-registration checks still use the real course code.

**Modified files:**
- `pom.xml` (new parent POM)
- `course-registration-app/pom.xml` (child POM)
- `course-registration-app/src/main/java/com/university/RegistrationService.java`
- `course-registration-app/src/test/java/com/university/RegistrationServiceTest.java`
- `Jenkinsfile`
- `k8s/deployment.yaml`, `k8s/service.yaml`, `k8s/kubectl-evidence.txt`
- `README.md`

**Known limitations:**
- The equivalence rule is hard-coded for CS301 and CS3A1 and works in one direction only (CS3A1 can stand in for CS301, not the reverse).
- The capacity fix was committed before its tests, so the commit order is fix, then tests.
- The prerequisite rule and its tests are in one commit.
- `<<< add any other limitation, e.g. Jenkins run done locally only >>>`