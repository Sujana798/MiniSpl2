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

CREATE TABLE IF NOT EXISTS item_reports (
    report_id INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_id INTEGER NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('LOST', 'FOUND')),
    category TEXT NOT NULL,
    brand TEXT,
    color TEXT,
    title TEXT NOT NULL,
    description TEXT,
    location TEXT NOT NULL,
    date_occurred TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'REPORTED' CHECK(status IN ('REPORTED', 'MATCHED', 'CLAIMED', 'RETURNED', 'CLOSED')),
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(user_id)
);


