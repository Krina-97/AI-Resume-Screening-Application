-- Seed nice-to-have skill lookup values (safe to re-run: skips if names already exist)
USE ai_resume_screening;

INSERT IGNORE INTO preferred_skills (name, active) VALUES
  ('Python/R Programming', TRUE),
  ('Machine Learning Basics', TRUE),
  ('Dashboard Tools (Power BI/Tableau)', TRUE),
  ('Cloud Computing', TRUE),
  ('DevOps Practices', TRUE),
  ('Agile/Scrum Methodology', TRUE),
  ('ERP Tools (SAP/Oracle)', TRUE),
  ('Taxation Knowledge', TRUE),
  ('Risk Management', TRUE),
  ('SEO/SEM Knowledge', TRUE),
  ('Content Creation', TRUE),
  ('CRM Tools (HubSpot/Salesforce)', TRUE),
  ('Lean Six Sigma', TRUE),
  ('Vendor Management', TRUE),
  ('Data-Driven Decision Making', TRUE);

SELECT id, name, active FROM preferred_skills ORDER BY name;
