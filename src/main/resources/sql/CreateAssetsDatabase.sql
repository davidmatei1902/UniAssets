USE EvidentaDotarilor;
GO

/*
=================================================================
    CREATE TABELE PARINTE
=================================================================
*/


CREATE TABLE Sali (
    SalaID INT IDENTITY(1,1) PRIMARY KEY,
    NumeSala NVARCHAR(100) NOT NULL,
    Etaj INT,
    Capacitate INT,
    TipSala NVARCHAR(50)
);
GO

CREATE TABLE Dotari (
    DotareID INT IDENTITY(1,1) PRIMARY KEY,
    NumeDotare NVARCHAR(100) NOT NULL,
    TipDotare NVARCHAR(50),
    Producator NVARCHAR(100),
    Stare NVARCHAR(50)
);
GO

CREATE TABLE Caracteristici (
    CaracteristiciID INT IDENTITY(1,1) PRIMARY KEY,
    NumeCaracteristica NVARCHAR(100) NOT NULL,
    Valoare NVARCHAR(100),
    Greutate DECIMAL(5,2)
);
GO

CREATE TABLE Facultati (
    FacultateID INT IDENTITY(1,1) PRIMARY KEY,
    NumeFacultate NVARCHAR(150) NOT NULL UNIQUE,
    CodFacultate NVARCHAR(10) UNIQUE, -- Câmpul nou adăugat
    Website NVARCHAR(255) -- Câmpul nou adăugat
);
GO

/*
=================================================================
    CREARE TABELE COPIL
=================================================================
*/

CREATE TABLE Departament (
    DepartamentID INT IDENTITY(1,1) PRIMARY KEY,
    NumeDepartament NVARCHAR(100) NOT NULL,
    SefDepartament NVARCHAR(100),
    Telefon NVARCHAR(20),
    FacultateID INT NOT NULL,
    CONSTRAINT FK_Departament_Facultate FOREIGN KEY (FacultateID) 
        REFERENCES Facultati(FacultateID)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);
GO

/*
=================================================================
    CREARE TABELE INTERMEDIARE
=================================================================
*/

CREATE TABLE SalaDotari (
    SalaID INT NOT NULL,
    DotareID INT NOT NULL,
    Cantitate INT,
    DataInstalare DATE,
    PRIMARY KEY (SalaID, DotareID),
    FOREIGN KEY (SalaID) REFERENCES Sali(SalaID),
    FOREIGN KEY (DotareID) REFERENCES Dotari(DotareID)
);
GO

CREATE TABLE DotariCaracteristici (
    DotareID INT NOT NULL,
    CaracteristiciID INT NOT NULL,
    PRIMARY KEY (DotareID, CaracteristiciID),
    FOREIGN KEY (DotareID) REFERENCES Dotari(DotareID),
    FOREIGN KEY (CaracteristiciID) REFERENCES Caracteristici(CaracteristiciID)
);
GO

CREATE TABLE SalaDepartament (
    SalaID INT NOT NULL,
    DepartamentID INT NOT NULL,
    TipUtilizare NVARCHAR(50),
    PRIMARY KEY (SalaID, DepartamentID),
    FOREIGN KEY (SalaID) REFERENCES Sali(SalaID),
    FOREIGN KEY (DepartamentID) REFERENCES Departament(DepartamentID)
);
GO