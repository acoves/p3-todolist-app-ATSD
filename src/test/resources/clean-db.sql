-- Option 1: Cascade the delete
DELETE FROM equipo_usuario; -- Clear child table first
DELETE FROM usuarios;       -- Then clear parent table

-- Option 2: Disable foreign key checks temporarily
SET REFERENTIAL_INTEGRITY FALSE;
DELETE FROM usuarios;       -- Perform the delete
SET REFERENTIAL_INTEGRITY TRUE; -- Re-enable foreign key checks