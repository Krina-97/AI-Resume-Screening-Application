-- Seed experience level lookup values (safe to re-run: skips if names already exist)
USE ai_resume_screening;

INSERT IGNORE INTO experience_levels (name, display_order, active) VALUES
  ('3-5 years', 1, TRUE),
  ('6-10 years', 2, TRUE),
  ('10-15 years', 3, TRUE),
  ('15+ years', 4, TRUE);

SELECT id, name, display_order, active FROM experience_levels ORDER BY display_order;
