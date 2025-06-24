# 📚 McGill VSB Course Notifier

A personal Java tool to monitor McGill's Visual Schedule Builder (VSB) for open seats in specific courses and send yourself SMS notifications when seats become available.

## 📋 Overview

This project uses Selenium to automate a browser, check course availability on McGill's VSB, and notifies you via SMS using Twilio when a seat opens up in a course you care about.

## ✨ Features

* Automated course checking using Selenium (real browser automation)
* SMS alerts via Twilio when a seat is available  
* Easy to configure for any course and term

## 📦 Requirements

* Java 11 or higher
* Chrome browser and ChromeDriver
* Selenium Java library
* Twilio account and credentials

## ⚙️ Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ParseDotEXE/course-notifier-mcgill.git
   cd course-notifier-mcgill
   ```

2. **Install dependencies**
   * Add Selenium and WebDriverManager to your project.
   * Download and set up ChromeDriver, or use WebDriverManager.

3. **Configure Twilio**
   * Add your Twilio credentials to a .env file:
   ```
   TWILIO_ACCOUNT_SID=your_sid
   TWILIO_AUTH_TOKEN=your_token
   TWILIO_FROM_PHONE=+1xxxxxxxxxx
   ```

4. **Edit the main class**
   * Set the course code and term you want to monitor in `McGillNotifier.java`.

## 🚀 Usage

Run the main class:
```bash (with maven)
mvn clean compile
mvn exec:java -Dexec.mainClass="com.vsbnotifier.main.McGillNotifier"
```

The program will:
* Open VSB in a browser
* Search for your specified course and term  
* Print course and section info to the console
* Send an SMS once a seat is available

## 📝 Note

This is a personal project for private use only.

Project Link: https://github.com/ParseDotEXE/course-notifier-mcgill.git

---

*Made with ❤️ for fellow McGill students*
