# McGill VSB Course Notifier

A Java application that monitors McGill's Visual Schedule Builder (VSB) for course availability and sends SMS notifications when seats become available.

## 📋 Overview

This application helps McGill students get notified when spots open up in full courses. It periodically checks the VSB system for course availability and sends immediate SMS notifications using the Twilio API when seats become available in your selected courses.

## ✨ Features

- **Course Monitoring**: Automatically checks McGill's VSB for seat availability in specified courses
- **Real-time Notifications**: Sends SMS alerts when a seat becomes available
- **Customizable Checks**: Set your own frequency for checking course availability
- **Easy Configuration**: Simple setup for course selection and notification preferences

## 🛠️ Technologies

- **Java**: Core application logic
- **Twilio API**: SMS notification service
- **McGill VSB**: Course data source (XML parsing)
- **HTTP Client**: For making API requests to VSB and Twilio

## 📦 Prerequisites

- Java JDK 11 or higher
- Twilio account (free trial available)
- Maven or Gradle for dependency management

## ⚙️ Installation

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/mcgill-vsb-notifier.git
   cd mcgill-vsb-notifier
   ```

2. Install dependencies
   ```bash
   mvn install
   ```

3. Configure your Twilio credentials
   - Copy `config.properties.example` to `config.properties`
   - Add your Twilio Account SID, Auth Token, and phone number

## 🚀 Usage

1. Configure the courses you want to monitor in `courses.json`:
   ```json
   [
     {
       "courseCode": "COMP 202",
       "crn": "12345",
       "term": "Fall 2025"
     },
     {
       "courseCode": "MATH 133",
       "crn": "67890",
       "term": "Fall 2025"
     }
   ]
   ```

2. Set your phone number in `config.properties`

3. Run the application:
   ```bash
   java -jar vsb-notifier.jar
   ```

4. The application will now:
   - Check VSB for course availability at your specified interval
   - Send you an SMS when a seat becomes available
   - Log all activities for your reference

## 📂 Project Structure

```
mcgill-vsb-notifier/
├── src/
│   └── main/
│       └── java/
│           ├── com/
│               └── vsbnotifier/
│                   ├── Main.java                  # Application entry point
│                   ├── config/
│                   │   └── ConfigLoader.java      # Loads configuration
│                   ├── service/
│                   │   ├── VSBService.java        # Handles VSB API calls
│                   │   └── NotificationService.java # Handles Twilio API calls
│                   ├── model/
│                   │   ├── Course.java            # Course data model
│                   │   └── SeatAvailability.java  # Availability data model
│                   └── util/
│                       └── XMLParser.java         # Parses VSB XML responses
├── resources/
│   ├── config.properties.example                  # Example configuration
│   └── log4j2.xml                                 # Logging configuration
├── pom.xml                                        # Maven dependencies
└── README.md                                      # Project documentation
```
## Interactions & Dependencies
```
+----------------+      +-------------------+      +----------------+
| McGillNotifier |----->| McGillCourseChecker |----->| VSB Website API |
| (Main Class)   |      | (Data Retrieval)   |      | (External)     |
+----------------+      +-------------------+      +----------------+
        |                        |
        v                        v
+----------------+      +-------------------+
| Config         |      | CourseInfo        |
| (Optional)     |      | (Data Model)      |
+----------------+      +-------------------+
        |
        v
+----------------+      +-------------------+
| TwilioNotifier |----->| Twilio API        |
| (Notifications)|      | (External)        |
+----------------+      +-------------------+
```
## Dependency Flow

1- McGillNotifier depends on:
   - McGillCourseChecker for course data
   - TwilioNotifier for sending notifications
   - Config for settings (optional)

2- McGillCourseChecker depends on:
   - VSB Website API (external)
   - Inner classes (CourseInfo, SectionInfo) as data models


3- TwilioNotifier depends on:
   - Twilio API (external)
   - Notification settings from Config or main class



## Data Flow
1- McGillNotifier gets configuration (term, course, CRN)
2- McGillCourseChecker retrieves course data from VSB
3- McGillCourseChecker parses and returns structured data
4- McGillNotifier checks availability against criteria
5- If seats available, TwilioNotifier sends SMS

## 🧩 Code Examples

### VSB API Call

```java
public class VSBService {
    private static final String VSB_API_URL = "https://vsb.mcgill.ca/vsb/getclassdata.jsp";
    
    public String fetchCourseData(String term, String courseCode) throws IOException {
        String url = VSB_API_URL + "?term=" + term + "&course=" + courseCode;
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "McGill-VSB-Notifier/1.0");
        
        // Read and return response
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        return response.toString();
    }
}
```

### Twilio Notification

```java
public class NotificationService {
    private final String ACCOUNT_SID;
    private final String AUTH_TOKEN;
    private final String TWILIO_PHONE;
    private final String TARGET_PHONE;
    
    public void sendNotification(Course course) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        
        Message message = Message.creator(
            new PhoneNumber(TARGET_PHONE),
            new PhoneNumber(TWILIO_PHONE),
            "A seat is available in " + course.getCode() + " (CRN: " + course.getCrn() + ")!"
        ).create();
        
        System.out.println("Notification sent! SID: " + message.getSid());
    }
}
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

Your Name - [your.email@example.com](mailto:your.email@example.com)

Project Link: [https://github.com/yourusername/mcgill-vsb-notifier](https://github.com/yourusername/mcgill-vsb-notifier)

---

*Made with ❤️ for fellow McGill students*
