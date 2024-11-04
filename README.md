
***Med Check Mobile Application
***OPSC7312 POE PART 2

****Youtube link
https://m.youtube.com/watch?v=1-WOMYdCUbY&pp=ygUITWVkY2hlY2s%3D


The following mobile application is being designed using the Android Studio IDE.
The application, which is being identified as "Med Check", it  has as a focal point, the ability of allowing users to log a medicine with the objective of them keeping track of the medical records. Med Check aims to provide a user-friendly interface for users to manage their medication schedules and improve their adherence to prescribed treatments. By centralizing medical records, users can easily monitor their health and well-being.

The following screenshot below is shwoing the Med Check logo:


Main features of the application :

*** Create an account/sign up using google - in order for users to record their medication, they should have an account to maintain their data kept and centralized, which all starts by the creation of an account by entering their personalized characteristics. If user has a google account, they can automatically sign up by the use of a Single Sign In feature. The application  supports Google Sign-In through Firebase Authentication, allowing users to register quickly if they have a Google account.

*** Login - Once the user has their account created, they can then login by entering their credentials which will be authenticated and verified if it exists in the database(meaning user is registered), therefore, they will be redirected to the Dashboard.

*** Dashboard - that is the landing page where user will have options to add medicine, view and explore the application as they desire. 

*** Add medicine - the following layout prompts the user to enter the medicine's information, which is the medicine name, strength and how they would like to schedule their dose (as desired or in a proper scheduling) , if the user selects as needed, they will log whenever they intake it, on the other hand, if they choose to schedule, they will be redirected to schedule dose page.

*** Schedule dose - with the following page, the user will enter the time they desire to take the medicine, what is the quantity and how frequently they wish to take it. Once they have entered, they can click on schedule dose button that will redirect them to my medicine page.

*** My medicine - this page is responsible for displaying the medicine details that were previously entered. 

*** Settings - Users can adjust application settings according to their preferences, including notification settings and account management.

*** Google Maps API - this feature was implemented using google console to extract the google places API. It will allow for users to access maps within the application on which they can be able to search for any nearby pharmacies.

*** Open FDA API - the following feature allows for users to search for the medication details so they can be better informed on the dosages, warnings and possible side effects that they may consist of. 

*** Gamification - the dashboard consists of awards for the user based on how they take their medicine and how often they use the application. 

Language selection
- A user has an option to pick between the 3 languages (English, Afrikaans and Portugues) to interact with the app. The LanguageFragment XML layout defines a simple interface where users can choose their preferred language using a set of radio buttons:

Radio Group (languageRadioGroup): Contains radio buttons for language selection.
English (radioEnglish) - text set by @string/english
Portuguese (radioPortuguese) - text set by @string/portuguese
Afrikaans (radioAfrikaans) - text set by @string/afrikaans

![1000006511](https://github.com/user-attachments/assets/8a1c653d-6ed7-4ef1-bf08-297c43fea1e8)


**Biometrics (fingerprint) system**
As stated by Android Developers,"One method of protecting sensitive information or premium content within your app is to request biometric authentication, such as using face recognition or fingerprint recognition. This guide explains how to support biometric login flows in your app."(2024)
This login authentication style helps correctly identify,  allow rightful Med Check users to check their mediciation details. To handle biometric secuirty, a user must tab on the preferencec tab on the navigation tab, they are able to change the password, add facial recognition, privacy and log out option.
![1000006509](https://github.com/user-attachments/assets/52437a4c-1cdf-45dd-868b-55ad1c84c164)

**Real-time notification**
The notification help the Med Check users continue engaging with the application by customizing the notifcation based on the time they want to be alerted. To use this feature, a user must tap on the Preferences tab below on the navigation bar then looks over the second option with the bell icon. 

![1000006509](https://github.com/user-attachments/assets/8a50333a-797b-4d0a-91e1-1f738edb5b4f)


*** Technologies Used ***
Android Studio: The primary IDE for developing the application.
Firebase: For user authentication and database management.
Google Maps API: To implement location services for pharmacy searches.
Open FDA API: To provide access to medication information.

*** Design Considerations ***
- The application was designed with a focal point in user experience. It's goal is to provide an intuitive and user friendly interface which is readable, clear and straightforward to accommodate different users from different age groups, taking into consideration that elder people tend to have a higher medical intake, hence , having them as the highest target group for the application. 

*** GitHub and GitHub actions ***
- GitHub was used as the version control to facilitate the cooperation and code change tracking throughout the development of the Med Check application, considering that the application was being designed by different team members. Continuous Integration and Deployment (CI/CD) using GitHub Actions was there to automate the deployment and testing procedures, by guaranteeing that, prior to being merged into the main branch, code changes are consistently built and tested.

***how data is saved and displayed in the MedCheck app, specifically for the "Add Medicine" and "My Medicine" functionalities.***

***Features Overview***
1. Add Medicine
The Add Medicine feature allows users to input medication details, such as name, strength, and frequency.
These details are saved to two storage systems:
Firebase Realtime Database: Stores medication data for broader use across multiple devices.
SQLite: Stores medication data locally on the device, allowing offline access.

3. My Medicine
The My Medicine feature allows users to view the list of saved medications.
It displays data from both Firebase and SQLite:
Firebase Data: Shows medication data that is synced with the server.
SQLite Data: Shows medication data saved locally on the device, useful for offline access.
![1000006510](https://github.com/user-attachments/assets/9770ad23-1031-4623-a8e7-037186ae4d38)

**Data Storage Workflow**
Firebase Database
Purpose: Cloud-based storage for storing medication data. It enables syncing across multiple devices and accessing data even after app reinstallation.
Structure: Medication data is stored under a medicines node in Firebase, with each medication entry containing details such as name, strength, and frequency.
**Data Retrieval Workflow**
My Medicine (Display Data)
From Firebase: Data is fetched in real-time from the Firebase database and displayed in a ListView. This allows users to see medications that are synced across devices.
From SQLite: Data is retrieved from SQLite and displayed in a TextView, providing a list of medications that can be accessed offline.

The following images are showing the medicine details user input being saved and displayed. Add Medicine requuires user input of the medication while My Medicine allows the user to read about their saved mediciation after tapping on the view button. 


Google Developers, 2024. Authenticate with Biometric Login on Android. Available at: https://developer.android.com/identity/sign-in/biometric-auth [Accessed 4 November 2024].

******* Azania Ncube ST10036066
******* Mayra Selemane ST10036905
******* Zacarias Antero ST10068763

