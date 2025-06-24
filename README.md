# 📚 McGill VSB Course Notifier

A personal Java tool to monitor McGill's Visual Schedule Builder (VSB) for open seats in specific courses and send yourself SMS notifications when seats become available.

## 📋 Overview

This project uses Selenium to automate a browser, check course availability on McGill's VSB, and notifies you via SMS using Twilio when a seat opens up in a course you want.

## ✨ Features

* Automated course checking using Selenium 😱
* SMS alerts via Twilio when a seat is available 🤩
* Easy to configure for any course and term 🤤

## 📦 Requirements

* Java 11 or higher (JDK)
* Chrome browser installed on your computer
* Twilio account and credentials
* Add all dependencies in the pom.xml file

## ⚙️ Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/ParseDotEXE/course-notifier-mcgill.git
   cd course-notifier-mcgill
   ```

2. **Install dependencies**
   * Add all dependencies in the pom.xml to your project if not automatically added

3. **Configure Twilio**
   * Add your Twilio credentials to a .env file at the root (dont forget to add .env to .gitignore if planning to commit to a github repo :) ):
   ```
   TWILIO_ACCOUNT_SID=your_sid
   TWILIO_AUTH_TOKEN=your_token
   TWILIO_FROM_PHONE=+1xxxxxxxxxx
   ```

4. **Edit the main class**
   * Set the course code and term, exactly like how McGill write them for example "Fall 2025" "MATH 240", you want to monitor in `McGillNotifier.java`.

## 🚀 Usage

Run the main class:
```bash (with maven but you could use classpath approach too)
mvn clean compile
mvn exec:java -Dexec.mainClass="com.vsbnotifier.main.McGillNotifier"
```

The program will:
* Open VSB in a browser
* Search for your specified course and term  
* Print course and section info to the console
* Periodically check VSB (every 45 seconds) to see if a seat becomes available
* Send an SMS once a seat is available

## 📝 Note

This is a personal project for private use only.

Project Link: https://github.com/ParseDotEXE/course-notifier-mcgill.git

---

*Made with ❤️ for McGill students*
