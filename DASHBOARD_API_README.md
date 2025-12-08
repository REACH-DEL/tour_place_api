# Dashboard API Endpoints Documentation

This document describes the API endpoints required for the Admin Dashboard frontend.

## Base URL

```
http://your-api-url/api/v1
```

## Authentication

All dashboard endpoints require authentication with JWT Bearer token:

```
Authorization: Bearer {accessToken}
```

---

## 1. Dashboard Statistics

**Endpoint:** `GET /api/v1/dashboard/stats`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns statistics for the dashboard including total counts and percentage changes.

### Request

```http
GET /api/v1/dashboard/stats
Authorization: Bearer {token}
```

### Response

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

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `totalUsers` | number | Total number of registered users |
| `totalUsersChange` | number | Percentage change in users (compared to previous period, e.g., last month) |
| `totalPlaces` | number | Total number of places |
| `totalPlacesChange` | number | Percentage change in places (compared to previous period) |
| `totalImages` | number | Total number of uploaded images (main + detail images) |
| `totalImagesChange` | number | Percentage change in images (compared to previous period) |

### Notes

- Percentage changes should be calculated compared to the previous period (e.g., current month vs last month, or current week vs last week)
- Positive values indicate growth, negative values indicate decline
- The frontend will format these as "+12.5%" or "-5.2%"

---

## 2. Places Overview Chart Data

**Endpoint:** `GET /api/v1/dashboard/places-overview`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns monthly data for places creation chart (last 6 months).

### Request

```http
GET /api/v1/dashboard/places-overview
Authorization: Bearer {token}
```

### Query Parameters (Optional)

- `months` (integer, default: 6) - Number of months to retrieve data for

### Response

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

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `month` | string | Month abbreviation (Jan, Feb, Mar, etc.) |
| `monthNumber` | number | Month number (1-12) |
| `year` | number | Year |
| `count` | number | Number of places created in that month |

### Notes

- Data should be ordered chronologically (oldest to newest)
- Default should return last 6 months
- Count should include all places created in that month (based on `createdAt` timestamp)

---

## 3. Recent Activity

**Endpoint:** `GET /api/v1/dashboard/recent-activity`  
**Authorization:** Required (ROLE_ADMIN)  
**Description:** Returns list of recent activities/actions performed in the system.

### Request

```http
GET /api/v1/dashboard/recent-activity
Authorization: Bearer {token}
```

### Query Parameters (Optional)

- `limit` (integer, default: 10) - Number of activities to return
- `offset` (integer, default: 0) - Pagination offset

### Response

```json
{
  "success": true,
  "message": "Recent activity retrieved successfully",
  "payload": [
    {
      "id": "activity-uuid-1",
      "action": "PLACE_CREATED",
      "actionLabel": "New place added",
      "entityType": "PLACE",
      "entityId": "place-uuid-1",
      "entityName": "Mountain View",
      "userId": "user-uuid-1",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-08T08:00:00",
      "timeAgo": "2 hours ago"
    },
    {
      "id": "activity-uuid-2",
      "action": "PLACE_UPDATED",
      "actionLabel": "Place updated",
      "entityType": "PLACE",
      "entityId": "place-uuid-2",
      "entityName": "Lake Paradise",
      "userId": "user-uuid-1",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-08T05:00:00",
      "timeAgo": "5 hours ago"
    },
    {
      "id": "activity-uuid-3",
      "action": "IMAGE_UPLOADED",
      "actionLabel": "Image uploaded",
      "entityType": "PLACE",
      "entityId": "place-uuid-3",
      "entityName": "Desert Oasis",
      "userId": "user-uuid-1",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-07T10:00:00",
      "timeAgo": "1 day ago"
    },
    {
      "id": "activity-uuid-4",
      "action": "PLACE_DELETED",
      "actionLabel": "Place deleted",
      "entityType": "PLACE",
      "entityId": "place-uuid-4",
      "entityName": "Old Location",
      "userId": "user-uuid-1",
      "userEmail": "admin@example.com",
      "timestamp": "2025-12-06T15:00:00",
      "timeAgo": "2 days ago"
    },
    {
      "id": "activity-uuid-5",
      "action": "USER_REGISTERED",
      "actionLabel": "New user registered",
      "entityType": "USER",
      "entityId": "user-uuid-2",
      "entityName": "System",
      "userId": "user-uuid-2",
      "userEmail": "newuser@example.com",
      "timestamp": "2025-12-05T12:00:00",
      "timeAgo": "3 days ago"
    }
  ],
  "status": "OK",
  "timestamp": "2025-12-08T10:00:00"
}
```

### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique activity ID |
| `action` | string | Action type enum (see below) |
| `actionLabel` | string | Human-readable action label |
| `entityType` | string | Type of entity (PLACE, USER, IMAGE) |
| `entityId` | string | ID of the affected entity |
| `entityName` | string | Name of the affected entity (place name, user email, etc.) |
| `userId` | string | ID of user who performed the action |
| `userEmail` | string | Email of user who performed the action |
| `timestamp` | string | ISO 8601 timestamp of when action occurred |
| `timeAgo` | string | Human-readable relative time (e.g., "2 hours ago") |

### Action Types

The following action types should be supported:

- `PLACE_CREATED` - When a new place is created
- `PLACE_UPDATED` - When a place is updated
- `PLACE_DELETED` - When a place is deleted
- `IMAGE_UPLOADED` - When an image is uploaded (main or detail)
- `IMAGE_DELETED` - When an image is deleted
- `USER_REGISTERED` - When a new user registers

### Notes

- Activities should be ordered by timestamp (newest first)
- `timeAgo` can be calculated on frontend, but providing it on backend is preferred
- For `USER_REGISTERED` action, `entityName` can be "System" or the user's email
- For deleted entities, `entityName` should still be available (from audit log or soft delete)

---

## Error Responses

All endpoints return errors in this format:

```json
{
  "success": false,
  "message": "Error message here",
  "status": "BAD_REQUEST",
  "timestamp": "2025-12-08T10:00:00"
}
```

### HTTP Status Codes

- `200 OK` - Request successful
- `401 UNAUTHORIZED` - Missing or invalid token
- `403 FORBIDDEN` - Insufficient permissions (not admin)
- `500 INTERNAL_SERVER_ERROR` - Server error

---

## Implementation Notes

### Statistics Calculation

For percentage changes in statistics:

1. **Total Users Change**: Compare current total users with total users from previous period
   ```
   change = ((current - previous) / previous) * 100
   ```

2. **Total Places Change**: Same calculation for places

3. **Total Images Change**: Same calculation for images

### Activity Logging

To implement the recent activity endpoint, you may need to:

1. Create an activity/audit log table that records:
   - Action type
   - Entity type and ID
   - User who performed the action
   - Timestamp

2. Log activities when:
   - Places are created/updated/deleted
   - Images are uploaded/deleted
   - Users register

3. Query this log table for the recent activity endpoint

### Chart Data

For the places overview chart:

1. Group places by month based on `createdAt` timestamp
2. Count places created in each month
3. Return data for the last N months (default 6)
4. Include month abbreviation, number, year, and count

---

## Example Frontend Usage

```javascript
// Get dashboard statistics
const statsResponse = await fetch('http://your-api-url/api/v1/dashboard/stats', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const stats = await statsResponse.json();

// Get places overview
const chartResponse = await fetch('http://your-api-url/api/v1/dashboard/places-overview', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const chartData = await chartResponse.json();

// Get recent activity
const activityResponse = await fetch('http://your-api-url/api/v1/dashboard/recent-activity?limit=10', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const activities = await activityResponse.json();
```

---

## Summary

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/v1/dashboard/stats` | GET | Get dashboard statistics | ROLE_ADMIN |
| `/api/v1/dashboard/places-overview` | GET | Get places chart data | ROLE_ADMIN |
| `/api/v1/dashboard/recent-activity` | GET | Get recent activities | ROLE_ADMIN |

---

**Note:** All endpoints should follow the same response format as other API endpoints in your system, with `success`, `message`, `payload`, `status`, and `timestamp` fields.

