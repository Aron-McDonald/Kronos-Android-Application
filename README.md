# Kronos Time Tracking App

Kronos is a sophisticated time-keeping Android application designed to help users track and manage their timesheet entries effectively. It allows users to set daily goals and analyze time spent on different categories, enhancing productivity and time management.

## Features

- User Authentication: Secure login functionality using username and password.
- Category Management: Allows users to create and manage categories for organizing timesheet entries.
- Timesheet Entry: Enables users to log timesheet details such as date, start and end time, description, and associated category.
- Photo Attachment: Offers the option to attach photos to timesheet entries for better record-keeping.
- Daily Goals: Users can set minimum and maximum daily work hour goals.
- Entry Viewing: Provides a view of all timesheet entries within a selected period, including access to attached photos.
- Time Analysis: Analyzes and displays the total hours spent per category within a user-selected period.
- Report: Generates reports based on timers created. Features a graph filterable by date, showing daily hours worked and comparing them to set goals.
- Feedback: Allows users to rate the app using a 1 to 5 stars system and leave comments for further improvement.
- Task To-Do List: Users can add tasks with descriptions to a to-do list and delete tasks as needed.

## Key Features to Note for Lecturers:
-Task To-Do List: Allows users to manage their tasks efficiently within the app.
- Feedback Feature: Engages users in app development by providing feedback tools.

### 1. Task To-Do List 
Functionality and User Interaction:

-The Task To-Do List feature is a straightforward tool within the app that enables users, such as lecturers and students, to manage their tasks. 
-Users can create new tasks by entering a description. Once a task is completed or no longer needed, users have the option to delete it from the list. 
-This streamlined feature focuses on simplicity, allowing users to maintain a clear and concise list of tasks without the complexity of managing due dates or task edits.

Technical Components:

-Each task creation and deletion is directly handled by interactions with a Firebase database. When a user adds a task, the task's description is stored in Firebase, and similarly, when a task is deleted, its record is removed from the database. 
-This setup ensures that the data regarding user tasks is always current and accurately reflects the user's to-do list. Firebase, known for its real-time data syncing and ease of use, provides a robust backend solution for managing the dynamic content of the Task To-Do List.

By focusing on these functionalities, the Task To-Do List feature remains user-friendly and highly functional, leveraging Firebase to ensure data integrity and real-time updates which are essential for maintaining an effective task management tool within the app.


### 2. Feedback Feature 

-Functionality and User Interaction:

-The Feedback Feature enables users to actively participate in the development and improvement of the app. 
-Users can rate the application using a 1 to 5 stars system, where 1 star indicates poor satisfaction and 5 stars indicate excellent satisfaction. 
-Additionally, a text box is provided for users to leave detailed comments or suggestions. 
-This dual approach to gathering feedback is essential for obtaining both quantitative and qualitative insights into user experiences and expectations.

Technical Components:

-All feedback data, including star ratings and textual comments, is stored in Firebase.
-This choice of database allows for real-time data updates and storage, ensuring that feedback is promptly recorded and accessible for analysis. 
-The Firebase database schema is designed to store entries containing user IDs, ratings, comments, and timestamps. 
-This organization supports efficient data retrieval for ongoing analysis and helps developers identify trends, common issues, and areas for enhancement based on user inputs.
-Using Firebase for the Feedback Feature enhances the app's ability to adapt and evolve according to user feedback, ensuring a responsive and user-centered development process.


## Getting Started

To set up the development environment and run the Kronos Time Tracking App locally, follow these steps:

### Prerequisites

- Android Studio 2023.3.1 or later
- Kotlin 1.9.2 or later
- Android SDK 34 or later

### Installation

1. Download the source code zip file from the submission in VC Learn.

2. Extract the contents of the zip file to a directory of your choice.

3. Open Android Studio and select "Open an existing Android Studio project".

4. Navigate to the directory where you extracted the source code and select the project folder.

5. Wait for Android Studio to build the project and resolve any dependencies.

6. Once the build process is complete, you can run the app on an emulator or a physical device.

   - To run on an emulator:
     - Open the AVD Manager in Android Studio (Tools -> AVD Manager)
     - Create a new virtual device or select an existing one
     - Click the "Run" button in Android Studio and select the virtual device
     
   - To run on a physical device:
     - Connect your Android device to your computer via USB
     - Enable USB debugging on your device (Settings -> Developer options -> USB debugging)
     - Click the "Run" button in Android Studio and select your device

7. The app should now be installed and launched on your selected device or emulator.

### Configuration

- Make sure you have a stable internet connection, as the app requires access to Firebase for authentication and data storage.
- If you encounter any issues related to Firebase, ensure that you have properly set up your Firebase project and configured the necessary API keys and permissions.

That's it! You should now have the Kronos Time Tracking App up and running on your local development environment. If you encounter any issues or have further questions, please refer to the Troubleshooting section or reach out to our support team.


## Architecture
Kronos follows the MVVM (Model-View-ViewModel) architecture pattern and utilizes Jetpack Compose for UI components and Jetpack Navigation for screen navigation.

## Main Components
- MainActivity: Entry point of the application.
- CategoryManager: Manages the lifecycle and storage of categories.
- ProjectManager: Handles project-related functionalities.
- TimerManager: Responsible for tracking and managing timers.
- LoginScreen: Manages user authentication.
- SignUpScreen: Interface for new user registration.
- HomePageScreen: Dashboard for navigating through the app's features.
- ProjectsScreen: Displays and manages project entries.
- TimersScreen: Manages timer entries.
- CategoriesScreen: Allows users to create and manage their categories.
- FeedbackScreen: Facilitates user feedback through ratings and comments.

## Dependencies
The application uses the following dependencies:
- Jetpack Compose - Modern toolkit for building native Android UI.
- Jetpack Navigation - Framework for navigating between different screens in the app.
- Material Design Components - UI components following the Material Design guidelines.
- Firebase - Suite for app development needs including database, authentication, and crash reporting.
- JUnit - Framework for writing and running repeatable tests.
- Espresso - Framework for UI testing.

## Contributing
We encourage community contributions to make Kronos even better. Here's how you can contribute:
1. Fork the repository.
2. Create a new branch for your features or fixes.
3. Commit your changes and push to your branch.
4. Submit a pull request to our main branch.

## Developers
- ST10091248 Tevessan
- ST10238400 Brandon
- ST10291606 Aron
- ST10182788 Shavir
- ST10091023 Rylan


### We would like to acknowledge the following resources for their contributions and inspiration:

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose): For providing the modern toolkit that helps build the native UI for our Android app.
- [Jetpack Navigation Documentation](https://developer.android.com/guide/navigation): For their navigation framework, which supports the app’s complex navigation needs.
- [Material Design Components](https://material.io/components?platform=android): For their comprehensive guidelines and components that enhance the UI/UX of our app.
- [Firebase](https://firebase.google.com/): For offering a robust backend platform that enhances our app’s functionality in authentication, data storage, and analytics.
- [JUnit](https://junit.org/junit4/): For their testing framework, which supports our application development with necessary testing capabilities.
- [Espresso](https://developer.android.com/training/testing/espresso): For providing the framework used in UI testing, ensuring our application's reliability and user-friendliness.
- [Android Developers Community](https://developer.android.com/community): For the support and vast resources available that assist in Android development.
- [Stack Overflow](https://stackoverflow.com): For the invaluable community-driven insights and solutions that guide our development process.

Thank you for using Kronos Time Tracking App!
