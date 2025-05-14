create table users
(

    id       bigint primary key,
    username varchar(50) not null unique,
    password varchar(255) not null,
    email    varchar(50) not null unique,
    phone_number varchar(30)

);

alter table users owner to root;
create sequence user_id_seq;
alter sequence user_id_seq owner to root;