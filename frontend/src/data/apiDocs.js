export const API_ENDPOINTS = [
    {
        id: 'ai',
        title: 'AI API',
        subtitle: 'Interact with AI services.',
        method: 'POST',
        path: '/ai',
        description: 'Generate AI insights or answers based on system data.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "insight": "System usage has increased by 15%."\n  }\n}`
    },
    {
        id: 'api-keys',
        title: 'API Keys API',
        subtitle: 'Manage programmatic access keys.',
        method: 'GET',
        path: '/api-keys',
        description: 'Returns a list of API keys for the current user or organization.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "name": "Production Key", "createdAt": "2026-06-23T00:00:00" }\n  ]\n}`
    },
    {
        id: 'audit-logs',
        title: 'Audit Logs API',
        subtitle: 'Retrieve system audit logs.',
        method: 'GET',
        path: '/audit-logs',
        description: 'Returns the audit trail logs for monitoring user activities.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 500, "action": "LOGIN", "username": "admin" }\n  ]\n}`
    },
    {
        id: 'auth',
        title: 'Authentication API',
        subtitle: 'Authenticate users and manage sessions.',
        method: 'POST',
        path: '/auth/login',
        description: 'Authenticate and receive a JWT token.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "token": "eyJhbG...",\n    "refreshToken": "d7a8c..."\n  }\n}`
    },
    {
        id: 'billing',
        title: 'Billing API',
        subtitle: 'Manage subscriptions and invoices.',
        method: 'GET',
        path: '/billing/invoices',
        description: 'Retrieve a list of billing invoices.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": "INV-001", "amount": 299.99, "status": "PAID" }\n  ]\n}`
    },
    {
        id: 'dashboard',
        title: 'Dashboard API',
        subtitle: 'Fetch aggregate dashboard statistics.',
        method: 'GET',
        path: '/dashboard/stats',
        description: 'Returns overview metrics for the main dashboard.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "totalUsers": 150,\n    "activeTeams": 12,\n    "monthlyRevenue": 5450.00\n  }\n}`
    },
    {
        id: 'events',
        title: 'Events API',
        subtitle: 'Manage calendar events.',
        method: 'GET',
        path: '/events',
        description: 'Returns a list of scheduled events.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "title": "Team Meeting", "startTime": "2026-06-25T10:00:00" }\n  ]\n}`
    },
    {
        id: 'files',
        title: 'Files API',
        subtitle: 'Manage uploaded files and documents.',
        method: 'GET',
        path: '/files',
        description: 'Returns a list of files accessible to the user.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 101, "filename": "report.pdf", "size": 1024000 }\n  ]\n}`
    },
    {
        id: 'invitations',
        title: 'Invitations API',
        subtitle: 'Invite new users to the platform.',
        method: 'POST',
        path: '/invitations',
        description: 'Send an email invitation to a new team member.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "id": 42,\n    "email": "newuser@example.com",\n    "status": "PENDING"\n  }\n}`
    },
    {
        id: 'notifications',
        title: 'Notifications API',
        subtitle: 'Manage in-app user notifications.',
        method: 'GET',
        path: '/notifications',
        description: 'Returns unread notifications for the current user.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "message": "Your task is overdue", "read": false }\n  ]\n}`
    },
    {
        id: 'organizations',
        title: 'Organizations API',
        subtitle: 'Manage organization details and hierarchies.',
        method: 'GET',
        path: '/organizations',
        description: "Returns details about the user's organization.",
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "name": "Vortex Inc", "status": "ACTIVE" }\n  ]\n}`
    },
    {
        id: 'preferences',
        title: 'Preferences API',
        subtitle: 'Manage user-specific preferences.',
        method: 'PUT',
        path: '/preferences',
        description: 'Update user UI preferences like language or theme.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "language": "th",\n    "theme": "dark"\n  }\n}`
    },
    {
        id: 'reports',
        title: 'Reports API',
        subtitle: 'Generate analytical reports.',
        method: 'GET',
        path: '/reports',
        description: 'Export or fetch detailed statistical reports.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "name": "Q1 Performance", "type": "CSV" }\n  ]\n}`
    },
    {
        id: 'roles',
        title: 'Roles API',
        subtitle: 'Manage system roles and permissions.',
        method: 'GET',
        path: '/roles',
        description: 'Returns a list of all defined roles.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "name": "SUPER_ADMIN", "permissions": ["*"] }\n  ]\n}`
    },
    {
        id: 'search',
        title: 'Search API',
        subtitle: 'Global search endpoint.',
        method: 'GET',
        path: '/search?q=query',
        description: 'Search across multiple entities like users, tasks, and teams.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "users": [],\n    "tasks": [{ "id": 1, "title": "Fix bug" }]\n  }\n}`
    },
    {
        id: 'sessions',
        title: 'Sessions API',
        subtitle: 'Manage active login sessions.',
        method: 'GET',
        path: '/sessions',
        description: 'Returns all active device sessions for the user.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "ipAddress": "192.168.1.1", "isCurrent": true }\n  ]\n}`
    },
    {
        id: 'settings',
        title: 'System Settings API',
        subtitle: 'Manage global platform configuration.',
        method: 'GET',
        path: '/settings',
        description: 'Retrieve global system settings.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "maintenanceMode": false,\n    "defaultLanguage": "en"\n  }\n}`
    },
    {
        id: 'task-comments',
        title: 'Task Comments API',
        subtitle: 'Add and retrieve comments on tasks.',
        method: 'GET',
        path: '/tasks/{id}/comments',
        description: 'Retrieve all comments for a specific task.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "content": "Working on it!", "authorId": 5 }\n  ]\n}`
    },
    {
        id: 'tasks',
        title: 'Tasks API',
        subtitle: 'Manage tasks and track progress.',
        method: 'GET',
        path: '/tasks',
        description: 'Returns a paginated list of tasks.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 101, "title": "Update API Docs", "status": "IN_PROGRESS" }\n  ]\n}`
    },
    {
        id: 'teams',
        title: 'Teams API',
        subtitle: 'Manage teams and their members.',
        method: 'GET',
        path: '/teams',
        description: 'Returns a list of teams.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "name": "Engineering", "memberCount": 12 }\n  ]\n}`
    },
    {
        id: 'tickets',
        title: 'Tickets API',
        subtitle: 'Manage support tickets.',
        method: 'GET',
        path: '/tickets',
        description: 'Returns a list of customer support tickets.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "subject": "Cannot login", "status": "OPEN" }\n  ]\n}`
    },
    {
        id: 'two-factor',
        title: 'Two-Factor API',
        subtitle: 'Manage 2FA enrollment.',
        method: 'POST',
        path: '/2fa/enroll',
        description: 'Generate a secret and QR code for 2FA enrollment.',
        responseExample: `{\n  "status": "success",\n  "data": {\n    "secret": "JBSWY3DPEHPK3PXP",\n    "qrCode": "data:image/png;base64,..."\n  }\n}`
    },
    {
        id: 'users',
        title: 'Users API',
        subtitle: 'Manage users within your organization.',
        method: 'GET',
        path: '/users',
        description: 'Returns a paginated list of users.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "email": "user@vortex.com", "firstName": "John" }\n  ]\n}`
    },
    {
        id: 'webhooks',
        title: 'Webhooks API',
        subtitle: 'Configure outbound webhooks.',
        method: 'GET',
        path: '/webhooks',
        description: 'Returns configured webhook endpoints.',
        responseExample: `{\n  "status": "success",\n  "data": [\n    { "id": 1, "url": "https://myapp.com/webhook", "event": "user.created" }\n  ]\n}`
    }
];
