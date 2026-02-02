## Overview
The RoundUpApp is a tiny app built to showcase some of the current Kotlin and Android architecture design patterns. The app helps users automatically save small amounts of money by "rounding up" their weekly transactions to the nearest pound and transferring the difference into a savings goal.

## Architecture
- MVI single state object unidirectional data flow architecture
- Jetpack Compose declarative UI
- Retrofit for API calls
- Room database to persist information locally
- Hilt dependency injection
- Compose UI and integration tests
- Unit testing
- Coroutines and flows
- Error handling
- Material Design 3 UI

## How To Use
### Prerequisites
*   Android Studio Otter 3 Feature Drop | 2025.2.3
*   Android SDK 36
*   An Android device or emulator running API level 24 or higher

>[!Warning]
>You will need to create and add a `secrets.properties` file containing `API_KEY="Bearer <your key>"` at the root folder. You can generate a key from the your Starling Developer account after you [Create a Sandbox customer](https://developer.starlingbank.com/sandbox/select).

### Run the app
To use the app simply clone the repository and run the app on your emulator or connected device.

## Features
This is a single activity single screen application, with the showing the following:
*   **View Account Balance:** See your current account balance at a glance. This will update once a transaction (round up) is made
*   **Transaction History:** A list of all the transactions made within the last 7 days.
*   **Savings Goal Management:** A dialog which allows you to create exactly one saving goal with a name and target amount and delete it when no longer useful.
*   **Round Up Transfers:** The app handles the transfer of your round-up savings to the created goal.  Once the transfer is completed the option will not be available until the goal is deleted
*   **Clean, Modern UI:** A user-friendly interface built with Jetpack Compose.

## Disclaimer
While the purpose of the app hopefully illustrates a clear understanding of core concepts concerning Android development, there is certainly room for improvement on various areas of the app, of which I am aware but ended up not tackling or partially tackling in this submission due to time constraints. Some of them are:
- Error state management especially in the ViewModel
- Loading state handling and indication
- Accounting for already made round-ups
- UI and theming

>[!info]
>Regarding the last point, for practicality after a goal is created, the user has the option to do a rounding up transfer once, after which the button disappears. Once the goal is deleted and another one is recreated, the transfer option appears again, regardless of the fact that the last 7 days round ups were already transferred.

I hope you enjoy the app. Bank responsibly :) 
