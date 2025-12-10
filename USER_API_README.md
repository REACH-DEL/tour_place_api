# User API Documentation

This guide explains how to use the Tour Place API endpoints available for users with the **USER** role.

## Table of Contents

1. [Base URL and Authentication](#base-url-and-authentication)
2. [Authentication Endpoints](#authentication-endpoints)
3. [Profile Management](#profile-management)
4. [Places](#places)
5. [Favorites](#favorites)
6. [Reviews](#reviews)
7. [Search History](#search-history)
8. [File Upload](#file-upload)
9. [Error Handling](#error-handling)
10. [Example Workflows](#example-workflows)

---

## Base URL and Authentication

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
Most endpoints require a **Bearer token** in the Authorization header. After login or registration, you'll receive a JWT token that should be included in all authenticated requests.

**Header Format:**
```
Authorization: Bearer <your-jwt-token>
```

**Example:**
```javascript
headers: {
  'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  'Content-Type': 'application/json'
}
```

---

## Authentication Endpoints

### 1. Register User

Register a new user account. An OTP will be sent to your email for verification.

**Endpoint:** `POST /api/v1/auth/register`

**Authentication:** Not required

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to email. Please verify OTP to complete registration.",
  "status": "OK"
}
```

**Next Step:** Check your email for the OTP and use it in the verify-otp endpoint.

---

### 2. Verify OTP

Complete registration by verifying the OTP sent to your email.

**Endpoint:** `POST /api/v1/auth/verify-otp`

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered and verified successfully",
  "payload": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "status": true,
      "role": "user",
      "profileImage": null,
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  },
  "status": "OK"
}
```

**Important:** Save the `accessToken` for authenticated requests.

---

### 3. Resend OTP

Resend OTP if you didn't receive it or it expired.

**Endpoint:** `POST /api/v1/auth/resend-otp`

**Authentication:** Not required

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

---

### 4. Login

Login with your email and password to get a JWT token.

**Endpoint:** `POST /api/v1/auth/login`

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "payload": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "user": {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "status": true,
      "role": "user",
      "profileImage": null
    }
  },
  "status": "OK"
}
```

---

### 5. Logout

Logout the current user. **Important:** You should remove the token from client storage.

**Endpoint:** `POST /api/v1/auth/logout`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Logout successful. Please remove the token from client storage.",
  "status": "OK"
}
```

---

### 6. Forgot Password

Request a password reset OTP to be sent to your email.

**Endpoint:** `POST /api/v1/auth/forgot-password`

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password reset OTP sent to email. Please check your inbox.",
  "status": "OK"
}
```

---

### 7. Reset Password

Reset your password using the OTP received via email.

**Endpoint:** `POST /api/v1/auth/reset-password`

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "otp": "123456",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password reset successfully. Please login with your new password.",
  "status": "OK"
}
```

---

### 8. Get OTP Status

Check the remaining time for an OTP (useful for countdown timer in UI).

**Endpoint:** `POST /api/v1/auth/otp-status`

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "john.doe@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP status retrieved successfully",
  "payload": {
    "hasOtp": true,
    "remainingSeconds": 95,
    "message": "OTP is valid. 95 seconds remaining."
  },
  "status": "OK"
}
```

---

## Profile Management

### 1. Get User Profile

Get the current authenticated user's profile information.

**Endpoint:** `GET /api/v1/auth/profile`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "payload": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "status": true,
    "role": "user",
    "profileImage": "https://example.com/profile.jpg",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "status": "OK"
}
```

---

### 2. Update Profile Image

Update your profile image URL. **Note:** First upload the image using the file upload endpoint to get the URL.

**Endpoint:** `PUT /api/v1/auth/profile/image`

**Authentication:** Required

**Request Body:**
```json
{
  "profileImageUrl": "https://example.com/files/view?fileName=profile/user123/image.jpg"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile image updated successfully",
  "payload": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "profileImage": "https://example.com/files/view?fileName=profile/user123/image.jpg",
    ...
  },
  "status": "OK"
}
```

---

### 3. Change Password

Change your password using your old password for confirmation.

**Endpoint:** `PUT /api/v1/auth/profile/password`

**Authentication:** Required

**Request Body:**
```json
{
  "oldPassword": "oldpassword123",
  "newPassword": "newpassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Password changed successfully",
  "status": "OK"
}
```

---

## Places

All place GET endpoints support **optional Bearer token**. If you provide a token, you'll see the `isFavorite` field indicating whether each place is in your favorites.

### 1. Get All Places

Get all places with optional filtering and favorite status.

**Endpoint:** `GET /api/v1/places?filter=all`

**Authentication:** Optional (Bearer token recommended)

**Query Parameters:**
- `filter` (optional): 
  - `"all"` or omit - Get all places
  - `"most_favorite"` - Get places ordered by favorite count

**Example:**
```javascript
// Without authentication
GET /api/v1/places?filter=all

// With authentication (to see favorite status)
GET /api/v1/places?filter=all
Headers: { Authorization: Bearer <token> }
```

**Response:**
```json
{
  "success": true,
  "message": "Places retrieved successfully",
  "payload": [
    {
      "placeId": "123e4567-e89b-12d3-a456-426614174000",
      "placeName": "Angkor Wat",
      "description": "Ancient temple complex",
      "mainImage": "https://example.com/image.jpg",
      "lat": 13.4125,
      "longitude": 103.8670,
      "additionalImages": ["https://example.com/img1.jpg"],
      "isFavorite": true,  // null if not authenticated, true/false if authenticated
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "OK"
}
```

---

### 2. Get Place by ID

Get detailed information about a specific place.

**Endpoint:** `GET /api/v1/places/{placeId}`

**Authentication:** Optional (Bearer token recommended)

**Example:**
```javascript
GET /api/v1/places/123e4567-e89b-12d3-a456-426614174000
Headers: { Authorization: Bearer <token> }  // Optional
```

**Response:** Same format as Get All Places (single object)

---

### 3. Search Places

Search places by name. Returns lightweight results with only `placeId` and `placeName`.

**Endpoint:** `GET /api/v1/places/search?query=Angkor`

**Authentication:** Optional

**Query Parameters:**
- `query` (required): Search term

**Response:**
```json
{
  "success": true,
  "message": "Search results retrieved successfully",
  "payload": [
    {
      "placeId": "123e4567-e89b-12d3-a456-426614174000",
      "placeName": "Angkor Wat"
    },
    {
      "placeId": "223e4567-e89b-12d3-a456-426614174001",
      "placeName": "Angkor Thom"
    }
  ],
  "status": "OK"
}
```

---

### 4. Get Nearby Places

Get places near a specific location using latitude and longitude.

**Endpoint:** `GET /api/v1/places/nearby?lat=13.4125&longitude=103.8670&limit=10`

**Authentication:** Optional (Bearer token recommended)

**Query Parameters:**
- `lat` (required): Latitude (-90 to 90)
- `longitude` (required): Longitude (-180 to 180)
- `limit` (optional): Maximum number of results (1-100, default: 10)

**Response:** Same format as Get All Places (array)

---

## Favorites

All favorite endpoints require authentication.

### 1. Add Favorite

Add a place to your favorites.

**Endpoint:** `POST /api/v1/favorites/{placeId}`

**Authentication:** Required

**Example:**
```javascript
POST /api/v1/favorites/123e4567-e89b-12d3-a456-426614174000
Headers: { Authorization: Bearer <token> }
```

**Response:**
```json
{
  "success": true,
  "message": "Place added to favorites",
  "payload": {
    "favId": "333e4567-e89b-12d3-a456-426614174002",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "placeId": "123e4567-e89b-12d3-a456-426614174000",
    "placeName": "Angkor Wat",
    "mainImage": "https://example.com/image.jpg",
    "createdAt": "2024-01-15T10:30:00"
  },
  "status": "CREATED"
}
```

---

### 2. Get All Favorites

Get all places in your favorites.

**Endpoint:** `GET /api/v1/favorites`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Favorites retrieved successfully",
  "payload": [
    {
      "favId": "333e4567-e89b-12d3-a456-426614174002",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "placeId": "123e4567-e89b-12d3-a456-426614174000",
      "placeName": "Angkor Wat",
      "mainImage": "https://example.com/image.jpg",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "OK"
}
```

---

### 3. Remove Favorite

Remove a place from your favorites.

**Endpoint:** `DELETE /api/v1/favorites/{placeId}`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Place removed from favorites",
  "status": "OK"
}
```

---

### 4. Check Favorite

Check if a place is in your favorites.

**Endpoint:** `GET /api/v1/favorites/check/{placeId}`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Check completed",
  "payload": true,  // or false
  "status": "OK"
}
```

---

## Reviews

### 1. Create Review

Create a review for a place. **Note:** One review per user per place. If you've already reviewed a place, use the update endpoint.

**Endpoint:** `POST /api/v1/reviews`

**Authentication:** Required

**Request Body:**
```json
{
  "placeId": "123e4567-e89b-12d3-a456-426614174000",
  "rating": 5,
  "comment": "Amazing place! Highly recommended."
}
```

**Rating:** Must be between 1 and 5 (integer)

**Response:**
```json
{
  "success": true,
  "message": "Review created successfully",
  "payload": {
    "reviewId": "444e4567-e89b-12d3-a456-426614174003",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "userProfileImage": "https://example.com/profile.jpg",
    "placeId": "123e4567-e89b-12d3-a456-426614174000",
    "placeName": "Angkor Wat",
    "rating": 5,
    "comment": "Amazing place! Highly recommended.",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "status": "CREATED"
}
```

---

### 2. Update Review

Update your own review.

**Endpoint:** `PUT /api/v1/reviews/{reviewId}`

**Authentication:** Required

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Updated comment"
}
```

**Note:** Both `rating` and `comment` are optional. Omit fields you don't want to update.

**Response:** Same format as Create Review

---

### 3. Delete Review

Delete your own review.

**Endpoint:** `DELETE /api/v1/reviews/{reviewId}`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Review deleted successfully",
  "status": "OK"
}
```

---

### 4. Get Reviews by Place

Get all reviews for a specific place (public endpoint).

**Endpoint:** `GET /api/v1/reviews/place/{placeId}`

**Authentication:** Not required

**Response:**
```json
{
  "success": true,
  "message": "Reviews retrieved successfully",
  "payload": [
    {
      "reviewId": "444e4567-e89b-12d3-a456-426614174003",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "John Doe",
      "userEmail": "john.doe@example.com",
      "userProfileImage": "https://example.com/profile.jpg",
      "placeId": "123e4567-e89b-12d3-a456-426614174000",
      "placeName": "Angkor Wat",
      "rating": 5,
      "comment": "Amazing place!",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "OK"
}
```

---

### 5. Get Reviews by User

Get all reviews by a specific user (public endpoint).

**Endpoint:** `GET /api/v1/reviews/user/{userId}`

**Authentication:** Not required

**Response:** Same format as Get Reviews by Place

---

### 6. Get Place Rating Statistics

Get average rating and rating distribution for a place (public endpoint).

**Endpoint:** `GET /api/v1/reviews/place/{placeId}/rating`

**Authentication:** Not required

**Response:**
```json
{
  "success": true,
  "message": "Place rating retrieved successfully",
  "payload": {
    "placeId": "123e4567-e89b-12d3-a456-426614174000",
    "placeName": "Angkor Wat",
    "averageRating": 4.5,
    "totalReviews": 120,
    "ratingCount1": 5,
    "ratingCount2": 10,
    "ratingCount3": 15,
    "ratingCount4": 30,
    "ratingCount5": 60
  },
  "status": "OK"
}
```

---

## Search History

All search history endpoints require authentication.

### 1. Create or Update Search History

Create a new search history entry or update existing one's timestamp (makes it the latest).

**Endpoint:** `POST /api/v1/search-history`

**Authentication:** Required

**Request Body:**
```json
{
  "placeId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Search history created/updated successfully",
  "payload": {
    "searchId": "555e4567-e89b-12d3-a456-426614174004",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "placeId": "123e4567-e89b-12d3-a456-426614174000",
    "placeName": "Angkor Wat",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "status": "CREATED"
}
```

---

### 2. Get Latest Search History

Get the 10 latest search history entries, ordered by `updated_at` descending.

**Endpoint:** `GET /api/v1/search-history`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Search history retrieved successfully",
  "payload": [
    {
      "searchId": "555e4567-e89b-12d3-a456-426614174004",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "placeId": "123e4567-e89b-12d3-a456-426614174000",
      "placeName": "Angkor Wat",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "status": "OK"
}
```

---

### 3. Update Search History Timestamp

Update the `updated_at` timestamp of a search history entry to make it the latest.

**Endpoint:** `PATCH /api/v1/search-history/{searchId}`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Search history timestamp updated successfully",
  "status": "OK"
}
```

---

### 4. Delete Search History

Delete a search history entry.

**Endpoint:** `DELETE /api/v1/search-history/{searchId}`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "message": "Search history deleted successfully",
  "status": "OK"
}
```

---

## File Upload

### 1. Upload File

Upload a file (image) and get the URL. Use this URL for profile images or other purposes.

**Endpoint:** `POST /api/v1/file/upload`

**Authentication:** Required

**Content-Type:** `multipart/form-data`

**Form Data:**
- `file` (required): The file to upload
- `fileType` (required): Type of file
  - `"PROFILE_IMAGE"` - For user profile images (users can upload their own)
  - `"MAIN_IMAGE"` - For place main images (admin only)
  - `"DETAIL_IMAGE"` - For place detail images (admin only)

**Note:** Users can only upload and manage their own `PROFILE_IMAGE` files.

**Response:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "payload": {
    "fileName": "profile/550e8400-e29b-41d4-a716-446655440000/image.jpg",
    "fileUrl": "http://localhost:8080/api/v1/file/view?fileName=profile/550e8400-e29b-41d4-a716-446655440000/image.jpg",
    "fileType": "PROFILE_IMAGE",
    "fileSize": 102400,
    "uploadedAt": "2024-01-15T10:30:00"
  },
  "status": "OK"
}
```

**Usage Example:**
```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('fileType', 'PROFILE_IMAGE');

fetch('http://localhost:8080/api/v1/file/upload', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer <token>'
  },
  body: formData
});
```

---

### 2. View File

View/download a file by its file name (public endpoint).

**Endpoint:** `GET /api/v1/file/view?fileName={fileName}`

**Authentication:** Not required

**Query Parameters:**
- `fileName` (required): The file name from upload response

**Example:**
```
GET /api/v1/file/view?fileName=profile/550e8400-e29b-41d4-a716-446655440000/image.jpg
```

**Response:** File content (image, etc.)

---

### 3. Delete File

Delete a file. Users can only delete their own profile images.

**Endpoint:** `DELETE /api/v1/file/delete/{fileName}`

**Authentication:** Required

**Note:** The `fileName` should be URL-encoded if it contains special characters.

**Response:**
```json
{
  "success": true,
  "message": "File deleted successfully",
  "status": "OK"
}
```

---

## Error Handling

All endpoints return errors in a consistent format:

```json
{
  "success": false,
  "message": "Error message describing what went wrong",
  "status": "BAD_REQUEST"  // or NOT_FOUND, UNAUTHORIZED, etc.
}
```

### Common HTTP Status Codes

- **200 OK** - Request successful
- **201 CREATED** - Resource created successfully
- **400 BAD_REQUEST** - Invalid request data or business logic error
- **401 UNAUTHORIZED** - Authentication required or invalid token
- **403 FORBIDDEN** - Insufficient permissions
- **404 NOT_FOUND** - Resource not found
- **500 INTERNAL_SERVER_ERROR** - Server error

### Common Error Messages

- `"Invalid email or password"` - Login failed
- `"Invalid or expired OTP"` - OTP verification failed
- `"User not found"` - User doesn't exist
- `"Place not found"` - Place doesn't exist
- `"You have already reviewed this place. Use update endpoint to modify your review."` - Duplicate review
- `"You can only update your own reviews"` - Unauthorized review update
- `"Old password is incorrect"` - Password change failed

---

## Example Workflows

### Workflow 1: Complete Registration Flow

```javascript
// Step 1: Register
const registerResponse = await fetch('http://localhost:8080/api/v1/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    fullName: 'John Doe',
    email: 'john.doe@example.com',
    password: 'password123'
  })
});

// Step 2: Check email for OTP, then verify
const verifyResponse = await fetch('http://localhost:8080/api/v1/auth/verify-otp', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john.doe@example.com',
    otp: '123456'
  })
});

const { payload } = await verifyResponse.json();
const token = payload.accessToken;

// Step 3: Save token for future requests
localStorage.setItem('token', token);
```

---

### Workflow 2: View Places with Favorite Status

```javascript
// Get all places with favorite status
const response = await fetch('http://localhost:8080/api/v1/places?filter=all', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`
  }
});

const { payload: places } = await response.json();

// Each place will have isFavorite: true/false (or null if not authenticated)
places.forEach(place => {
  console.log(`${place.placeName}: ${place.isFavorite ? 'Favorited' : 'Not favorited'}`);
});
```

---

### Workflow 3: Add Place to Favorites and Create Review

```javascript
const token = localStorage.getItem('token');
const placeId = '123e4567-e89b-12d3-a456-426614174000';

// Step 1: Add to favorites
await fetch(`http://localhost:8080/api/v1/favorites/${placeId}`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Step 2: Create a review
await fetch('http://localhost:8080/api/v1/reviews', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    placeId: placeId,
    rating: 5,
    comment: 'Amazing place!'
  })
});
```

---

### Workflow 4: Upload Profile Image

```javascript
const token = localStorage.getItem('token');

// Step 1: Upload file
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('fileType', 'PROFILE_IMAGE');

const uploadResponse = await fetch('http://localhost:8080/api/v1/file/upload', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const { payload: fileData } = await uploadResponse.json();
const fileUrl = fileData.fileUrl;

// Step 2: Update profile with the URL
await fetch('http://localhost:8080/api/v1/auth/profile/image', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    profileImageUrl: fileUrl
  })
});
```

---

### Workflow 5: Password Reset Flow

```javascript
// Step 1: Request password reset
await fetch('http://localhost:8080/api/v1/auth/forgot-password', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john.doe@example.com'
  })
});

// Step 2: Check OTP status (for countdown timer)
const statusResponse = await fetch('http://localhost:8080/api/v1/auth/otp-status', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john.doe@example.com'
  })
});

const { payload: status } = await statusResponse.json();
console.log(`OTP expires in ${status.remainingSeconds} seconds`);

// Step 3: Reset password with OTP
await fetch('http://localhost:8080/api/v1/auth/reset-password', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    email: 'john.doe@example.com',
    otp: '123456',
    newPassword: 'newpassword123'
  })
});
```

---

## Notes

1. **Token Expiration:** JWT tokens may expire. If you receive a 401 Unauthorized error, you should re-authenticate.

2. **Optional Authentication:** For place GET endpoints, authentication is optional. However, providing a token allows you to see your favorite status (`isFavorite` field).

3. **File Upload Limits:** Check with your backend administrator for maximum file size limits (typically 5MB).

4. **OTP Expiration:** OTPs expire after 2 minutes. Use the `otp-status` endpoint to check remaining time.

5. **One Review Per Place:** Each user can only create one review per place. To modify your review, use the update endpoint.

6. **Profile Images:** Users can only upload and manage their own profile images. Main and detail images are admin-only.

---

## Support

For issues or questions, contact your API administrator or check the Swagger UI documentation at:
```
http://localhost:8080/swagger-ui.html
```

