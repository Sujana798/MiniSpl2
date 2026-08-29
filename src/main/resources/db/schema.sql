CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    student_id TEXT,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN')),
    recovery_code_hash,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);