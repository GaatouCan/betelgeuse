CREATE TABLE `avatar`
(
    `player_id` BIGINT NOT NULL COMMENT '玩家ID',
    `index` INT NOT NULL COMMENT '索引',
    `expired_time` BIGINT NOT NULL DEFAULT 0 COMMENT '过期时间',
    `activated` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否激活',
    `in_used` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否正在使用',
    PRIMARY KEY (`player_id`, `index`)
) ENGINE = InnoDB CHARACTER SET = utf16 COMMENT '玩家头像';