-- Seed skill lookup values (safe to re-run: skips if names already exist)
USE ai_resume_screening;

INSERT IGNORE INTO skills (name, active) VALUES
  ('Data Visualization', TRUE),
  ('SQL & Database Management', TRUE),
  ('Statistical Analysis', TRUE),
  ('Problem Solving', TRUE),
  ('Programming & Development', TRUE),
  ('System Design', TRUE),
  ('Financial Reporting', TRUE),
  ('Budgeting & Forecasting', TRUE),
  ('Analytical Skills', TRUE),
  ('Digital Marketing', TRUE),
  ('Market Research', TRUE),
  ('Communication & Branding', TRUE),
  ('Process Optimization', TRUE),
  ('Project Management', TRUE),
  ('Supply Chain/Resource Planning', TRUE);

SELECT id, name, active FROM skills ORDER BY name;
