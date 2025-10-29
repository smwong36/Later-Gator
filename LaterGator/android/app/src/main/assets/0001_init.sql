-- =========================================
-- Later Gator - SQLite Schema (DB v1)
-- =========================================

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

BEGIN;

-- -----------------------------------------
-- Admin / versioning
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS schema_migrations (
	id              INTEGER PRIMARY KEY,
	version         INTEGER NOT NULL,
	applied_at_ms   INTEGER NOT NULL
);

-- -----------------------------------------
-- Core
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS profile (
	id                   INTEGER PRIMARY KEY,
	tz                   TEXT NOT NULL,                         -- IANA tz
	weekly_goal_minutes  INTEGER DEFAULT 0,
	privacy_level        TEXT NOT NULL DEFAULT 'local_only',
	created_at_ms        INTEGER NOT NULL,
	updated_at_ms        INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS apps (
	id            INTEGER PRIMARY KEY,
	package_name  TEXT NOT NULL UNIQUE,
	label         TEXT NOT NULL,
	category      TEXT,
	is_tracked    INTEGER NOT NULL DEFAULT 1 CHECK (is_tracked IN (0,1)),
	created_at_ms INTEGER NOT NULL,
	updated_at_ms INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS time_limit_prefs (
	id                           INTEGER PRIMARY KEY,
	scope                        TEXT NOT NULL,                  -- 'global' | 'per_app'
	app_id                       INTEGER REFERENCES apps(id) ON DELETE CASCADE,
	-- CURRENT values (editable)
	daily_limit_minutes_current  INTEGER,                        -- NULL = no daily cap
	weekly_limit_minutes_current INTEGER,                        -- NULL = no weekly cap
	-- ORIGINAL values (for reports/explainability)
	daily_limit_minutes_original  INTEGER,
	weekly_limit_minutes_original INTEGER,
	active                       INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0,1)),
	created_at_ms                INTEGER NOT NULL,
	updated_at_ms                INTEGER NOT NULL,
	notes                        TEXT
);
-- Avoid duplicate per-app rows; allow one global row
CREATE UNIQUE INDEX IF NOT EXISTS idx_time_limit_unique
	ON time_limit_prefs(scope, app_id);

-- Ledger when a limit is hit (for exact-time interrupts and reports)
CREATE TABLE IF NOT EXISTS time_limit_hits_ledger (
	id                 INTEGER PRIMARY KEY,
	at_ms              INTEGER NOT NULL,
	scope              TEXT NOT NULL,                            -- 'global' | 'per_app'
	app_id             INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	period             TEXT NOT NULL,                            -- 'daily' | 'weekly'
	limit_minutes      INTEGER NOT NULL,                         -- limit value in effect
	used_minutes       INTEGER NOT NULL,                         -- computed at hit
	action_taken       TEXT NOT NULL,                            -- 'warn'|'soft_block'|'hard_block'|'snooze_offered'|'snooze_granted'
	meta_json          TEXT                                       -- optional details
);
CREATE INDEX IF NOT EXISTS idx_time_limit_hits_time ON time_limit_hits_ledger(at_ms);

CREATE TABLE IF NOT EXISTS usage_sessions (
	id            INTEGER PRIMARY KEY,
	app_id        INTEGER NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
	started_at_ms INTEGER NOT NULL,
	ended_at_ms   INTEGER NOT NULL,
	duration_ms   INTEGER NOT NULL,
	source        TEXT NOT NULL,                                -- 'system'|'manual'|'import'
	blocked_flag  INTEGER NOT NULL DEFAULT 0 CHECK (blocked_flag IN (0,1)),
	notes         TEXT
);
CREATE INDEX IF NOT EXISTS idx_usage_app_time ON usage_sessions(app_id, started_at_ms);
CREATE INDEX IF NOT EXISTS idx_usage_started   ON usage_sessions(started_at_ms);

CREATE TABLE IF NOT EXISTS events (
	id         INTEGER PRIMARY KEY,
	at_ms      INTEGER NOT NULL,
	kind       TEXT NOT NULL,                                   -- e.g., 'mode_enabled','notif_sent'
	app_id     INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	meta_json  TEXT                                             -- JSON
);
CREATE INDEX IF NOT EXISTS idx_events_kind_time ON events(kind, at_ms);

CREATE TABLE IF NOT EXISTS weekly_snapshots (
	id                   INTEGER PRIMARY KEY,
	week_start_date      TEXT NOT NULL,                         -- 'YYYY-MM-DD' (ISO Monday)
	app_id               INTEGER REFERENCES apps(id) ON DELETE CASCADE,
	total_minutes        INTEGER NOT NULL DEFAULT 0,
	sessions_count       INTEGER NOT NULL DEFAULT 0,
	interventions_count  INTEGER NOT NULL DEFAULT 0,
	avg_session_minutes  REAL NOT NULL DEFAULT 0.0,
	created_at_ms        INTEGER NOT NULL,
	UNIQUE (week_start_date, app_id)
);
CREATE INDEX IF NOT EXISTS idx_snapshots_week ON weekly_snapshots(week_start_date);

-- -----------------------------------------
-- Modes
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS mode_settings (
	id                    INTEGER PRIMARY KEY,
	mode_name             TEXT NOT NULL,                        -- 'focus','vacation','sleep','custom-x'
	enabled               INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0,1)),
	priority              INTEGER NOT NULL DEFAULT 0,
	schedule_kind         TEXT NOT NULL DEFAULT 'none',         -- 'none'|'daily_window'|'days_of_week'
	start_time_local      TEXT,                                 -- 'HH:MM'
	end_time_local        TEXT,                                 -- 'HH:MM'
	days_mask             TEXT,                                 -- e.g., 'Mon,Tue,Wed'
	allow_strategy        TEXT NOT NULL DEFAULT 'all',          -- 'allowlist'|'blocklist'|'all'
	allowed_apps_json     TEXT,                                 -- JSON array of package_names
	blocked_apps_json     TEXT,                                 -- JSON array of package_names
	block_notifications   INTEGER NOT NULL DEFAULT 0 CHECK (block_notifications IN (0,1)),
	respect_system_dnd    INTEGER NOT NULL DEFAULT 0 CHECK (respect_system_dnd IN (0,1)),
	created_at_ms         INTEGER NOT NULL,
	updated_at_ms         INTEGER NOT NULL,
	notes                 TEXT
);
CREATE INDEX IF NOT EXISTS idx_modes_enabled_prio ON mode_settings(enabled, priority);

-- -----------------------------------------
-- Snooze
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS snooze_prefs (
	id                      INTEGER PRIMARY KEY,
	scope                   TEXT NOT NULL,                      -- 'global'|'per_app'
	app_id                  INTEGER REFERENCES apps(id) ON DELETE CASCADE,
	max_per_day_current     INTEGER NOT NULL,
	max_per_week_current    INTEGER NOT NULL,
	max_per_day_original    INTEGER NOT NULL,
	max_per_week_original   INTEGER NOT NULL,
	created_at_ms           INTEGER NOT NULL,
	updated_at_ms           INTEGER NOT NULL,
	notes                   TEXT
);

CREATE TABLE IF NOT EXISTS snooze_ledger (
	id              INTEGER PRIMARY KEY,
	used_at_ms      INTEGER NOT NULL,
	app_id          INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	reason          TEXT,
	linked_event_id INTEGER REFERENCES events(id) ON DELETE SET NULL,
	notes           TEXT
);
CREATE INDEX IF NOT EXISTS idx_snooze_time ON snooze_ledger(used_at_ms);

-- -----------------------------------------
-- Notifications / DND
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS notification_prefs (
	id                 INTEGER PRIMARY KEY,
	max_per_day        INTEGER NOT NULL DEFAULT 0,
	max_per_week       INTEGER NOT NULL DEFAULT 0,
	quiet_start_local  TEXT,                                    -- 'HH:MM'
	quiet_end_local    TEXT,                                    -- 'HH:MM'
	reminders_enabled  INTEGER NOT NULL DEFAULT 1 CHECK (reminders_enabled IN (0,1)),
	channel_caps_json  TEXT,                                    -- JSON per-channel caps
	created_at_ms      INTEGER NOT NULL,
	updated_at_ms      INTEGER NOT NULL
);

-- -----------------------------------------
-- Emergency Bypass
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS emergency_prefs (
	id              INTEGER PRIMARY KEY,
	enabled         INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0,1)),
	activation_note TEXT,
	activated_at_ms   INTEGER,
	deactivated_at_ms INTEGER,
	priority        INTEGER NOT NULL DEFAULT 9999,
	created_at_ms   INTEGER NOT NULL,
	updated_at_ms   INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS emergency_allowed_apps (
	id       INTEGER PRIMARY KEY,
	app_id   INTEGER NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
	notes    TEXT
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_emergency_allowed_unique ON emergency_allowed_apps(app_id);

-- -----------------------------------------
-- Pomodoro
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS pomodoro_prefs (
	id                      INTEGER PRIMARY KEY,
	work_minutes            INTEGER NOT NULL DEFAULT 25,
	short_break_minutes     INTEGER NOT NULL DEFAULT 5,
	long_break_minutes      INTEGER NOT NULL DEFAULT 15,
	intervals_per_long_break INTEGER NOT NULL DEFAULT 4,
	auto_start_next         INTEGER NOT NULL DEFAULT 0 CHECK (auto_start_next IN (0,1)),
	daily_goal_intervals    INTEGER NOT NULL DEFAULT 0,
	sound_enabled           INTEGER NOT NULL DEFAULT 0 CHECK (sound_enabled IN (0,1)),
	sound_key               TEXT,
	sound_volume            INTEGER NOT NULL DEFAULT 100,
	mascot_theme_key        TEXT,
	created_at_ms           INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS pomodoro_ledger (
	id                  INTEGER PRIMARY KEY,
	started_at_ms       INTEGER NOT NULL,
	ended_at_ms         INTEGER NOT NULL,
	kind                TEXT NOT NULL,                          -- 'work'|'short_break'|'long_break'
	planned_minutes     INTEGER NOT NULL,
	actual_minutes      INTEGER NOT NULL,
	outcome             TEXT NOT NULL,                          -- 'completed'|'skipped'|'interrupted'|'expired'
	interruption_reason TEXT,
	app_id              INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	notes               TEXT,
	created_at_ms       INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pomo_time ON pomodoro_ledger(started_at_ms);

-- -----------------------------------------
-- Reflections / Goals / Check-ins
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS reflection_prefs (
	id             INTEGER PRIMARY KEY,
	am_enabled     INTEGER NOT NULL DEFAULT 0 CHECK (am_enabled IN (0,1)),
	pm_enabled     INTEGER NOT NULL DEFAULT 0 CHECK (pm_enabled IN (0,1)),
	am_time_local  TEXT,
	pm_time_local  TEXT,
	created_at_ms  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS reflection_sessions (
	id                INTEGER PRIMARY KEY,
	kind              TEXT NOT NULL,                            -- 'am'|'pm'
	at_ms             INTEGER NOT NULL,
	for_date          TEXT NOT NULL,                            -- 'YYYY-MM-DD'
	discipline_1_5    INTEGER,
	motivation_1_5    INTEGER,
	notes             TEXT
);
CREATE INDEX IF NOT EXISTS idx_reflection_for_date ON reflection_sessions(for_date);

CREATE TABLE IF NOT EXISTS goals (
	id               INTEGER PRIMARY KEY,
	for_date         TEXT NOT NULL,                             -- 'YYYY-MM-DD'
	text             TEXT NOT NULL,
	created_via      TEXT NOT NULL,                             -- 'reflection_am'|'manual'
	status           TEXT NOT NULL DEFAULT 'planned',           -- 'planned'|'completed'|'skipped'
	created_at_ms    INTEGER NOT NULL,
	completed_at_ms  INTEGER
);
CREATE INDEX IF NOT EXISTS idx_goals_for_date ON goals(for_date);

CREATE TABLE IF NOT EXISTS checkin_prefs (
	id                 INTEGER PRIMARY KEY,
	enabled            INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0,1)),
	daily_max          INTEGER NOT NULL DEFAULT 0,
	window_start_local TEXT,                                    -- 'HH:MM'
	window_end_local   TEXT,                                    -- 'HH:MM'
	created_at_ms      INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS checkin_ledger (
	id               INTEGER PRIMARY KEY,
	prompted_at_ms   INTEGER NOT NULL,
	goal_id          INTEGER REFERENCES goals(id) ON DELETE SET NULL,
	question_text    TEXT NOT NULL,
	response         TEXT,                                      -- 'yes'|'no'|'snooze'|'dismissed'
	responded_at_ms  INTEGER,
	linked_snooze_id INTEGER REFERENCES snooze_ledger(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_checkins_time ON checkin_ledger(prompted_at_ms);

-- -----------------------------------------
-- Sleep Hygiene
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS sleep_prefs (
	id                  INTEGER PRIMARY KEY,
	enabled             INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0,1)),
	bedtime_local       TEXT,                                   -- 'HH:MM'
	winddown_minutes    INTEGER NOT NULL DEFAULT 0,
	nudge_after_bedtime INTEGER NOT NULL DEFAULT 0 CHECK (nudge_after_bedtime IN (0,1)),
	created_at_ms       INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS sleep_nudges_ledger (
	id            INTEGER PRIMARY KEY,
	at_ms         INTEGER NOT NULL,
	kind          TEXT NOT NULL,                                -- 'winddown'|'bedtime'|'after_bedtime'
	app_id        INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	action_taken  TEXT                                          -- 'dismissed'|'opened_app'|'opened_settings'
);
CREATE INDEX IF NOT EXISTS idx_sleep_nudges_time ON sleep_nudges_ledger(at_ms);

-- -----------------------------------------
-- Rewards & Motivation (optional but included in v1 to avoid migrations later)
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS points_ledger (
	id        INTEGER PRIMARY KEY,
	at_ms     INTEGER NOT NULL,
	delta     INTEGER NOT NULL,
	reason    TEXT NOT NULL,                                    -- 'goal_met'|'streak'|'focus_interval'|'weekly_improvement'
	meta_json TEXT                                              -- JSON
);
CREATE INDEX IF NOT EXISTS idx_points_time ON points_ledger(at_ms);

CREATE TABLE IF NOT EXISTS streaks (
	id           INTEGER PRIMARY KEY,
	type         TEXT NOT NULL,                                 -- 'daily_focus'|'goal_completion'
	current_len  INTEGER NOT NULL DEFAULT 0,
	longest_len  INTEGER NOT NULL DEFAULT 0,
	updated_at_ms INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS badges (
	id            INTEGER PRIMARY KEY,
	slug          TEXT NOT NULL UNIQUE,
	name          TEXT NOT NULL,
	description   TEXT,
	asset_key     TEXT NOT NULL,                                -- maps to bundled asset
	criteria_json TEXT                                          -- JSON rule
);

CREATE TABLE IF NOT EXISTS earned_badges (
	id            INTEGER PRIMARY KEY,
	badge_id      INTEGER NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
	earned_at_ms  INTEGER NOT NULL,
	evidence_json TEXT                                          -- JSON snapshot
);
CREATE INDEX IF NOT EXISTS idx_earned_badges_time ON earned_badges(earned_at_ms);

CREATE TABLE IF NOT EXISTS baseline_usage (
	id               INTEGER PRIMARY KEY,
	metric           TEXT NOT NULL,                             -- 'weekly_minutes_all'|'weekly_minutes_by_app'
	week_start_date  TEXT NOT NULL,
	app_id           INTEGER REFERENCES apps(id) ON DELETE SET NULL,
	value_minutes    INTEGER NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_baseline_unique ON baseline_usage(metric, week_start_date, app_id);

-- -----------------------------------------
-- Security (App Lock)
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS security_prefs (
	id                 INTEGER PRIMARY KEY,
	app_lock_enabled   INTEGER NOT NULL DEFAULT 0 CHECK (app_lock_enabled IN (0,1)),
	lock_method        TEXT,                                    -- 'biometric'|'device_credential'|'pin'
	last_unlock_at_ms  INTEGER,
	failed_attempts    INTEGER NOT NULL DEFAULT 0
);

-- -----------------------------------------
-- Calendar (scaffold; off by default)
-- -----------------------------------------
CREATE TABLE IF NOT EXISTS calendar_prefs (
	id          INTEGER PRIMARY KEY,
	enabled     INTEGER NOT NULL DEFAULT 0 CHECK (enabled IN (0,1)),
	provider    TEXT,                                           -- 'android'|'google'
	sync_scope  TEXT,                                           -- 'all'|'selected'
	last_sync_ms INTEGER
);

CREATE TABLE IF NOT EXISTS calendar_events_cache (
	provider_event_id TEXT PRIMARY KEY,
	title             TEXT,
	start_ms          INTEGER,
	end_ms            INTEGER,
	busy              INTEGER NOT NULL DEFAULT 1 CHECK (busy IN (0,1)),
	source_calendar   TEXT
);
CREATE INDEX IF NOT EXISTS idx_calendar_time ON calendar_events_cache(start_ms, end_ms);

-- -----------------------------------------
-- Finalize
-- -----------------------------------------
INSERT INTO schema_migrations(version, applied_at_ms)
SELECT 1, strftime('%s','now')*1000
WHERE NOT EXISTS (SELECT 1 FROM schema_migrations WHERE version = 1);

COMMIT;

-- Keep an explicit pragma for tooling that reads user_version:
PRAGMA user_version = 1;
