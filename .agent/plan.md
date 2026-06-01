# Project Plan

Textcalc: A text calculator app. It features a blank text page where users can enter text and numerical values. The app parses each line, extracts numerical values, and displays the total sum of all numbers at the bottom. It should follow Material Design 3, have a vibrant color scheme, an adaptive icon, and support edge-to-edge display. Users can create multiple documents, lines starting with '#' are ignored for calculation, and users can save/name documents with specific titles.

## Project Brief

# Project Brief: Textcalc

Textcalc is a productivity-focused Android application that transforms a simple notepad into a powerful calculation tool. By treating text lines as data sources, it allows users to mix descriptive notes with numerical values, automatically providing a live total of all figures entered.

## Features

1.  **Intelligent Text Parsing**: A real-time editor that extracts numerical values from every line while ignoring "comment" lines starting with `#`.
2.  **Live Calculation Footer**: A persistent bottom bar that displays the aggregate sum of all extracted numbers within the current document.
3.  **Document Management System**: A workspace allowing users to create, name, and switch between multiple independent documents for different projects or budgets.
4.  **Explicit Document Saving**: A dedicated flow for naming new documents and renaming existing ones to keep files organized.
5.  **Material Design 3 Aesthetic**: A vibrant, energetic UI utilizing Material 3 components, dynamic color theming, and full edge-to-edge display support.

## High-Level Tech Stack

*   **Kotlin**: The primary language for all application logic.
*   **Jetpack Compose**: For building a modern, reactive, and declarative user interface.
*   **Room Persistence Library**: Required for managing and storing multiple user documents and their contents.
*   **KSP (Kotlin Symbol Processing)**: Used for efficient code generation for the Room database.
*   **Kotlin Coroutines & Flow**: To handle background text parsing and reactive data updates from the database to the UI.
*   **Material 3**: To implement the latest design system, including adaptive icons and vibrant color schemes.

## Implementation Steps

### Task_1_Core_Logic_ViewModel: Implement parsing logic and ViewModel.
- **Status:** COMPLETED

### Task_2_Main_UI_Compose: Build main Compose UI with edge-to-edge support.
- **Status:** COMPLETED

### Task_3_Theme_Icon_Assets: Implement M3 vibrant theme and adaptive icon.
- **Status:** COMPLETED

### Task_4_Run_Verify: Initial verification of base app features.
- **Status:** COMPLETED

### Task_5_Room_Persistence_MultiDoc: Implement Room and update parser for '#' comments.
- **Status:** COMPLETED

### Task_6_Doc_UI_Final_Verify: Implement Navigation Drawer/Document selection.
- **Status:** COMPLETED

### Task_7.1_NamingDialog_UI: Implement a reusable NamingDialog composable with TextField and focus requester.
- **Status:** COMPLETED
- **Updates:** Implemented reusable NamingDialog composable in `ui/components/NamingDialog.kt`. The dialog includes:
- **Acceptance Criteria:**
  - Dialog shows TextField
  - Focus is requested on appear
  - Validation for empty names

### Task_7.2_Room_DAO_Update: Update DocumentDao with a method to update document titles by ID.
- **Status:** COMPLETED
- **Updates:** Updated `DocumentDao.kt` to include `updateTitle(id, title)` method. This allows updating only the title of a document without affecting its content or other fields. The method is a suspend function as required.
- **Acceptance Criteria:**
  - DAO has updateTitle(id, title) method

### Task_7.3_ViewModel_Integration: Update ViewModel to handle createNewDocument(name) and renameDocument(id, name).
- **Status:** COMPLETED
- **Updates:** Updated CalculatorViewModel to handle createNewDocument(name: String) and renameDocument(id: Int, newName: String).
- **Acceptance Criteria:**
  - ViewModel interacts with DAO to save names

### Task_7.4_UI_Integration: Add Rename action to the UI and update the New Document flow to prompt for a name.
- **Status:** COMPLETED
- **Updates:** Integrated NamingDialog into MainActivity. Updated the New Document flow (from TopAppBar and Navigation Drawer) to prompt for a name. Added a Rename action (Edit icon) to the TopAppBar that allows users to rename the current document. Ensured proper state management for dialog visibility and pre-population of the current title. Verified build success and UI reactivity.
- **Acceptance Criteria:**
  - Rename option in menu
  - New document FAB triggers naming dialog

### Task_8_Final_Refinement_Verify: Refine UI to prominently display document names and perform final end-to-end verification.
- **Status:** IN_PROGRESS
- **Updates:** Fixed a race condition in CalculatorViewModel initialization that caused new 'Untitled' documents to be created on startup instead of loading the last used one. The app now waits for the database query to complete before deciding to create a new document. Verified that lastModified is updated during edits and document selection. Ready for final verification.
- **Acceptance Criteria:**
  - Current doc name in TopAppBar
  - Persistence verified across app restarts

