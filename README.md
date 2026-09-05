### Project Overview

A JavaFX-based desktop application designed to manage lost and found items within a university campus. The system provides a centralized platform for reporting, matching, claiming, verifying, and recovering lost items.

### Main Users

* **Student/User:** Reports lost/found items, searches items, views potential matches, submits claims, and tracks claim status.
* **Admin:** Manages reports, reviews potential matches, verifies claims, approves/rejects claims, confirms item returns, and views analytics.

### Core Workflow

**Report Lost/Found Item → Automatic Matching → Potential Match → Claim → Admin Verification → Approve/Reject → Item Return**

### Matching System

The system compares attributes such as:
* Category
* Location
* Brand
* Color
* Date
* Description

A weighted algorithm calculates a **Match Score** to identify potential matches.

Example:

**Category 20% + Location 20% + Brand 15% + Color 10% + Date 10% + Description 25%**

### Search & Reports

Users can search/filter lost and found items by category, location, date, etc.

Admin can view analytical reports such as:

* Most frequently lost item categories
* Common lost locations
* Total matched/recovered items
* Recovery rate

### Main Database Entities

`User | LostItem | FoundItem | Match | Claim | Notification`

SQLite will provide persistent storage with appropriate primary keys, foreign keys, relationships, and constraints.

### Technology Stack

**Java + JavaFX + Maven + SQLite + JUnit**

### Key Value of the Project

The project is more than a CRUD application because it contains meaningful business logic involving **automatic matching, multi-step claim verification, state management, notifications, and recovery tracking**.
