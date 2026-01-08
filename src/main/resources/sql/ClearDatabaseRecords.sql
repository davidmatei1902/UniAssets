USE EvidentaDotarilor;
GO

-- Ordinea contează din cauza relațiilor FK
DELETE FROM SalaDotari;
DELETE FROM DotariCaracteristici;
DELETE FROM SalaDepartament;

DELETE FROM Departament;
DELETE FROM Sali;
DELETE FROM Dotari;
DELETE FROM Caracteristici;
DELETE FROM Facultati;
DELETE FROM Utilizatori
GO