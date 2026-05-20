-- Seed location lookup values (safe to re-run: skips if names already exist)
USE ai_resume_screening;

INSERT IGNORE INTO locations (name, active) VALUES
  ('Mumbai', TRUE),
  ('Bangalore', TRUE),
  ('Delhi', TRUE),
  ('Hyderabad', TRUE),
  ('Chennai', TRUE);

SELECT id, name, active FROM locations ORDER BY name;
