---------------------création table utilisateurs----------------------
DROP TABLE IF EXISTS utilisateurs CASCADE;
CREATE TABLE utilisateurs(
	 PASSEWORD VARCHAR(100) PRIMARY KEY,
	 username VARCHAR(100) NOT NULL,
	 useremail VARCHAR(100) 
);
INSERT INTO utilisateurs (PASSEWORD,username,useremail) values ('admin','admin','admin@gmail.com');
---------------------création table enseignant----------------------
DROP TABLE IF EXISTS enseignant CASCADE;
CREATE TABLE  enseignant(
	matricule VARCHAR(100) PRIMARY KEY,
	nom_enseignant VARCHAR(100) NOT NULL
);
---------------------création table ue----------------------
DROP TABLE IF EXISTS ue CASCADE;
CREATE TABLE  ue(
	id_ue VARCHAR(50) PRIMARY KEY,
	intitule VARCHAR(100) NOT NULL
);
---------------------création table classe----------------------
DROP TABLE IF EXISTS classe CASCADE;
CREATE TABLE  classe(
	id_classe SERIAL PRIMARY KEY,
	effectif INT NOT NULL,
	niveau VARCHAR(100),
	filiere VARCHAR(100)
);
---------------------création table évènement----------------------
DROP TABLE IF EXISTS evenement CASCADE;
CREATE TABLE evenement(
	id_evenement SERIAL PRIMARY KEY,
	type_evenement VARCHAR(100),
	nbre_participants INT
);
---------------------création table maintenance----------------------
DROP TABLE IF EXISTS maintenance CASCADE;
CREATE TABLE maintenance(
	id_maintenance SERIAL PRIMARY KEY,
	type_maintenance VARCHAR(100)
);
---------------------création table salle----------------------
DROP TABLE IF EXISTS salle CASCADE;
CREATE TABLE salle(
	 id_salle VARCHAR(100) PRIMARY KEY,
	 capacite INT NOT NULL,
	 type_salle VARCHAR(50) NOT NULL
);
---------------------création table occuper----------------------
DROP TABLE IF EXISTS occuper CASCADE;
CREATE TABLE occuper(
	id_classe INT,
	id_salle VARCHAR(100),
	date_ocu DATE,
	hd_ocu TIME,
	hf_ocu TIME,
	PRIMARY KEY(id_classe,id_salle,date_ocu),
	FOREIGN KEY(id_classe) REFERENCES classe(id_classe),
	FOREIGN KEY(id_salle) REFERENCES salle(id_salle)
);
---------------------création table suivre----------------------
DROP TABLE IF EXISTS suivre CASCADE;
CREATE TABLE suivre(
	id_classe INT,
	id_ue VARCHAR(100),
	PRIMARY KEY(id_classe,id_ue),
	FOREIGN KEY(id_classe) REFERENCES classe(id_classe),
	FOREIGN KEY(id_ue) REFERENCES ue(id_ue)
);
---------------------création table dispenser----------------------
DROP TABLE IF EXISTS dispenser CASCADE;
CREATE TABLE dispenser(
	matricule VARCHAR(100),
	id_ue VARCHAR(100),
	PRIMARY KEY(matricule,id_ue),
	FOREIGN KEY(matricule) REFERENCES enseignant(matricule),
	FOREIGN KEY(id_ue) REFERENCES ue(id_ue)
);
---------------------création table dérouler----------------------
DROP TABLE IF EXISTS derouler CASCADE;
CREATE TABLE derouler(
	id_salle VARCHAR(100),
	id_evenement INT,
	date_der DATE,
	hd_der TIME,
	hf_der TIME,
	PRIMARY KEY(id_evenement,id_salle,date_der),
	FOREIGN KEY(id_evenement) REFERENCES evenement(id_evenement),
	FOREIGN KEY(id_salle) REFERENCES salle(id_salle)
);
---------------------création table effectuer----------------------
DROP TABLE IF EXISTS effectuer CASCADE;
CREATE TABLE effectuer(
	id_salle VARCHAR(100),
	id_maintenance INT,
	date_eff DATE,
	hd_eff TIME,
	hf_eff TIME,
	PRIMARY KEY(id_maintenance,id_salle),
	FOREIGN KEY(id_maintenance) REFERENCES maintenance(id_maintenance),
	FOREIGN KEY(id_salle) REFERENCES salle(id_salle)
);


