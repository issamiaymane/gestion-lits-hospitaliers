# Gestion des Lits Hospitaliers

> Hospital bed management system with real-time availability tracking, built with Spring Boot microservices. **[Academic]**

**Mini Projet - Architecture Distribuée : Spring Boot Microservices & Docker**

Université Mohammed V de Rabat - Ecole Nationale Supérieure d'Arts et Métiers (ENSAM)
Filière INDIA (2ème année - S4) | A.U. : 2025/2026 | Pr. A. El Qadi

## Membres du groupe

| Membre | Rôle |
|--------|------|
| **Aymane Issami** | Développeur Full-Stack |
| **Houssam Kichchou** | Développeur Full-Stack |

## Description du projet

Système de gestion des lits et des flux hospitaliers permettant d'optimiser l'affectation des lits, le transfert entre services et la planification des sorties pour réduire les temps d'attente aux urgences.

### Acteurs
- **Urgentistes / Médecins régulateurs** - Gestion des admissions d'urgence
- **Cadre de santé / Gestionnaire de lits** - Affectation et suivi des lits
- **Brancardiers / Logistique interne** - Transport et nettoyage
- **Direction** - Tableau de bord opérationnel

## Architecture Microservices

```
                    ┌─────────────────┐
                    │   API Gateway   │
                    │   (port 8080)   │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
    ┌─────────▼──┐  ┌───────▼────┐  ┌──────▼───────┐
    │    Bed     │  │  Patient   │  │  Capacity    │
    │ Management │  │   Flow     │  │  Prediction  │
    │ (8081)     │  │  (8082)    │  │  (8083)      │
    └────────────┘  └────────────┘  └──────────────┘
              │              │              │
    ┌─────────▼──┐  ┌───────▼────┐         │
    │Notification│  │ Dashboard  │◄────────┘
    │    Ops     │  │  Service   │
    │  (8084)    │  │  (8085)    │
    └────────────┘  └────────────┘
              │              │
    ┌─────────▼──────────────▼────────────┐
    │        Eureka Discovery Service      │
    │             (port 8761)              │
    └─────────────────────────────────────┘
```

### Microservices

| Service | Port | Rôle |
|---------|------|------|
| **discovery-service** | 8761 | Eureka Server - Registre des services |
| **gateway-service** | 8080 | API Gateway - Point d'entrée unique |
| **bed-management-service** | 8081 | Gestion des lits (CRUD, statut, affectation) |
| **patient-flow-service** | 8082 | Flux patients (admission, transfert, sortie) |
| **capacity-prediction-service** | 8083 | Prédiction de capacité et taux d'occupation |
| **notification-ops-service** | 8084 | Notifications opérationnelles |
| **dashboard-service** | 8085 | Tableau de bord agrégé et KPIs |

## Technologies utilisées

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Cloud 2023.0.0** (Eureka, Gateway)
- **Spring Data JPA** (Hibernate)
- **MySQL 8.0** (production) / **H2** (développement)
- **Docker & Docker Compose**
- **Swagger / OpenAPI 3** (documentation API)
- **Maven** (gestion des dépendances)

## Modèle de données

### Bed (Lit)
```
Bed {
    id, bedNumber, ward, roomNumber, floor,
    bedType (STANDARD | ICU | PEDIATRIC | MATERNITY),
    status (FREE | OCCUPIED | RESERVED | CLEANING),
    currentPatientId, lastStatusChange, notes
}
```

**Cycle de vie d'un lit :**
```
FREE ──► OCCUPIED (affectation patient)
FREE ──► RESERVED (réservation)
RESERVED ──► OCCUPIED (patient arrive)
RESERVED ──► FREE (annulation)
OCCUPIED ──► CLEANING (libération/sortie patient)
CLEANING ──► FREE (nettoyage terminé)
```

### Patient
```
Patient {
    id, firstName, lastName, dateOfBirth, gender, nationalId,
    contactPhone, emergencyContact,
    currentStatus (REGISTERED | ADMITTED | IN_TRANSFER | DISCHARGED | EMERGENCY),
    currentWard, admissionDate, dischargeDate, medicalNotes
}
```

**Trajectoire patient :** `Enregistrement → Admission → Transfert(s) → Sortie`

### PatientTransfer
```
PatientTransfer {
    id, patientId, fromWard, toWard, transferDate,
    reason, authorizedBy,
    status (PENDING | IN_PROGRESS | COMPLETED | CANCELLED)
}
```

## Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Option 1 : Avec Docker Compose (recommandé)

```bash
# 1. Compiler tous les services
mvn clean package -DskipTests

# 2. Lancer tous les conteneurs
docker-compose up --build

# 3. Accéder aux services
# Eureka Dashboard : http://localhost:8761
# API Gateway :      http://localhost:8080
# Swagger UIs :
#   - Bed Management :  http://localhost:8081/swagger-ui.html
#   - Patient Flow :    http://localhost:8082/swagger-ui.html
#   - Capacity :        http://localhost:8083/swagger-ui.html
#   - Notifications :   http://localhost:8084/swagger-ui.html
#   - Dashboard :       http://localhost:8085/swagger-ui.html
```

### Option 2 : En local (développement)

```bash
# 1. Lancer le service de découverte en premier
cd discovery-service && mvn spring-boot:run &

# 2. Attendre que Eureka démarre, puis lancer les autres services
cd bed-management-service && mvn spring-boot:run &
cd patient-flow-service && mvn spring-boot:run &
cd capacity-prediction-service && mvn spring-boot:run &
cd notification-ops-service && mvn spring-boot:run &
cd dashboard-service && mvn spring-boot:run &

# 3. Lancer le gateway en dernier
cd gateway-service && mvn spring-boot:run &
```

## Endpoints API

### Bed Management Service (`/api/beds`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/beds` | Lister tous les lits (filtres: ward, status) |
| GET | `/api/beds/{id}` | Obtenir un lit par ID |
| POST | `/api/beds` | Créer un nouveau lit |
| PUT | `/api/beds/{id}/status` | Modifier le statut d'un lit |
| DELETE | `/api/beds/{id}` | Supprimer un lit |
| POST | `/api/beds/assign` | Affecter un lit à un patient |
| POST | `/api/beds/{id}/release` | Libérer un lit |
| GET | `/api/beds/statistics` | Statistiques globales |
| GET | `/api/beds/assignments/{bedId}` | Historique des affectations |

### Patient Flow Service (`/api/patients`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/patients` | Lister les patients (filtres: ward, status) |
| GET | `/api/patients/{id}` | Obtenir un patient par ID |
| POST | `/api/patients` | Enregistrer un patient |
| POST | `/api/patients/{id}/admit` | Admettre un patient |
| POST | `/api/patients/{id}/transfer` | Initier un transfert |
| POST | `/api/patients/transfers/{id}/complete` | Compléter un transfert |
| POST | `/api/patients/{id}/discharge` | Sortie du patient |
| GET | `/api/patients/{id}/transfers` | Historique des transferts |
| GET | `/api/patients/statistics` | Statistiques des flux |

### Capacity Prediction Service (`/api/capacity`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/capacity/current` | Capacité actuelle globale |
| GET | `/api/capacity/prediction/{ward}` | Prédiction pour un service |
| GET | `/api/capacity/predictions` | Toutes les prédictions |
| POST | `/api/capacity/record` | Enregistrer un snapshot |

### Notification Ops Service (`/api/notifications`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/notifications` | Lister les notifications |
| GET | `/api/notifications/{id}` | Obtenir une notification |
| POST | `/api/notifications` | Créer une notification |
| PUT | `/api/notifications/{id}/read` | Marquer comme lue |
| PUT | `/api/notifications/{id}/sent` | Marquer comme envoyée |
| GET | `/api/notifications/ward/{ward}/unread` | Non lues par service |
| GET | `/api/notifications/statistics` | Statistiques |
| POST | `/api/notifications/bed-release` | Alerte libération lit |
| POST | `/api/notifications/cleaning` | Alerte nettoyage |
| POST | `/api/notifications/transfer-alert` | Alerte transfert |

### Dashboard Service (`/api/dashboard`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/dashboard` | Tableau de bord complet |
| GET | `/api/dashboard/kpis` | Indicateurs clés (KPIs) |

## Collection Postman

La collection Postman est disponible dans le dossier `postman/` :
- `Hospital_Bed_Management.postman_collection.json`

Importer ce fichier dans Postman pour tester toutes les APIs.

## Structure du projet

```
gestion-lits-hospitaliers/
├── pom.xml                          # Parent POM
├── docker-compose.yml               # Orchestration Docker
├── README.md
├── postman/                         # Collection Postman
├── discovery-service/               # Eureka Server (8761)
├── gateway-service/                 # API Gateway (8080)
├── bed-management-service/          # Gestion des lits (8081)
│   ├── controller/BedController
│   ├── model/Bed, BedAssignment, BedStatus, BedType
│   ├── repository/BedRepository, BedAssignmentRepository
│   ├── service/BedService
│   └── dto/BedDTO, CreateBedRequest, ...
├── patient-flow-service/            # Flux patients (8082)
│   ├── controller/PatientFlowController
│   ├── model/Patient, PatientTransfer, PatientStatus, TransferStatus
│   ├── repository/PatientRepository, PatientTransferRepository
│   ├── service/PatientFlowService
│   └── dto/PatientDTO, CreatePatientRequest, ...
├── capacity-prediction-service/     # Prédictions (8083)
│   ├── controller/CapacityController
│   ├── model/OccupancyRecord
│   ├── repository/OccupancyRecordRepository
│   ├── service/CapacityPredictionService
│   └── dto/CapacityPredictionDTO, ...
├── notification-ops-service/        # Notifications (8084)
│   ├── controller/NotificationController
│   ├── model/Notification, NotificationType, Priority, NotificationStatus
│   ├── repository/NotificationRepository
│   ├── service/NotificationService
│   └── dto/NotificationDTO, ...
└── dashboard-service/               # Dashboard (8085)
    ├── controller/DashboardController
    ├── service/DashboardService
    └── dto/DashboardDTO, BedOverviewDTO, ...
```

## Livrables

- [x] Architecture microservices complète (7 services)
- [x] Modèle de données "lit/patient/service" avec cycle de vie
- [x] API REST documentée (OpenAPI/Swagger)
- [x] Collection Postman pour tester les APIs
- [x] docker-compose.yml fonctionnel avec tous les services
- [ ] Rapport PDF (à rédiger séparément)
