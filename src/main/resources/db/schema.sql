CREATE TABLE IF NOT EXISTS presents
(
  id            INTEGER auto_increment NOT NULL PRIMARY KEY,
  title         VARCHAR(50) NOT NULL,
  description   VARCHAR(1024) NOT NULL DEFAULT '',
  price         INTEGER NOT NULL DEFAULT 0,
  url           VARCHAR(1024),
  image_url     VARCHAR(1024),
  status        VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
  contact       VARCHAR(64)
)
