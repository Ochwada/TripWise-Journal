# 🚍 TripJournal (Journal  Service - Microservice 4)


---

### 🔗 [TripWise-Architecture 🍀 Overview Repository ](https://github.com/Ochwada/TripWise-Architecture)

Microservices ⬇️ part of **TripWise System**


#### 🖇️ Microservice 0: TripHub - [ Gateway  Service]( )
#### 🖇️ Microservice 1: TripPass - [ Authentication Service](https://github.com/Ochwada/TripWise-Pass)
#### 🖇️ Microservice 2: TripProfile - [ User Profile Service ](https://github.com/reyhanovelek/TripProfile-Service)
#### 🖇️ Microservice 3: TripPlanner - [ Planner Service](https://github.com/Jind19/TripWise_Planner)
#### 🖇️ Microservice 4: TripJournal - [ Journal Service](https://github.com/Ochwada/TripWise-Journal)
#### 🖇️ Microservice 5: TripWeather  - [ Weather Service](https://github.com/OrnellaDelVicario/tripwise_tripweather)
#### 🖇️ Microservice 6: TripMedia - [ Media Service](https://github.com/Ochwada/TripWise-Media)

---

## About TripJournal

TripJournal is a microservice within the TripWise ecosystem, responsible for capturing and storing activities, memories, 
and travel notes.

It complements other services like TripPlanner (itinerary management) and TripMediaVault (photo storage), providing
a personal journaling experience for travelers. Users can write trip logs, add highlights, and associate them with trips 
and destinations.

Built with Spring Boot 3 and MongoDB, TripJournal is designed to be lightweight, flexible, and scalable. It integrates 
securely with TripPass (auth service) for JWT-based access.

##  Features
- Create, update, and delete journals/notes 
- Tag journals by trip, city, or itinerary item 
- Link with TripMediaVault for photo attachments 
- Secure endpoints with JWT from TripPass 
- Integration with *TripPlanner* and TripWeather 
- Document-oriented persistence with MongoDB 
- Deployable via Docker, CI/CD pipelines, and AWS EC2

##  Project Structure
```yaml
tripjournal/
├─ pom.xml
├─ src/main/java/com/triphub/TripJournal/
│ ├─ TripJournalApplication.java
│ ├─ config/
│ │ ├─ SecurityConfig.java
│ │ ├─ MongoIndexConfig.java
│ │ └─ RequestContext.java
│ ├─ security/
│ │ ├─ JwtAuthenticationFilter.java
│ │ ├─ JwtVerifier.java
│ │ └─ JwtPrincipal.java
│ ├─ controller/
│ │ ├─ JournalController.java
│ │ └─ GlobalExceptionHandler.java
│ ├─ service/
│ │ └─ JournalService.java
│ ├─ repository/
│ │ └─ JournalRepository.java
│ ├─ domain/
│ │ └─ Journal.java
│ ├─ dto/
│ │ ├─ CreateJournalRequest.java
│ │ ├─ UpdateJournalRequest.java
│ │ ├─ SearchJournalRequest.java
│ │ ├─ JournalResponse.java
│ │ ├─ PageResponse.java
│ │ └─ ErrorResponse.java
│ └─ mapper/
│ └─ JournalMapper.java
└─ src/main/resources/
└─ application.yml
```

## Environment Configurations

`.env` file:

```.dotenv
#-------------------------------------------
# TripJournal Configuration
#-------------------------------------------
MONGO_URI=mongodb://localhost:27017/tripjournal
JWT_SECRET=your_jwt_secret_from_TripPass
```
### Run with Docker

```yaml
docker build -t tripjournal .
docker run -p 9092:9092 tripjournal

```
Service is  available at:
> - Localhost → http://localhost:9092/tripjournal
> - Dockerized → https://tripwise:9092/tripjournal



## 🌐 API Endpoints

| Method   | Endpoint           | Auth Required | Description                          |
|----------|--------------------|---------------|--------------------------------------|
| `GET`    | `/journals`        | ✅ Yes         | Fetch all journals for the user      |
| `POST`   | `/journals`        | ✅ Yes         | Create a new travel journal entry    |
| `GET`    | `/journals/{id}`   | ✅ Yes         | Retrieve a single journal by ID      |
| `PUT`    | `/journals/{id}`   | ✅ Yes         | Update an existing journal entry     |
| `DELETE` | `/journals/{id}`   | ✅ Yes         | Delete a journal entry               |
| `GET`    | `/journals/search` | ✅ Yes         | Search journals by city/trip keyword |



## Integration Map (Travel-Themed Microservices - Tripwise)

1.  TripHub  → Gateway (entry point)
2. TripPass  → Auth (JWT/OAuth2)
3. TripProfile  → User profiles & preferences 
4. TripPlanner  → Itinerary, packing, weather snapshots 
5. TripJournal  → Journals, memories, activities 
6. TripWeather  → Weather forecasts for trips 
7. TripMediaVault  → Photos, avatars, attachments
