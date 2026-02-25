-- Create points table

CREATE TABLE points (
	firstName VARCHAR(256) NOT NULL,
	lastName VARCHAR(256) NOT NULL,
	points INT NOT NULL CHECK (points >= 0),
	PRIMARY KEY (firstName, lastName)
);

-- Create events table

CREATE TABLE events (
	name VARCHAR(256) NOT NULL,
	location VARCHAR(256) NOT NULL,
	timeAndDate DATETIME NOT NULL,
	PRIMARY KEY (location, timeAndDate)
);

-- Create members table

CREATE TABLE members (
	firstName VARCHAR(256) NOT NULL,
	lastName VARCHAR(256) NOT NULL,
	email VARCHAR(256),
	phoneNumber VARCHAR(256),
	PRIMARY KEY (firstName, lastName)
);

-- Create accounts table

CREATE TABLE accounts (
	username VARCHAR(256) NOT NULL,
	passwordHash VARCHAR(512) NOT NULL,
	userRank ENUM ('Member', 'Director', 'Chairman') NOT NULL,
	PRIMARY KEY (username)
);
