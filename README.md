⚡ ZapGo — EV Charging & Route Planner

A full-stack web application designed to help EV users plan long-distance trips with intelligent routing, automatic charging stop detection, and real-time statistics — all wrapped in a clean, modern UI.

🚀 Features

🔐 Authentication

Google OAuth for users

Custom admin login

Protected routes using React Router

🗺️ Smart EV Route Planning

Distance, duration & energy usage estimation

Automatic charging stop suggestions

Interactive map interface

🔋 Charging Station Management (Admin)

Add / edit / delete charging stations

Send notifications to stations (simulated)

📊 Booking Insights

View all bookings in real time

Total distance, total travel time

Daily booking analytics using Chart.js

☁️ Cloud Integration

Firebase Firestore (real-time)

Firebase Authentication

User booking sync

Admin analytics auto-updated

🛠️ Tech Stack
Frontend

React + Vite

React Router

Chart.js

Custom CSS (no UI framework)

Backend / Cloud

Firebase Firestore

Firebase Authentication

Deployment

Vercel (CI/CD enabled)

📁 Project Structure

src/
 ├── assets/            → icons, logos
 ├── components/        → shared components (sidebar, topbar, etc.)
 ├── context/           → AuthContext for login state
 ├── firebase/          → firebase.js config
 ├── hooks/             → custom hooks
 ├── pages/             → all user/admin pages
 ├── styles.css         → global styles
 ├── user.css           → user dashboard styles
 ├── admin.css          → admin dashboard styles
 └── main.jsx

