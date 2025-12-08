# Dashboard API - Frontend Integration Guide

This guide provides complete documentation for frontend developers to integrate with the Dashboard API endpoints.

## Table of Contents

1. [Authentication](#authentication)
2. [Base URL](#base-url)
3. [Dashboard Endpoints](#dashboard-endpoints)
4. [Error Handling](#error-handling)
5. [Complete Examples](#complete-examples)

---

## Authentication

All dashboard endpoints require **ROLE_ADMIN** authentication using JWT Bearer tokens.

### Getting the Token

```javascript
// Login as admin first
const login = async (email, password) => {
  const response = await fetch('http://your-api-url/api/v1/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: email,
      password: password
    })
  });
  
  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('accessToken', data.payload.accessToken);
    return data.payload.accessToken;
  } else {
    throw new Error(data.message);
  }
};
```

### Using the Token

```javascript
// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
};
```

---

## Base URL

```
http://your-api-url/api/v1/dashboard
```

Replace `your-api-url` with your actual API server URL (e.g., `http://localhost:8080` or `https://api.example.com`).

---

## Dashboard Endpoints

### 1. Get Dashboard Statistics

**Endpoint:** `GET /api/v1/dashboard/stats`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns statistics for the dashboard including total counts and percentage changes.

#### Request

```javascript
const getDashboardStats = async () => {
  const response = await fetch('http://your-api-url/api/v1/dashboard/stats', {
    method: 'GET',
    headers: getAuthHeaders()
  });
  
  return response.json();
};
```

**No query parameters or request body required.**

#### Response

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "payload": {
    "totalUsers": 1200,
    "totalUsersChange": 12.5,
    "totalPlaces": 500,
    "totalPlacesChange": 8.3,
    "totalImages": 2450,
    "totalImagesChange": 15.2
  },
  "status": "OK",
  "timestamp": "2025-12-08T10:00:00"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `totalUsers` | number | Total number of registered users |
| `totalUsersChange` | number | Percentage change in users (compared to previous month). Positive = growth, Negative = decline |
| `totalPlaces` | number | Total number of places |
| `totalPlacesChange` | number | Percentage change in places (compared to previous month) |
| `totalImages` | number | Total number of uploaded images (main + detail images) |
| `totalImagesChange` | number | Percentage change in images (compared to previous month) |

**Example Usage:**

```javascript
// Fetch dashboard statistics
const stats = await getDashboardStats();

if (stats.success) {
  const data = stats.payload;
  
  // Display statistics
  console.log(`Total Users: ${data.totalUsers} (${data.totalUsersChange > 0 ? '+' : ''}${data.totalUsersChange}%)`);
  console.log(`Total Places: ${data.totalPlaces} (${data.totalPlacesChange > 0 ? '+' : ''}${data.totalPlacesChange}%)`);
  console.log(`Total Images: ${data.totalImages} (${data.totalImagesChange > 0 ? '+' : ''}${data.totalImagesChange}%)`);
  
  // Format for display
  const formattedUsersChange = `${data.totalUsersChange > 0 ? '+' : ''}${data.totalUsersChange.toFixed(1)}%`;
  const formattedPlacesChange = `${data.totalPlacesChange > 0 ? '+' : ''}${data.totalPlacesChange.toFixed(1)}%`;
  const formattedImagesChange = `${data.totalImagesChange > 0 ? '+' : ''}${data.totalImagesChange.toFixed(1)}%`;
}
```

---

### 2. Get Places Overview Chart Data

**Endpoint:** `GET /api/v1/dashboard/places-overview`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns monthly data for places creation chart (last N months).

#### Request

```javascript
const getPlacesOverview = async (months = 6) => {
  const response = await fetch(
    `http://your-api-url/api/v1/dashboard/places-overview?months=${months}`,
    {
      method: 'GET',
      headers: getAuthHeaders()
    }
  );
  
  return response.json();
};
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `months` | integer | No | `6` | Number of months to retrieve data for (1-12) |

**Example Requests:**

```javascript
// Get last 6 months (default)
const chartData = await getPlacesOverview();

// Get last 12 months
const chartData12 = await getPlacesOverview(12);

// Get last 3 months
const chartData3 = await getPlacesOverview(3);
```

#### Response

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Places overview retrieved successfully",
  "payload": [
    {
      "month": "Jan",
      "monthNumber": 1,
      "year": 2025,
      "count": 65
    },
    {
      "month": "Feb",
      "monthNumber": 2,
      "year": 2025,
      "count": 78
    },
    {
      "month": "Mar",
      "monthNumber": 3,
      "year": 2025,
      "count": 90
    },
    {
      "month": "Apr",
      "monthNumber": 4,
      "year": 2025,
      "count": 85
    },
    {
      "month": "May",
      "monthNumber": 5,
      "year": 2025,
      "count": 95
    },
    {
      "month": "Jun",
      "monthNumber": 6,
      "year": 2025,
      "count": 100
    }
  ],
  "status": "OK",
  "timestamp": "2025-12-08T10:00:00"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `month` | string | Month abbreviation (Jan, Feb, Mar, etc.) |
| `monthNumber` | number | Month number (1-12) |
| `year` | number | Year |
| `count` | number | Number of places created in that month |

**Example Usage with Chart.js:**

```javascript
// Fetch places overview data
const overview = await getPlacesOverview(6);

if (overview.success) {
  const data = overview.payload;
  
  // Prepare data for Chart.js
  const labels = data.map(item => `${item.month} ${item.year}`);
  const counts = data.map(item => item.count);
  
  // Create chart
  const ctx = document.getElementById('placesChart').getContext('2d');
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Places Created',
        data: counts,
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true
        }
      }
    }
  });
}
```

**Example Usage with Recharts (React):**

```jsx
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';

const PlacesOverviewChart = () => {
  const [data, setData] = useState([]);
  
  useEffect(() => {
    const fetchData = async () => {
      const response = await getPlacesOverview(6);
      if (response.success) {
        setData(response.payload);
      }
    };
    fetchData();
  }, []);
  
  return (
    <LineChart width={600} height={300} data={data}>
      <CartesianGrid strokeDasharray="3 3" />
      <XAxis dataKey="month" />
      <YAxis />
      <Tooltip />
      <Legend />
      <Line type="monotone" dataKey="count" stroke="#8884d8" name="Places Created" />
    </LineChart>
  );
};
```

---

### 3. Get Recent Activity

**Endpoint:** `GET /api/v1/dashboard/recent-activity`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns list of recent activities/actions performed in the system.

#### Request

```javascript
const getRecentActivity = async (limit = 10, offset = 0) => {
  const response = await fetch(
    `http://your-api-url/api/v1/dashboard/recent-activity?limit=${limit}&offset=${offset}`,
    {
      method: 'GET',
      headers: getAuthHeaders()
    }
  );
  
  return response.json();
};
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | integer | No | `10` | Number of activities to return (1-100) |
| `offset` | integer | No | `0` | Pagination offset |

**Example Requests:**

```javascript
// Get latest 10 activities (default)
const activities = await getRecentActivity();

// Get latest 20 activities
const activities20 = await getRecentActivity(20);

// Get next page (pagination)
const nextPage = await getRecentActivity(10, 10);
```

#### Response

**Success Response (200 OK):**

```json
{
  "success": true,
  "message": "Recent activity retrieved successfully",
  "payload": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "action": "PLACE_CREATED",
      "actionLabel": "New place added",
      "entityType": "PLACE",
      "entityId": "660e8400-e29b-41d4-a716-446655440001",
      "entityName": "Mountain View",
      "userId": "770e8400-e29b-41d4-a716-446655440002",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-08T08:00:00",
      "timeAgo": "2 hours ago"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440003",
      "action": "PLACE_UPDATED",
      "actionLabel": "Place updated",
      "entityType": "PLACE",
      "entityId": "660e8400-e29b-41d4-a716-446655440004",
      "entityName": "Lake Paradise",
      "userId": "770e8400-e29b-41d4-a716-446655440002",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-08T05:00:00",
      "timeAgo": "5 hours ago"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440005",
      "action": "IMAGE_UPLOADED",
      "actionLabel": "Image uploaded",
      "entityType": "IMAGE",
      "entityId": "660e8400-e29b-41d4-a716-446655440006",
      "entityName": "Desert Oasis",
      "userId": "770e8400-e29b-41d4-a716-446655440002",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-07T10:00:00",
      "timeAgo": "1 day ago"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440007",
      "action": "PLACE_DELETED",
      "actionLabel": "Place deleted",
      "entityType": "PLACE",
      "entityId": "660e8400-e29b-41d4-a716-446655440008",
      "entityName": "Old Location",
      "userId": "770e8400-e29b-41d4-a716-446655440002",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-06T15:00:00",
      "timeAgo": "2 days ago"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440009",
      "action": "USER_REGISTERED",
      "actionLabel": "New user registered",
      "entityType": "USER",
      "entityId": "770e8400-e29b-41d4-a716-446655440010",
      "entityName": "newuser@example.com",
      "userId": "770e8400-e29b-41d4-a716-446655440010",
      "userEmail": "newuser@example.com",
      "timestamp": "2025-12-05T12:00:00",
      "timeAgo": "3 days ago"
    }
  ],
  "status": "OK",
  "timestamp": "2025-12-08T10:00:00"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | string (UUID) | Unique activity ID |
| `action` | string | Action type enum (see below) |
| `actionLabel` | string | Human-readable action label |
| `entityType` | string | Type of entity (PLACE, USER, IMAGE) |
| `entityId` | string (UUID) | ID of the affected entity |
| `entityName` | string | Name of the affected entity (place name, user email, etc.) |
| `userId` | string (UUID) | ID of user who performed the action |
| `userEmail` | string | Email of user who performed the action |
| `timestamp` | string (ISO 8601) | Timestamp of when action occurred |
| `timeAgo` | string | Human-readable relative time (e.g., "2 hours ago") |

**Action Types:**

| Action | Label | Description |
|--------|-------|-------------|
| `PLACE_CREATED` | "New place added" | When a new place is created |
| `PLACE_UPDATED` | "Place updated" | When a place is updated |
| `PLACE_DELETED` | "Place deleted" | When a place is deleted |
| `IMAGE_UPLOADED` | "Image uploaded" | When an image is uploaded (main or detail) |
| `IMAGE_DELETED` | "Image deleted" | When an image is deleted |
| `USER_REGISTERED` | "New user registered" | When a new user registers |

**Example Usage (React Component):**

```jsx
import React, { useState, useEffect } from 'react';

const RecentActivityList = () => {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    const fetchActivities = async () => {
      try {
        const response = await getRecentActivity(10);
        if (response.success) {
          setActivities(response.payload);
        }
      } catch (error) {
        console.error('Error fetching activities:', error);
      } finally {
        setLoading(false);
      }
    };
    
    fetchActivities();
  }, []);
  
  const getActionIcon = (action) => {
    switch (action) {
      case 'PLACE_CREATED':
        return '➕';
      case 'PLACE_UPDATED':
        return '✏️';
      case 'PLACE_DELETED':
        return '🗑️';
      case 'IMAGE_UPLOADED':
        return '📷';
      case 'IMAGE_DELETED':
        return '🗑️';
      case 'USER_REGISTERED':
        return '👤';
      default:
        return '📝';
    }
  };
  
  if (loading) {
    return <div>Loading activities...</div>;
  }
  
  return (
    <div className="activity-list">
      <h2>Recent Activity</h2>
      {activities.map((activity) => (
        <div key={activity.id} className="activity-item">
          <span className="activity-icon">{getActionIcon(activity.action)}</span>
          <div className="activity-content">
            <p className="activity-action">{activity.actionLabel}</p>
            <p className="activity-details">
              {activity.entityName} by {activity.userEmail}
            </p>
            <p className="activity-time">{activity.timeAgo}</p>
          </div>
        </div>
      ))}
    </div>
  );
};
```

**Example Usage (Vue Component):**

```vue
<template>
  <div class="activity-list">
    <h2>Recent Activity</h2>
    <div v-if="loading">Loading activities...</div>
    <div v-else>
      <div 
        v-for="activity in activities" 
        :key="activity.id" 
        class="activity-item"
      >
        <span class="activity-icon">{{ getActionIcon(activity.action) }}</span>
        <div class="activity-content">
          <p class="activity-action">{{ activity.actionLabel }}</p>
          <p class="activity-details">
            {{ activity.entityName }} by {{ activity.userEmail }}
          </p>
          <p class="activity-time">{{ activity.timeAgo }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activities: [],
      loading: true
    };
  },
  async mounted() {
    try {
      const response = await this.getRecentActivity(10);
      if (response.success) {
        this.activities = response.payload;
      }
    } catch (error) {
      console.error('Error fetching activities:', error);
    } finally {
      this.loading = false;
    }
  },
  methods: {
    async getRecentActivity(limit = 10, offset = 0) {
      const response = await fetch(
        `http://your-api-url/api/v1/dashboard/recent-activity?limit=${limit}&offset=${offset}`,
        {
          method: 'GET',
          headers: this.getAuthHeaders()
        }
      );
      return response.json();
    },
    getAuthHeaders() {
      const token = localStorage.getItem('accessToken');
      return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      };
    },
    getActionIcon(action) {
      const icons = {
        'PLACE_CREATED': '➕',
        'PLACE_UPDATED': '✏️',
        'PLACE_DELETED': '🗑️',
        'IMAGE_UPLOADED': '📷',
        'IMAGE_DELETED': '🗑️',
        'USER_REGISTERED': '👤'
      };
      return icons[action] || '📝';
    }
  }
};
</script>
```

---

## Error Handling

### Standard Error Response Format

All endpoints return errors in this format:

```json
{
  "success": false,
  "message": "Error message here",
  "status": "UNAUTHORIZED",
  "timestamp": "2025-12-08T10:00:00"
}
```

### HTTP Status Codes

| Code | Status | Description |
|------|--------|-------------|
| `200` | OK | Request successful |
| `401` | UNAUTHORIZED | Missing or invalid token |
| `403` | FORBIDDEN | Insufficient permissions (not admin) |
| `500` | INTERNAL_SERVER_ERROR | Server error |

### Error Handling Example

```javascript
const handleApiCall = async (apiFunction) => {
  try {
    const response = await apiFunction();
    
    if (!response.success) {
      switch (response.status) {
        case 'UNAUTHORIZED':
          // Token expired or invalid
          localStorage.removeItem('accessToken');
          window.location.href = '/login';
          break;
        case 'FORBIDDEN':
          // Not admin
          alert('Access denied: Admin role required');
          break;
        default:
          alert(`Error: ${response.message}`);
      }
      return null;
    }
    
    return response.payload;
  } catch (error) {
    // Handle network errors
    console.error('Network error:', error);
    alert('Network error. Please check your connection.');
    return null;
  }
};

// Usage
const stats = await handleApiCall(() => getDashboardStats());
```

---

## Complete Examples

### Example 1: Dashboard Page (React)

```jsx
import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';

const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [chartData, setChartData] = useState([]);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch all dashboard data in parallel
        const [statsRes, chartRes, activitiesRes] = await Promise.all([
          getDashboardStats(),
          getPlacesOverview(6),
          getRecentActivity(10)
        ]);

        if (statsRes.success) setStats(statsRes.payload);
        if (chartRes.success) setChartData(chartRes.payload);
        if (activitiesRes.success) setActivities(activitiesRes.payload);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <div>Loading dashboard...</div>;
  }

  return (
    <div className="dashboard">
      <h1>Admin Dashboard</h1>
      
      {/* Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Users</h3>
          <p className="stat-value">{stats?.totalUsers}</p>
          <p className={`stat-change ${stats?.totalUsersChange >= 0 ? 'positive' : 'negative'}`}>
            {stats?.totalUsersChange >= 0 ? '+' : ''}{stats?.totalUsersChange?.toFixed(1)}%
          </p>
        </div>
        
        <div className="stat-card">
          <h3>Total Places</h3>
          <p className="stat-value">{stats?.totalPlaces}</p>
          <p className={`stat-change ${stats?.totalPlacesChange >= 0 ? 'positive' : 'negative'}`}>
            {stats?.totalPlacesChange >= 0 ? '+' : ''}{stats?.totalPlacesChange?.toFixed(1)}%
          </p>
        </div>
        
        <div className="stat-card">
          <h3>Total Images</h3>
          <p className="stat-value">{stats?.totalImages}</p>
          <p className={`stat-change ${stats?.totalImagesChange >= 0 ? 'positive' : 'negative'}`}>
            {stats?.totalImagesChange >= 0 ? '+' : ''}{stats?.totalImagesChange?.toFixed(1)}%
          </p>
        </div>
      </div>

      {/* Chart */}
      <div className="chart-section">
        <h2>Places Created (Last 6 Months)</h2>
        <LineChart width={800} height={300} data={chartData}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="month" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="count" stroke="#8884d8" name="Places Created" />
        </LineChart>
      </div>

      {/* Recent Activity */}
      <div className="activity-section">
        <h2>Recent Activity</h2>
        {activities.map((activity) => (
          <div key={activity.id} className="activity-item">
            <span>{activity.actionLabel}</span>
            <span>{activity.entityName}</span>
            <span>{activity.timeAgo}</span>
          </div>
        ))}
      </div>
    </div>
  );
};
```

### Example 2: Using Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://your-api-url/api/v1/dashboard'
});

// Add token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Dashboard API functions
export const dashboardApi = {
  getStats: () => api.get('/stats'),
  getPlacesOverview: (months = 6) => api.get(`/places-overview?months=${months}`),
  getRecentActivity: (limit = 10, offset = 0) => 
    api.get(`/recent-activity?limit=${limit}&offset=${offset}`)
};

// Usage
const loadDashboard = async () => {
  try {
    const [stats, chart, activities] = await Promise.all([
      dashboardApi.getStats(),
      dashboardApi.getPlacesOverview(6),
      dashboardApi.getRecentActivity(10)
    ]);
    
    console.log('Stats:', stats.data.payload);
    console.log('Chart Data:', chart.data.payload);
    console.log('Activities:', activities.data.payload);
  } catch (error) {
    if (error.response?.status === 401) {
      // Handle unauthorized
      window.location.href = '/login';
    }
  }
};
```

---

## Summary

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/v1/dashboard/stats` | GET | Get dashboard statistics | ROLE_ADMIN |
| `/api/v1/dashboard/places-overview` | GET | Get places chart data | ROLE_ADMIN |
| `/api/v1/dashboard/recent-activity` | GET | Get recent activities | ROLE_ADMIN |

---

## Support

For API documentation and testing, visit:
- Swagger UI: `http://your-api-url/swagger-ui.html`
- API Docs: `http://your-api-url/v3/api-docs`

---

**Note:** All dashboard endpoints require the user to have `ROLE_ADMIN` role. Regular users will receive `403 FORBIDDEN` errors.

