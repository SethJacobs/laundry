# Laundry Scheduler

A beautiful, full-stack laundry scheduling application for families. Built with Java Spring Boot backend and React frontend, designed to run on a Raspberry Pi.

## Features

- 🔐 **User Authentication**: Secure login and signup for family members
- 📅 **Calendar View**: Interactive calendar showing all booked time slots
- ⏰ **Time Slot Booking**: Easy booking interface for laundry time slots
- 🏠 **Home Assistant Integration**: Real-time washing machine status display
  - Shows if washer is running, time remaining, and current status
  - Prevents bookings when machine is in use
  - Beautiful animated status card with live updates
- 📧 **Email Notifications**: 
  - Weekly schedule emails sent every Monday at 8 AM
  - Daily reminders sent at 7 AM for bookings on that day
- 👮 **Admin Panel**: Block/suspend users who break the rules
- 🎨 **Modern UI**: Sleek, responsive design with Tailwind CSS
- 🐳 **Docker Ready**: Easy deployment with Docker Compose

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring Security with JWT
- PostgreSQL
- Spring Mail for email notifications

### Frontend
- React 18
- Vite
- React Big Calendar
- Tailwind CSS
- Axios

## Prerequisites

- Docker and Docker Compose installed
- For email notifications: Gmail account with App Password (or other SMTP server)

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd laundry
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your settings
   ```

3. **Set up email (optional but recommended)**
   - For Gmail: Create an App Password at https://myaccount.google.com/apppasswords
   - Update `MAIL_USERNAME` and `MAIL_PASSWORD` in `.env`

4. **Set up Home Assistant integration (optional)**
   - Get a Long-Lived Access Token from Home Assistant
   - Update `HA_BASE_URL`, `HA_TOKEN`, and entity names in `.env`
   - Set `HA_ENABLED=true` to activate

5. **Build and start services**
   ```bash
   docker-compose up -d
   ```

6. **Access the application**
   - Frontend: http://localhost
   - Backend API: http://localhost:8080/api

7. **Create your first admin user**
   - Sign up through the web interface
   - Then manually set `is_admin = true` in the database:
     ```bash
     docker exec -it laundry-postgres psql -U laundry_user -d laundry_db
     UPDATE users SET is_admin = true WHERE username = 'your_username';
     ```

## Development

### Backend Development

```bash
cd backend
# Make sure you have Maven installed or use the Maven wrapper
./mvnw spring-boot:run
```

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## Deployment on Raspberry Pi

1. **SSH into your Raspberry Pi**
   ```bash
   ssh pi@your-raspberry-pi-ip
   ```

2. **Install Docker and Docker Compose**
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker pi
   sudo apt-get install docker-compose-plugin
   ```

3. **Clone and configure**
   ```bash
   git clone <repository-url>
   cd laundry
   cp .env.example .env
   # Edit .env with your settings
   ```

4. **Build and start**
   ```bash
   docker-compose up -d --build
   ```

5. **Access from your network**
   - The app will be available at `http://your-raspberry-pi-ip`

## Home Assistant Integration

The application can integrate with Home Assistant to display real-time status for both the washer and dryer.

### Setup

1. **Create a Long-Lived Access Token in Home Assistant:**
   - Go to your Home Assistant profile
   - Scroll down to "Long-Lived Access Tokens"
   - Create a new token and copy it

2. **Configure in `.env`:**
   ```bash
   HA_ENABLED=true
   HA_BASE_URL=http://homeassistant.home:8123
   HA_TOKEN=your-long-lived-access-token
   
   # Washer entities (defaults are pre-configured for common LG/Samsung models)
   # HA_WASHER_RUNNING=binary_sensor.zt300866n_laundry_remote_status
   # HA_WASHER_TIME_REMAINING=sensor.zt300866n_laundry_time_remaining
   # HA_WASHER_STATUS=sensor.zt300866n_laundry_machine_state
   
   # Dryer entities (defaults are pre-configured for common LG/Samsung models)
   # HA_DRYER_RUNNING=binary_sensor.av931535g_laundry_remote_status
   # HA_DRYER_TIME_REMAINING=sensor.av931535g_laundry_time_remaining
   # HA_DRYER_STATUS=sensor.av931535g_laundry_machine_state
   ```

3. **Required Home Assistant Entities:**

   **Washer (defaults configured):**
   - `binary_sensor.zt300866n_laundry_remote_status` - Binary sensor indicating if washer is running (on = running)
   - `sensor.zt300866n_laundry_time_remaining` - Sensor with time remaining in minutes (decimal value)
   - `sensor.zt300866n_laundry_machine_state` - Sensor with machine state (Run, Idle, etc.)
   - `sensor.zt300866n_laundry_sub_cycle` - (Optional) Sub-cycle status (Fill, Wash, Rinse, etc.)
   - `binary_sensor.zt300866n_laundry_end_of_cycle` - (Optional) End of cycle indicator

   **Dryer (defaults configured):**
   - `binary_sensor.av931535g_laundry_remote_status` - Binary sensor indicating if dryer is running (on = running)
   - `sensor.av931535g_laundry_time_remaining` - Sensor with time remaining in minutes (decimal value)
   - `sensor.av931535g_laundry_machine_state` - Sensor with machine state (Run, Idle, etc.)
   - `sensor.av931535g_laundry_sub_cycle` - (Optional) Sub-cycle status (Drying, etc.)
   - `binary_sensor.av931535g_laundry_end_of_cycle` - (Optional) End of cycle indicator

   **Note:** If your entity names are different, you can override them in `.env` using the `HA_*` environment variables.

### Features

- **Real-time Status Display**: Beautiful animated cards showing both washer and dryer status
- **Time Remaining**: Shows countdown when machines are running
- **Smart Booking Prevention**: Automatically prevents bookings when either machine is in use
- **Cached Updates**: Status is cached for 30 seconds to reduce API calls
- **Visual Distinction**: Washer uses blue theme, dryer uses orange theme

## Email Configuration

The application sends two types of emails:

1. **Weekly Schedule**: Sent every Monday at 8 AM to all users
2. **Daily Reminders**: Sent every day at 7 AM to users with bookings that day

To configure email:
- Update `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD` in `.env`
- For Gmail, you need to use an App Password, not your regular password

## Admin Features

As an admin, you can:
- View all users
- Block users temporarily (with expiration date) or permanently
- Unblock users
- See blocked users' status and reasons

## API Endpoints

### Authentication
- `POST /api/auth/signin` - Login
- `POST /api/auth/signup` - Register

### Bookings
- `GET /api/bookings` - Get all bookings (with optional date range)
- `GET /api/bookings/my-bookings` - Get current user's bookings
- `POST /api/bookings` - Create a booking
- `DELETE /api/bookings/{id}` - Delete a booking

### Machines
- `GET /api/machines/washer` - Get washing machine status from Home Assistant
- `GET /api/machines/dryer` - Get dryer status from Home Assistant

### Admin
- `GET /api/admin/users` - Get all users (admin only)
- `POST /api/admin/block-user` - Block a user (admin only)
- `POST /api/admin/unblock-user/{userId}` - Unblock a user (admin only)

## Troubleshooting

### Database connection issues
- Make sure PostgreSQL container is healthy: `docker ps`
- Check logs: `docker logs laundry-postgres`

### Email not sending
- Verify SMTP credentials in `.env`
- Check backend logs: `docker logs laundry-backend`
- For Gmail, ensure you're using an App Password

### Frontend not loading
- Check if backend is running: `docker logs laundry-backend`
- Verify nginx configuration
- Check frontend logs: `docker logs laundry-frontend`

## License

MIT

