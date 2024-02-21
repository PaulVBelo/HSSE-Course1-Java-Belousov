CREATE TABLE article
(
    article_id      BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200)             NOT NULL,
    tags            VARCHAR(200)             NOT NULL,
    comments        VARCHAR(200)             ,
    trending        BOOLEAN                  NOT NULL
);

CREATE TABLE comment
(
    comment_id      BIGSERIAL   PRIMARY KEY,
    content         TEXT                                   NOT NULL,
    article_id      BIGINT REFERENCES article (article_id) NOT NULL
);