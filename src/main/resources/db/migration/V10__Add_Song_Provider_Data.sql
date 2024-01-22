alter table song
    add provider_id varchar(255)                            null,
    add type        enum ('YOUTUBE_VIDEO', 'SPOTIFY_TRACK') null;

UPDATE song
SET provider_id = REPLACE(REPLACE(link, "https://open.spotify.com/track/", ''), "https://www.youtube.com/watch?v=", ''),
    type        = IF(link LIKE "%youtube%", "YOUTUBE_VIDEO", "SPOTIFY_TRACK")
;


alter table song
    modify provider_id varchar(255) not null,
    modify type enum ('YOUTUBE_VIDEO', 'SPOTIFY_TRACK') not null;

