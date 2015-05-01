-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.26-community-nt


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema glycopeakfinder
--

CREATE DATABASE IF NOT EXISTS glycopeakfinder;
USE glycopeakfinder;

--
-- Definition of table `ions`
--

DROP TABLE IF EXISTS `ions`;
CREATE TABLE `ions` (
  `ions_id` int(10) unsigned NOT NULL auto_increment,
  `order` int(10) unsigned NOT NULL,
  `name` varchar(100) NOT NULL,
  `formula` varchar(45) NOT NULL,
  `signum` int(10) unsigned NOT NULL,
  `mass_mono` double NOT NULL,
  `mass_avg` double NOT NULL,
  `charge` int(10) unsigned NOT NULL,
  `exchange` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`ions_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='charged ions';

--
-- Dumping data for table `ions`
--

/*!40000 ALTER TABLE `ions` DISABLE KEYS */;
INSERT INTO `ions` (`ions_id`,`order`,`name`,`formula`,`signum`,`mass_mono`,`mass_avg`,`charge`,`exchange`) VALUES 
 (1,100,'Sodium','Na+',1,22.98976967,22.98976967,1,1),
 (2,200,'Potassium','K+',1,38.9637069,39.098301,1,1),
 (3,300,'Lithium','Li+',1,6.0151223,6.940037,1,1),
 (4,1,'Hydrogen','H+',1,1.007825032,1.007941,1,0),
 (5,1000,'Hydrogen','-H+',0,1.007825032,1.007941,1,0);
/*!40000 ALTER TABLE `ions` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
