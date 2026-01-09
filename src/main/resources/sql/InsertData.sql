USE EvidentaDotarilor;
GO

PRINT '1/7: Resetare completa baza de date...';
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

PRINT '2/7: Inserare Utilizatori...';
INSERT INTO Utilizatori (username, parola) VALUES 
('admin', '1234'), ('david', 'passDavid'), ('rares', 'passRares'), ('student', 'pass123');
GO

PRINT '3/7: Inserare Facultati si Departamente...';
INSERT INTO Facultati (NumeFacultate, CodFacultate, Website) VALUES 
('Automatica si Calculatoare', 'AC', 'https://acs.pub.ro'),
('Electronica, Telecomunicatii si Tehnologia Informatiei', 'ETTI', 'https://etti.pub.ro'),
('Inginerie Mecanica si Mecatronica', 'FIMM', 'https://fimm.upb.ro'),
('Antreprenoriat, Ingineria si Managementul Afacerilor', 'FAIMA', 'https://faima.pub.ro'),
('Transporturi', 'TR', 'https://transport.pub.ro');

DECLARE @FacAC_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'AC');
DECLARE @FacETTI_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'ETTI');
DECLARE @FacFIMM_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'FIMM');
DECLARE @FacFAIMA_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'FAIMA');
DECLARE @FacTR_ID INT = (SELECT FacultateID FROM Facultati WHERE CodFacultate = 'TR');

INSERT INTO Departament (NumeDepartament, FacultateID, SefDepartament, Telefon) VALUES 
('Calculatoare', @FacAC_ID, 'Prof. Dr. Popescu', '021-402-9101'),
('Automatica', @FacAC_ID, 'Prof. Dr. Ionescu', '021-402-9100'),
('Telecomunicatii', @FacETTI_ID, 'Prof. Dr. Stan', '021-402-4500'),
('Dispozitive Electronice', @FacETTI_ID, 'Conf. Dr. Marin', '021-402-4501'),
('Mecatronica', @FacFIMM_ID, 'Prof. Dr. Voicu', '021-402-9200'),
('Termotehnica', @FacFIMM_ID, 'Prof. Dr. Dobre', '021-402-9201'),
('Management', @FacFAIMA_ID, 'Prof. Dr. Econ. Radu', '021-402-9300'),
('Autovehicule Rutiere', @FacTR_ID, 'Conf. Dr. Ing. Alexe', '021-402-9400');
GO

PRINT '4/7: Inserare Sali...';
INSERT INTO Sali (NumeSala, Etaj, Capacitate, TipSala) VALUES
('EC101', 1, 120, 'Curs'), ('ED312', 3, 35, 'Laborator'), ('PR504', 5, 45, 'Laborator'),
('B204', 2, 25, 'Laborator'), ('A02', 0, 200, 'Curs'),
('CK102', 1, 30, 'Laborator'), ('CF001', 0, 95, 'Curs'),
('BN205', 2, 35, 'Laborator'), ('BN001', 0, 80, 'Curs'),
('JA101', 1, 20, 'Laborator'), ('JE001', 0, 115, 'Curs');
GO

PRINT '5/7: Inserare Dotari...';
INSERT INTO Dotari (NumeDotare, TipDotare, Producator, Stare) VALUES
('PC Workstation', 'Computing', 'Dell', 'Functional'),
('Osciloscop Digital', 'Measurement', 'Rigol', 'Functional'),
('Imprimanta 3D Resin', 'Prototyping', 'Creality', 'Defect'),
('Banc de probe motor', 'Mechanical', 'Bosch', 'Mentenanta'),
('Proiector 4K Laser', 'Multimedia', 'Epson', 'Defect'),
('Switch Core 10G', 'Networking', 'Cisco', 'Functional'),
('Kit Dezvoltare NVIDIA', 'AI', 'NVIDIA', 'Functional'),
('Analizor Logica', 'Measurement', 'Keysight', 'Defect'),
('Server Rack 2U', 'Computing', 'HPE', 'Functional');
GO

PRINT '6/7: Inserare Caracteristici si Asociere Dotari...';
INSERT INTO Caracteristici (NumeCaracteristica, Valoare, Greutate) VALUES
('Procesor', 'Intel i9-13900K', 0.50), 
('Memorie RAM', '64GB DDR5', 0.10),
('Stocare', '2TB NVMe SSD', 0.05),
('Luminozitate', '5000 Lumeni', 8.00);

INSERT INTO DotariCaracteristici (DotareID, CaracteristiciID)
SELECT d.DotareID, c.CaracteristiciID FROM Dotari d, Caracteristici c
WHERE d.NumeDotare = 'PC Workstation' AND c.NumeCaracteristica IN ('Procesor', 'Memorie RAM', 'Stocare');
GO

PRINT '7/7: Populare masiva dotari si mapare departamentala...';

INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, (SELECT DotareID FROM Dotari WHERE NumeDotare = 'PC Workstation'), 20, GETDATE() FROM Sali;

INSERT INTO SalaDotari (SalaID, DotareID, Cantitate, DataInstalare)
SELECT SalaID, DotareID, 6, GETDATE()
FROM (
    SELECT s.SalaID, d.DotareID, ROW_NUMBER() OVER(PARTITION BY s.SalaID ORDER BY NEWID()) as RN
    FROM Sali s CROSS JOIN Dotari d WHERE d.NumeDotare != 'PC Workstation'
) t WHERE RN <= 3;

INSERT INTO SalaDepartament (SalaID, DepartamentID, TipUtilizare)
SELECT s.SalaID, d.DepartamentID, s.TipSala FROM Sali s, Departament d
WHERE (s.NumeSala LIKE 'EC%' AND d.NumeDepartament = 'Calculatoare')
   OR (s.NumeSala LIKE 'ED%' AND d.NumeDepartament = 'Calculatoare')
   OR (s.NumeSala LIKE 'PR%' AND d.NumeDepartament = 'Automatica')
   OR (s.NumeSala LIKE 'B%' AND d.NumeDepartament = 'Telecomunicatii')
   OR (s.NumeSala LIKE 'A%' AND d.NumeDepartament = 'Telecomunicatii')
   OR (s.NumeSala LIKE 'CK%' AND d.NumeDepartament = 'Mecatronica')
   OR (s.NumeSala LIKE 'CF%' AND d.NumeDepartament = 'Termotehnica')
   OR (s.NumeSala LIKE 'BN%' AND d.NumeDepartament = 'Management')
   OR (s.NumeSala LIKE 'JA%' AND d.NumeDepartament = 'Autovehicule Rutiere')
   OR (s.NumeSala LIKE 'JE%' AND d.NumeDepartament = 'Autovehicule Rutiere');

PRINT 'Baza de date University Assets Manager a fost initializata si populata!';