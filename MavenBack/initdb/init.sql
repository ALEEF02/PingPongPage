-- Dumping database structure for ppp
CREATE DATABASE IF NOT EXISTS `ppp` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `ppp`;

-- Dumping structure for table ppp.games
CREATE TABLE IF NOT EXISTS `games` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT,
    `date` timestamp NULL DEFAULT NULL,
    `status` int(11) NOT NULL DEFAULT 1 COMMENT '0 = rejected, 1 = pending, 2 = accepted, 3 = elo calculated',
    `sender` int(11) NOT NULL,
    `receiver` int(11) NOT NULL,
    `winner` int(11) NOT NULL,
    `winner_score` int(11) NOT NULL DEFAULT 0,
    `loser_score` int(11) NOT NULL DEFAULT 0,
    `rating_cycle` int(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `status` (`status`),
    KEY `sender` (`sender`),
    KEY `receiver` (`receiver`),
    KEY `sender_receiver` (`sender`,`receiver`)
    ) ENGINE=InnoDB AUTO_INCREMENT=286 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table ppp.glicko
CREATE TABLE IF NOT EXISTS `glicko` (
                                        `id` int(11) NOT NULL AUTO_INCREMENT,
    `userId` int(11) DEFAULT 0,
    `date` timestamp NULL DEFAULT NULL,
    `rating` double NOT NULL DEFAULT 1400,
    `rd` double NOT NULL DEFAULT 350,
    `volatility` double NOT NULL DEFAULT 0.06,
    `rating_cycle` int(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `userId` (`userId`)
    ) ENGINE=InnoDB AUTO_INCREMENT=240 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table ppp.services
CREATE TABLE IF NOT EXISTS `services` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `activated` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
    ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table ppp.service_variables
CREATE TABLE IF NOT EXISTS `service_variables` (
                                                   `service_id` int(11) NOT NULL,
    `variable` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`service_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Data exporting was unselected.

-- Dumping structure for table ppp.users
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` int(11) NOT NULL AUTO_INCREMENT,
    `username` varchar(50) DEFAULT NULL,
    `email` varchar(50) DEFAULT NULL,
    `token` varchar(33) DEFAULT NULL,
    `token_expiry_date` timestamp NULL DEFAULT NULL,
    `rating` double NOT NULL DEFAULT 1400,
    `rd` double NOT NULL DEFAULT 350,
    `volatility` double NOT NULL DEFAULT 0.06,
    `join_date` timestamp NULL DEFAULT NULL,
    `login_date` timestamp NULL DEFAULT NULL,
    `banned` int(11) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username` (`username`)
    ) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;