# Minecraft Mods Catalog — Concurrency demo

This project includes examples for asynchronous business operations and race-condition demonstrations.

## Endpoints

- POST /api/demo/async
  - Request body: { "delayMs": 250, "label": "demo-task" }
  - Response: taskId, status, and message
- GET /api/demo/async/{taskId}
  - Returns current status: ACCEPTED, RUNNING, COMPLETED, FAILED
- GET /api/demo/counter?threads=50&incrementsPerThread=1000
  - Shows unsafe and atomic counter results
