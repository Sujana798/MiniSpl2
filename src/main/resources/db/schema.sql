CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    student_id TEXT,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('STUDENT', 'ADMIN')),
    recovery_code_hash TEXT NOT NULL,
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

CREATE TABLE IF NOT EXISTS matches (
    match_id INTEGER PRIMARY KEY AUTOINCREMENT,
    lost_report_id INTEGER NOT NULL,
    found_report_id INTEGER NOT NULL,
    match_score REAL NOT NULL,
    confidence TEXT NOT NULL CHECK(confidence IN ('HIGH', 'MEDIUM', 'LOW')),
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'CONFIRMED', 'REJECTED')),
    category_score REAL NOT NULL,
    description_score REAL NOT NULL,
    location_score REAL NOT NULL,
    date_score REAL NOT NULL,
    attribute_score REAL NOT NULL,
    match_reason TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (lost_report_id, found_report_id),
    FOREIGN KEY (lost_report_id) REFERENCES item_reports(report_id),
    FOREIGN KEY (found_report_id) REFERENCES item_reports(report_id)
);

CREATE TABLE IF NOT EXISTS claims (
    claim_id INTEGER PRIMARY KEY AUTOINCREMENT,
    match_id INTEGER NOT NULL,
    claimant_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'APPROVED', 'REJECTED', 'RETURNED')),
    reviewed_by INTEGER,
    reviewed_at TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (claimant_id) REFERENCES users(user_id),
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);