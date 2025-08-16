# MiniCRM

MiniCRM is a lightweight Customer Relationship Management (CRM) Android app designed for small businesses and individual users to efficiently manage customers and their orders. It combines local persistence with cloud synchronization to ensure data is safe and accessible in real-time.

## Features

- **Customer Management**: Add, edit, view, and delete customer information including name, email, phone, and company.  
- **Order Management**: Track customer orders with title, amount, and date.  
- **Local Database**: Uses Room for offline storage of customers and orders.  
- **Cloud Sync**: Real-time synchronization with Firebase Firestore ensures all data is up-to-date across devices.  
- **RecyclerView UI**: Modern, responsive lists for both customers and orders.  
- **Simple and Lightweight**: Minimal dependencies, easy to maintain and scale.  

## Tech Stack

- **Kotlin** – Primary programming language  
- **Room** – Local database for offline data storage  
- **Firebase Firestore** – Real-time cloud database synchronization  
- **Coroutines** – Asynchronous operations for smooth UI  
- **Material Design** – Modern UI components  

## Usage

- Launch the app to view the list of customers.  
- Tap the **+ button** to add a new customer.  
- Select a customer to view details and their orders.  
- Orders can be added, edited, or deleted.  
- All changes are automatically synced with Firestore in real-time.  
- A demo video is available in the `demo` folder. Users can download it to see the app in action.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Chinmaynawghare/MiniCRM.git
