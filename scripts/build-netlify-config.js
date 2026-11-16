'use strict';

const fs = require('fs');
const path = require('path');

const configuredApi = (process.env.MYF_API || '').trim();
if (!configuredApi) {
  throw new Error('Set the MYF_API Netlify environment variable to the HTTPS Cloudflare hostname.');
}

let apiUrl;
try {
  apiUrl = new URL(configuredApi);
} catch {
  throw new Error('MYF_API must be a valid HTTPS origin, for example https://api.example.com.');
}

if (apiUrl.protocol !== 'https:' ||
    !apiUrl.hostname ||
    apiUrl.username ||
    apiUrl.password ||
    apiUrl.pathname !== '/' ||
    apiUrl.search ||
    apiUrl.hash) {
  throw new Error('MYF_API must be an HTTPS origin without credentials, path, query, or fragment.');
}

const configPath = path.resolve(__dirname, '../frontend/runtime-config.js');
fs.writeFileSync(
    configPath,
    `window.MYF_API = ${JSON.stringify(apiUrl.origin)};\n`,
    'utf8'
);

console.log(`Generated frontend API configuration for ${apiUrl.origin}.`);
