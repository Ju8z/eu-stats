import axios from 'axios';

// Defaulting to a relative API path keeps browser requests on the current host.
// That makes local dev, VPS preview, and reverse-proxied deployments work without hard-coded localhost URLs.
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api';

const apiClient = axios.create({
    baseURL: apiBaseUrl,
    headers: {
        'Content-Type': 'application/json'
    }
});

export default apiClient;
