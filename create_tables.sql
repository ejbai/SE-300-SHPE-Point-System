USE shpe_db;

-- Create events table

CREATE TABLE events (
	name VARCHAR(256) NOT NULL,
	location VARCHAR(256) NOT NULL,
	timeAndDate DATETIME NOT NULL,
	pointsNeeded INT,
	pointsEarned INT,
	PRIMARY KEY (location, timeAndDate)
);

-- Create members table

CREATE TABLE members (
	studentID INT NOT NULL,
	firstName VARCHAR(256) NOT NULL,
	lastName VARCHAR(256) NOT NULL,
	email VARCHAR(256),
	phoneNumber VARCHAR(256),
	points INT,
	PRIMARY KEY (studentID)
);

-- Create accounts table

CREATE TABLE accounts (
	username VARCHAR(256) NOT NULL,
	passwordHashSHA256 VARCHAR(512) NOT NULL,
	userRank ENUM ('Member', 'Director', 'Chairman') NOT NULL,
	PRIMARY KEY (username)
);

--------------------------------------------------------------------------

-- Create some sample events

INSERT INTO events VALUES
	('Event1', 'The park', '2026-03-04', 20, 5),
	('Event2', 'The lake', '2026-03-06', 12, 3),
	('Event3', 'The courtyard', '2026-03-07', 10, 7)
;
	
