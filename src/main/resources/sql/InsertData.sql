USE EvidentaDotarilor;
GO

PRINT 'Curatare tabele si resetare indecsi...';
DELETE FROM SalaDotari; DELETE FROM DotariCaracteristici; DELETE FROM SalaDepartament;
DELETE FROM Sali; DELETE FROM Departament; DELETE FROM Facultati;
DELETE FROM Dotari; DELETE FROM Caracteristici; DELETE FROM Utilizatori; 

DBCC CHECKIDENT ('Facultati', RESEED, -1);
DBCC CHECKIDENT ('Departament', RESEED, -1);
DBCC CHECKIDENT ('Sali', RESEED, -1);
DBCC CHECKIDENT ('Dotari', RESEED, -1);
DBCC CHECKIDENT ('Caracteristici', RESEED, -1);
DBCC CHECKIDENT ('Utilizatori', RESEED, -1);
GO

PRINT 'Inserare Utilizatori...';
INSERT INTO Utilizatori (username, parola) VALUES 
('admin', '1234'), ('david', 'passDavid'), ('rares', 'passRares'), ('student', 'pass123');
GO

PRINT 'Inserare Facultati...';
INSERT INTO Facultati (NumeFacultate, CodFacultate, Website) VALUES 
('Automatica si Calculatoare', 'AC', 'https://acs.pub.ro'),
('Electronica, Telecomunicatii si Tehnologia Informatiei', 'ETTI', 'https://etti.pub.ro'),
('Inginerie Mecanica si Mecatronica', 'FIMM', 'https://fimm.upb.ro'),
('Antreprenoriat, Ingineria si Managementul Afacerilor', 'FAIMA', 'https://faima.pub.ro'),
('Transporturi', 'TR', 'https://transport.pub.ro');
GO

PRINT 'Inserare Departamente...';
DECLARE @FacAC_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'AC');
DECLARE @FacETTI_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'ETTI');
DECLARE @FacFIMM_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'FIMM');
DECLARE @FacFAIMA_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'FAIMA');
DECLARE @FacTR_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'TR');

INSERT INTO Departament (NumeDepartament, FacultateID, SefDepartament, Telefon) VALUES 
('Automatica', @FacAC_ID, 'Prof. Dr. Ionescu', '021-402-9100'),
('Calculatoare', @FacAC_ID, 'Prof. Dr. Popescu', '021-402-9101'),
('Telecomunicatii', @FacETTI_ID, 'Prof. Dr. Stan', '021-402-4500'),
('Dispozitive Electronice', @FacETTI_ID, 'Conf. Dr. Marin', '021-402-4501'),
('Mecatronica si Mecanica de Precizie', @FacFIMM_ID, 'Prof. Dr. Voicu', '021-402-9200'),
('Termotehnica', @FacFIMM_ID, 'Prof. Dr. Dobre', '021-402-9201'),
('Management si Antreprenoriat', @FacFAIMA_ID, 'Prof. Dr. Econ. Radu', '021-402-9300'),
('Autovehicule Rutiere', @FacTR_ID, 'Conf. Dr. Ing. Alexe', '021-402-9400'),
('Trafic si Siguranta', @FacTR_ID, 'S.L. Dr. Ing. Sandu', '021-402-9401');
GO

PRINT 'Inserare Sali...';
INSERT INTO Sali (NumeSala, Etaj, Capacitate, TipSala) VALUES
('EC101', 1, 101, 'Curs'), ('EC004', 0, 50, 'Curs'), ('EC102', 1, 80, 'Curs'),
('EG301', 3, 120, 'Curs'), ('ED312', 3, 30, 'Laborator'), ('PR504', 5, 40, 'Laborator'),
('A101', 1, 150, 'Curs'), ('B032', 0, 25, 'Laborator'), ('B204', 2, 30, 'Laborator'),
('A02', 0, 200, 'Curs'), ('CF001', 0, 90, 'Curs'), ('CJ105', 1, 20, 'Laborator'),
('CK102', 1, 25, 'Laborator'), ('CA01', 0, 100, 'Curs'), ('BN001', 0, 80, 'Curs'),
('BN120', 1, 30, 'Laborator'), ('BN205', 2, 30, 'Laborator'), ('JE001', 0, 110, 'Curs'),
('JE105', 1, 15, 'Laborator'), ('JA101', 1, 20, 'Laborator');
GO

PRINT 'Mapare Sala - Departament...';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Curs' FROM Sali s, Departament d WHERE s.NumeSala LIKE 'EC%' AND d.NumeDepartament = 'Calculatoare';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Laborator' FROM Sali s, Departament d WHERE s.NumeSala LIKE 'ED%' AND d.NumeDepartament = 'Automatica';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Curs' FROM Sali s, Departament d WHERE s.NumeSala IN ('A101', 'A02') AND d.NumeDepartament = 'Telecomunicatii';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Laborator' FROM Sali s, Departament d WHERE s.NumeSala IN ('B032', 'B204') AND d.NumeDepartament = 'Dispozitive Electronice';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Laborator' FROM Sali s, Departament d WHERE s.NumeSala IN ('CK102') AND d.NumeDepartament = 'Termotehnica';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Curs' FROM Sali s, Departament d WHERE s.NumeSala IN ('CF001', 'CA01') AND d.NumeDepartament = 'Mecatronica si Mecanica de Precizie';
INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, 'Laborator' FROM Sali s, Departament d WHERE s.NumeSala LIKE 'JA%' AND d.NumeDepartament = 'Trafic si Siguranta';
GO

PRINT 'Inserare Dotari...';
INSERT INTO Dotari (NumeDotare, TipDotare, Producator, Stare) VALUES
('Proiector Video 4K', 'Multimedia', 'Epson', 'Functional'),
('PC Desktop i7 12th', 'Computing', 'Dell', 'Functional'),
('PC Desktop i5 10th', 'Computing', 'HP', 'Functional'),
('Tabla Interactiva 65', 'Smartboard', 'Samsung', 'Functional'),
('Router Wi-Fi 6 Mesh', 'Networking', 'Cisco', 'Functional'),
('Osciloscop Digital', 'Measurement', 'Rigol', 'Functional'),
('Imprimanta 3D Resin', 'Prototyping', 'Creality', 'Defect'),
('Banc de probe motor', 'Mechanical', 'Bosch', 'Mentenanta'),
('Laptop Ultrabook Pro', 'Computing', 'Lenovo', 'Functional'),
('Multimetru Fluke', 'Measurement', 'Fluke', 'Functional'),
('Brat Robotic Kuka', 'Automation', 'Kuka', 'Functional'),
('Switch 48 Porturi', 'Networking', 'TP-Link', 'Functional');
GO

PRINT 'Inserare Caracteristici...';
INSERT INTO Caracteristici (NumeCaracteristica, Valoare, Greutate) VALUES
('Rezolutie', '3840x2160', 0.00), ('CPU', 'Intel i7', 0.00), ('RAM', '32GB', 0.00),
('Viteza', '1000Mbps', 0.00), ('Refresh', '144Hz', 0.00), ('Volum Print', '300mm', 15.00);
GO

PRINT 'Distribuire Dotari (Legaturi SalaDotari)...';
-- Adaugam PC-uri in laboratoare (AC si ETTI)
INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'PC Desktop i7 12th'), 15, '2023-01-10' 
FROM Sali WHERE NumeSala IN ('ED312', 'B204', 'PR504');

-- Adaugam PC-uri mai vechi in cursuri
INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'PC Desktop i5 10th'), 1, '2021-09-01' 
FROM Sali WHERE TipSala = 'Curs';

-- Adaugam Proiectoare in toate salile de curs
INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'Proiector Video 4K'), 1, '2022-05-20' 
FROM Sali WHERE TipSala = 'Curs';

-- Echipamente de masura in laboratoarele ETTI
INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'Osciloscop Digital'), 8, '2022-10-15' 
FROM Sali WHERE NumeSala LIKE 'B%';

-- Laptopuri pentru FAIMA
INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'Laptop Ultrabook Pro'), 20, '2023-03-12' 
FROM Sali WHERE NumeSala LIKE 'BN%';
GO

PRINT 'Asociere Dotari - Caracteristici...';
INSERT INTO DotariCaracteristici (DotareID, CaracteristiciID)
SELECT d.DotareID, c.CaracteristiciID FROM Dotari d, Caracteristici c
WHERE d.NumeDotare = 'PC Desktop i7 12th' AND c.NumeCaracteristica IN ('CPU', 'RAM');

INSERT INTO DotariCaracteristici (DotareID, CaracteristiciID)
SELECT d.DotareID, c.CaracteristiciID FROM Dotari d, Caracteristici c
WHERE d.NumeDotare = 'Proiector Video 4K' AND c.NumeCaracteristica = 'Rezolutie';
GO

PRINT 'Sistem initializat cu date extinse!';