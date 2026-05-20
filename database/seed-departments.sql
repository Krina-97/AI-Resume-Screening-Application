-- Seed department lookup values (safe to re-run: skips if names already exist)
USE ai_resume_screening;

INSERT IGNORE INTO departments (name, active) VALUES
  ('Engineering', TRUE),
  ('Finance & Accounting', TRUE),
  ('Data & Analytics', TRUE),
  ('Marketing', TRUE),
  ('Operations', TRUE);

SELECT id, name, active FROM departments ORDER BY name;
