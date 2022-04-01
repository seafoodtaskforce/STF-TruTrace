CREATE DATABASE  IF NOT EXISTS `wwf_shrimp_database_v2` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `wwf_shrimp_database_v2`;
-- MySQL dump 10.13  Distrib 5.7.9, for Win32 (AMD64)
--
-- Host: localhost    Database: wwf_shrimp_database_v2
-- ------------------------------------------------------
-- Server version	5.7.9-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `app_resources`
--

DROP TABLE IF EXISTS `app_resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `app_resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_key` varchar(255) DEFAULT NULL,
  `value` mediumtext,
  `locale` varchar(10) DEFAULT NULL,
  `type` varchar(100) DEFAULT 'document',
  `sub_type` varchar(100) DEFAULT 'type metadata',
  `platform` varchar(45) DEFAULT NULL,
  `description` text,
  `tags` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2101 DEFAULT CHARSET=utf8mb4 COMMENT='Resource Table for internationalized resources.\nNOTE: locale is the locale/langauge ISO code.\nIn general English (en) should always be present and should be defaulted to if other locale is not found';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `audit_data`
--

DROP TABLE IF EXISTS `audit_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audit_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `actor_name` varchar(200) NOT NULL,
  `actor_type` varchar(64) DEFAULT NULL,
  `action` varchar(200) NOT NULL,
  `item_type` varchar(200) NOT NULL,
  `item_id` varchar(200) NOT NULL,
  `field` varchar(200) DEFAULT NULL,
  `previous_value` varchar(200) DEFAULT NULL,
  `new_value` varchar(200) DEFAULT NULL,
  `timestamp` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18443 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `associated_template_id` bigint(20) DEFAULT NULL,
  `document_type_id` bigint(20) NOT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `userid` bigint(20) DEFAULT NULL,
  `username` varchar(200) CHARACTER SET utf8 NOT NULL,
  `document_type` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `creation_timestamp` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `document_image_blob` mediumblob,
  `document_image_uri` varchar(256) CHARACTER SET utf8 DEFAULT NULL,
  `type_hex_color` varchar(200) CHARACTER SET utf8 DEFAULT NULL,
  `sync_id` varchar(36) CHARACTER SET utf8 DEFAULT NULL,
  `organization_id` bigint(20) NOT NULL DEFAULT '1',
  `group_id` bigint(20) NOT NULL DEFAULT '1',
  `doc_status` varchar(45) CHARACTER SET utf8 DEFAULT 'DRAFT',
  `gps_location` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  `updation_timestamp` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  `updation_server_timestamp` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`,`document_type_id`,`username`,`group_id`,`organization_id`),
  KEY `fk_document_user1_idx` (`userid`,`username`),
  CONSTRAINT `fk_document_user1` FOREIGN KEY (`userid`, `username`) REFERENCES `user` (`id`, `name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4375 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_attachment_data`
--

DROP TABLE IF EXISTS `document_attachment_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_attachment_data` (
  `parent_doc_id` bigint(20) NOT NULL,
  `attached_doc_id` bigint(20) NOT NULL,
  PRIMARY KEY (`parent_doc_id`,`attached_doc_id`),
  KEY `fk_attached_document_child_idx` (`attached_doc_id`),
  CONSTRAINT `fk_attached_document_child` FOREIGN KEY (`attached_doc_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_attached_document_parent` FOREIGN KEY (`parent_doc_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_entity_data`
--

DROP TABLE IF EXISTS `document_entity_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_entity_data` (
  `document_id` bigint(20) NOT NULL,
  `entity_data_id` bigint(20) NOT NULL,
  PRIMARY KEY (`document_id`,`entity_data_id`),
  KEY `fk_document_entity_data_entity_data1_idx` (`entity_data_id`),
  KEY `fk_document_entity_data_document1_idx` (`document_id`),
  CONSTRAINT `fk_document_entity_data_document1` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_entity_data_entity_data1` FOREIGN KEY (`entity_data_id`) REFERENCES `entity_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_import`
--

DROP TABLE IF EXISTS `document_import`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_import` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `doc_import_id` bigint(20) NOT NULL,
  `file_data` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=798 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_link_data`
--

DROP TABLE IF EXISTS `document_link_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_link_data` (
  `parent_doc_id` bigint(20) NOT NULL,
  `linked_doc_id` bigint(20) NOT NULL,
  PRIMARY KEY (`parent_doc_id`,`linked_doc_id`),
  KEY `fk_document_child_idx` (`linked_doc_id`),
  CONSTRAINT `fk_document_child` FOREIGN KEY (`linked_doc_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_parent` FOREIGN KEY (`parent_doc_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_location_data`
--

DROP TABLE IF EXISTS `document_location_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_location_data` (
  `document_sync_id` varchar(36) NOT NULL,
  `gps_location` varchar(45) NOT NULL,
  PRIMARY KEY (`document_sync_id`,`gps_location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_notes_data`
--

DROP TABLE IF EXISTS `document_notes_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_notes_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_doc_id` bigint(20) DEFAULT NULL,
  `timestamp` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `ordinal` int(11) NOT NULL DEFAULT '0',
  `note_data` text CHARACTER SET utf8,
  `note_data_header` text CHARACTER SET utf8,
  `creator_user` varchar(64) CHARACTER SET utf8 NOT NULL,
  PRIMARY KEY (`id`,`ordinal`)
) ENGINE=InnoDB AUTO_INCREMENT=525 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_ocr_data`
--

DROP TABLE IF EXISTS `document_ocr_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_ocr_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ocr_match_text` varchar(128) DEFAULT NULL,
  `doc_type_id` varchar(45) DEFAULT NULL,
  `match_type` varchar(45) NOT NULL DEFAULT 'EXACT' COMMENT 'Possible values: "EXACT", "PREFIX", "ANYWHERE_IN_STRING", "REGEX"',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Schema for OCR data for extracting data form documents using OCR for specific text match.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_org_group_rel`
--

DROP TABLE IF EXISTS `document_org_group_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_org_group_rel` (
  `id` bigint(20) NOT NULL,
  `doc_id` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_document_idx_idx` (`doc_id`),
  KEY `fk_group_idx_idx` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_page`
--

DROP TABLE IF EXISTS `document_page`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_page` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `page_number` int(11) DEFAULT NULL,
  `page_data` mediumblob,
  `document_id` bigint(20) NOT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `page_data_uri` varchar(256) DEFAULT NULL,
  `page_data_type` varchar(200) DEFAULT NULL,
  `page_data_thumbnail` mediumblob,
  PRIMARY KEY (`id`,`document_id`),
  KEY `fk_DocumentPage_Document1_idx` (`document_id`),
  CONSTRAINT `fk_DocumentPage_Document1` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=15106 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_page_entity_data`
--

DROP TABLE IF EXISTS `document_page_entity_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_page_entity_data` (
  `document_page_id` bigint(20) NOT NULL,
  `entity_data_id` bigint(20) NOT NULL,
  PRIMARY KEY (`document_page_id`,`entity_data_id`),
  KEY `fk_document_page_entity_data_entity_data1_idx` (`entity_data_id`),
  KEY `fk_document_page_entity_data_document_page1_idx` (`document_page_id`),
  CONSTRAINT `fk_document_page_entity_data_document_page1` FOREIGN KEY (`document_page_id`) REFERENCES `document_page` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_page_entity_data_entity_data1` FOREIGN KEY (`entity_data_id`) REFERENCES `entity_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_recipient_data`
--

DROP TABLE IF EXISTS `document_recipient_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_recipient_data` (
  `parent_doc_id` bigint(20) NOT NULL,
  `to_user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`parent_doc_id`,`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_tag_data`
--

DROP TABLE IF EXISTS `document_tag_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_tag_data` (
  `document_id` bigint(20) NOT NULL,
  `tag_data_id` bigint(20) NOT NULL,
  PRIMARY KEY (`document_id`,`tag_data_id`),
  KEY `fk_document_tag_data_tag_data1_idx` (`tag_data_id`),
  KEY `fk_document_tag_data_document1_idx` (`document_id`),
  CONSTRAINT `fk_document_tag_data_document1` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_tag_data_tag_data1` FOREIGN KEY (`tag_data_id`) REFERENCES `tag_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_template`
--

DROP TABLE IF EXISTS `document_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `min_pages` int(11) DEFAULT NULL,
  `max_pages` int(11) DEFAULT NULL,
  `version` varchar(7) DEFAULT NULL,
  `document_type_id` bigint(20) NOT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`,`document_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_template_entity_data`
--

DROP TABLE IF EXISTS `document_template_entity_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_template_entity_data` (
  `document_template_id` bigint(20) NOT NULL,
  `entity_data_id` bigint(20) NOT NULL,
  PRIMARY KEY (`document_template_id`,`entity_data_id`),
  KEY `fk_document_template_entity_data_entity_data1_idx` (`entity_data_id`),
  KEY `fk_document_template_entity_data_document_template1_idx` (`document_template_id`),
  CONSTRAINT `fk_document_template_entity_data_document_template1` FOREIGN KEY (`document_template_id`) REFERENCES `document_template` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_document_template_entity_data_entity_data1` FOREIGN KEY (`entity_data_id`) REFERENCES `entity_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `document_type`
--

DROP TABLE IF EXISTS `document_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `document_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  `color_hex_code` varchar(10) DEFAULT NULL,
  `document_designation` varchar(45) DEFAULT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `is_resource` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=193 DEFAULT CHARSET=utf8 COMMENT='name - is the key to the resource name of the data\nvalue - will be ommited in future versions and will be looked up based on the key (i.e. name)';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dynamic_field_data`
--

DROP TABLE IF EXISTS `dynamic_field_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamic_field_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'The main id',
  `parent_doc_id` bigint(20) NOT NULL COMMENT 'The docuemnt to which this is attached',
  `parent_dynamic_field_id` bigint(20) NOT NULL COMMENT 'The parent field defintion to which this is the data for',
  `value` varchar(1000) DEFAULT NULL COMMENT 'The actual value stored in the field',
  PRIMARY KEY (`id`,`parent_doc_id`,`parent_dynamic_field_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4438 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dynamic_field_def`
--

DROP TABLE IF EXISTS `dynamic_field_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamic_field_def` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `org_id` bigint(20) NOT NULL,
  `document_type_id` bigint(20) NOT NULL,
  `display_name` varchar(45) NOT NULL,
  `description` varchar(200) NOT NULL,
  `field_type_id` bigint(20) NOT NULL,
  `max_length` int(11) NOT NULL DEFAULT '20',
  `is_required` tinyint(1) DEFAULT '1',
  `ordinal` int(11) NOT NULL,
  `is_doc_id` tinyint(1) DEFAULT '0',
  `ocr_match_text` varchar(128) DEFAULT NULL,
  `ocr_match_length` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`org_id`,`document_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=169 DEFAULT CHARSET=utf8mb4 COMMENT='This is the table to store synami field data definition';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dynamic_field_type`
--

DROP TABLE IF EXISTS `dynamic_field_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dynamic_field_type` (
  `id` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  `mask` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `entity_data`
--

DROP TABLE IF EXISTS `entity_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entity_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `value` blob,
  `data_type` varchar(264) DEFAULT NULL,
  `is_required` tinyint(1) DEFAULT '0',
  `delete_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_allowed_doctype_rel`
--

DROP TABLE IF EXISTS `group_allowed_doctype_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_allowed_doctype_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_group_type_id` bigint(20) DEFAULT NULL,
  `doc_type_ids` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=158 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_data`
--

DROP TABLE IF EXISTS `group_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `group_data_type_id` bigint(20) NOT NULL,
  `business_id_number` varchar(45) DEFAULT NULL,
  `legal_business_name` varchar(45) DEFAULT NULL,
  `business_address` varchar(100) DEFAULT NULL,
  `gps_location` varchar(45) DEFAULT NULL,
  `email_address` varchar(200) DEFAULT NULL,
  `verified` tinyint(1) DEFAULT '0',
  `activated` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`,`group_data_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=310 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_data_type`
--

DROP TABLE IF EXISTS `group_data_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_data_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  `color_hex_code` varchar(10) DEFAULT NULL,
  `order_index` int(11) NOT NULL DEFAULT '0',
  `org_id` bigint(20) NOT NULL DEFAULT '0',
  `value_id` bigint(20) DEFAULT '0',
  `is_resource` tinyint(1) DEFAULT '1',
  `associated_groups` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=353 DEFAULT CHARSET=utf8 COMMENT='name - is the key to the resource name of the data\nvalue - will be ommited in future versions and will be looked up based on the key (i.e. name)\nvalue_id - will be used to fetch internationalized values for the actual name.';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_group_rel_tree`
--

DROP TABLE IF EXISTS `group_group_rel_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_group_rel_tree` (
  `id` bigint(20) NOT NULL,
  `parent_group_id` bigint(20) DEFAULT NULL,
  `child_group_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_group_child_idx` (`child_group_id`),
  CONSTRAINT `fk_group_child` FOREIGN KEY (`child_group_id`) REFERENCES `group_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_role`
--

DROP TABLE IF EXISTS `group_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `group_role` (
  `group_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`group_id`,`role_id`),
  KEY `fk_group_role_role1_idx` (`role_id`),
  KEY `fk_group_role_group1_idx` (`group_id`),
  CONSTRAINT `fk_group_role_group1` FOREIGN KEY (`group_id`) REFERENCES `group_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_group_role_role1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metadata`
--

DROP TABLE IF EXISTS `metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `text_value` varchar(200) DEFAULT NULL,
  `long_text_value` longtext,
  `object_value` longblob,
  `metadata_type_id` bigint(20) NOT NULL,
  `document_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`,`metadata_type_id`,`document_id`),
  KEY `fk_Metadata_MetadataType1_idx` (`metadata_type_id`),
  KEY `fk_Metadata_Document1_idx` (`document_id`),
  CONSTRAINT `fk_Metadata_Document1` FOREIGN KEY (`document_id`) REFERENCES `document` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Metadata_MetadataType1` FOREIGN KEY (`metadata_type_id`) REFERENCES `metadata_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `metadata_type`
--

DROP TABLE IF EXISTS `metadata_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadata_type` (
  `id` bigint(20) NOT NULL,
  `name` varchar(25) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userid` bigint(20) DEFAULT NULL,
  `notification_type` varchar(200) DEFAULT NULL,
  `creation_timestamp` varchar(64) DEFAULT NULL,
  `notification_timestamp` varchar(64) DEFAULT NULL,
  `auditId` bigint(20) DEFAULT NULL,
  `text` varchar(256) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3892 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) DEFAULT NULL,
  `description` varchar(1024) DEFAULT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization_group_rel_tree`
--

DROP TABLE IF EXISTS `organization_group_rel_tree`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `organization_group_rel_tree` (
  `parent_org_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`parent_org_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `permission_type` varchar(10) NOT NULL,
  `permission_scope_type` varchar(15) DEFAULT NULL,
  `expiry_date` datetime DEFAULT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`,`permission_type`,`resource_id`),
  KEY `fk_Permission_Resource1_idx` (`resource_id`),
  CONSTRAINT `fk_Permission_Resource1` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource`
--

DROP TABLE IF EXISTS `resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource` (
  `Id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_object_id` bigint(20) NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  `resource_type_id` bigint(20) NOT NULL,
  PRIMARY KEY (`Id`,`resource_object_id`,`resource_type_id`),
  KEY `fk_resource_resource_type1_idx` (`resource_type_id`),
  CONSTRAINT `fk_resource_resource_type1` FOREIGN KEY (`resource_type_id`) REFERENCES `resource_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `resource_type`
--

DROP TABLE IF EXISTS `resource_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `resource_type` (
  `id` bigint(20) NOT NULL,
  `name` varchar(25) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(25) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_permission`
--

DROP TABLE IF EXISTS `role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_permission` (
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `fk_role_permission_permission1_idx` (`permission_id`),
  KEY `fk_role_permission_role1_idx` (`role_id`),
  CONSTRAINT `fk_role_permission_permission1` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_role_permission_role1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_resource`
--

DROP TABLE IF EXISTS `role_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role_resource` (
  `role_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  `resource_type` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`role_id`,`resource_id`),
  KEY `fk_role_resource_resource1_idx` (`resource_id`),
  KEY `fk_role_resource_role1_idx` (`role_id`),
  CONSTRAINT `fk_role_resource_resource1` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_role_resource_role1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `screening_data`
--

DROP TABLE IF EXISTS `screening_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `screening_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `test_date` varchar(64) DEFAULT NULL,
  `health_score` int(11) NOT NULL DEFAULT '0',
  `floor_number` varchar(10) DEFAULT NULL,
  `station_id` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `security_token`
--

DROP TABLE IF EXISTS `security_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `security_token` (
  `user_id` bigint(20) NOT NULL,
  `token_value` varchar(256) NOT NULL,
  `expiration_date` datetime NOT NULL,
  `invalidated` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`user_id`,`token_value`),
  KEY `fk_security_token_user1_idx` (`user_id`),
  CONSTRAINT `fk_security_token_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supported_languages`
--

DROP TABLE IF EXISTS `supported_languages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `supported_languages` (
  `id` bigint(20) NOT NULL,
  `name` varchar(5) NOT NULL,
  `value` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='name - the iso code for the language\nvalue - the full name of the supported language';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supported_languages_org_rel`
--

DROP TABLE IF EXISTS `supported_languages_org_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `supported_languages_org_rel` (
  `org_id` int(20) NOT NULL,
  `language_id` int(20) NOT NULL,
  PRIMARY KEY (`org_id`,`language_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_data`
--

DROP TABLE IF EXISTS `tag_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tag_text` varchar(200) NOT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `custom_tag_prefix` varchar(45) DEFAULT NULL,
  `organization_id` bigint(20) DEFAULT NULL,
  `custom` tinyint(1) DEFAULT '0',
  `owner` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=500 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_prefix_lu`
--

DROP TABLE IF EXISTS `tag_prefix_lu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tag_prefix_lu` (
  `id` bigint(20) NOT NULL,
  `name` varchar(45) NOT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trace_data`
--

DROP TABLE IF EXISTS `trace_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trace_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `global_transaction_id` bigint(20) DEFAULT NULL,
  `trace_type_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`,`trace_type_id`,`resource_id`),
  KEY `fk_trace_Data_trace_type1_idx` (`trace_type_id`),
  KEY `fk_trace_Data_resource1_idx` (`resource_id`),
  CONSTRAINT `fk_trace_Data_resource1` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_trace_Data_trace_type1` FOREIGN KEY (`trace_type_id`) REFERENCES `trace_type` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `trace_type`
--

DROP TABLE IF EXISTS `trace_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trace_type` (
  `id` bigint(20) NOT NULL,
  `name` varchar(25) DEFAULT NULL,
  `value` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `password` varchar(64) NOT NULL,
  `delete_flag` tinyint(1) DEFAULT '0',
  `profile_image` longblob,
  PRIMARY KEY (`id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=506 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_contact`
--

DROP TABLE IF EXISTS `user_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_contact` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `email_address` varchar(200) DEFAULT NULL,
  `cell_number` varchar(15) DEFAULT NULL,
  `user_id` bigint(20) NOT NULL,
  `first_name` varchar(45) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `nick_name` varchar(45) DEFAULT NULL,
  `line_id` varchar(45) DEFAULT NULL,
  `activated` tinyint(1) DEFAULT '0',
  `verified` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`,`user_id`),
  KEY `fk_user_contact_user1_idx` (`user_id`),
  CONSTRAINT `fk_user_contact_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=505 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_group`
--

DROP TABLE IF EXISTS `user_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_group` (
  `user_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`group_id`),
  KEY `fk_user_group_group1_idx` (`group_id`),
  KEY `fk_user_group_user1_idx` (`user_id`),
  CONSTRAINT `fk_user_group_group1` FOREIGN KEY (`group_id`) REFERENCES `group_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_group_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_organization`
--

DROP TABLE IF EXISTS `user_organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_organization` (
  `user_id` bigint(20) NOT NULL,
  `organization_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`organization_id`),
  KEY `fk_user_organization_organization1_idx` (`organization_id`),
  KEY `fk_user_organization_user1_idx` (`user_id`),
  CONSTRAINT `fk_user_organization_organization1` FOREIGN KEY (`organization_id`) REFERENCES `organization` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_organization_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_role_role1_idx` (`role_id`),
  KEY `fk_user_role_user1_idx` (`user_id`),
  CONSTRAINT `fk_user_role_role1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_role_user1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-03-31 22:46:51
