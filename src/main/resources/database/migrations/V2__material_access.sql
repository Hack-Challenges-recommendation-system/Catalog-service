-- How a material is accessed: FREE, SUBSCRIPTION (part of a subscription) or PURCHASE (bought separately).
-- paid stays true for both kinds that aren't free: they show only a short benefit and the provided link.
ALTER TABLE material ADD COLUMN access VARCHAR(16) NOT NULL DEFAULT 'FREE';

UPDATE material SET access = 'PURCHASE' WHERE paid;
