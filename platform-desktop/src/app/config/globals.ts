'use strict'
import { Document } from '../models/document';

/**
 * Application Related data
 */
export const APPLICATION_VERSION = 'v0.95.84 (optimized candidate 008)';

/**
 * REST Server paths
 */
// remote
export const SERVER_URI_REMOTE = 'http://204.236.203.207:8080/WWFShrimpProject_v2/api_v2';
export const SERVER_URI_REMOTE_PILOT = 'http://3.86.84.130:8080/WWFShrimpProject_v2/api_v2';
export const SERVER_URI_REMOTE_PILOT_AZURE = 'http://40.114.33.194:8080/WWFShrimpProject_v2/api_v2';
// local
export const SERVER_URI_LOCAL_DEV = 'http://localhost:8080/WWFShrimpProject/api_v2';
export const SERVER_URI = SERVER_URI_REMOTE_PILOT_AZURE;


/**
 * Tag formatting
 */
export const FORMATTING_DELIMITER = ';;;;';
export const CUSTOM_TAG_PREFIX = 'CUSTOM:';

/**
 * Doc Infor Form Formatting
 */
export const DOC_INFO_DATA_FORMATTING_DELIMITER = '~@@@~';
export const DOC_INFO_DATA_FORMATTING_DELIMITER_SPLIT_STRING = '~;~';
export const DOC_INFO_DATA_FORMATTING_TRANSPORT_STRING = '{id}~@@@~{data}';



/**
 * Paths for Routing
 */
export const PAGES_ROUTE = 'pages';
export const LOGIN_PAGE_ROUTE = 'login';
export const DOCUMENTS_PAGE_ROUTE = 'documents';
export const PROFILE_PAGE_ROUTE = 'profile';
export const HOME_PAGE_ROUTE = 'home';
export const ADMIN_PAGE_ROUTE = 'admin';
export const REGISTER_PAGE_ROUTE = 'register';
export const TRACE_MAP_ROUTE = 'register';

/**
 * General Roles
 */

export const ROLE_GENERAL_USER = 6;
export const ROLE_SUPER_ADMIN = 1;
export const ROLE_ADMIN = 2;
export const ROLE_ORG_ADMIN = 7;
export const ROLE_MATRIX_ADMIN = 8;

/**
 * Notifications
 */

export const EMITTER_SEED_VALUE  = new Document();
