create table users (id int,mail varchar(30), login varchar(30), dateOfBirth date, password varchar(30), photo varchar(30),status varchar(30));
create table chat (id int,admin int, name varchar(30));
create table membership (chatId int,userInChat int, isBanned boolean);
create table message (textOfMessage varchar(30), date timestamp, additionalContent varchar(30),chatId int,who int);
