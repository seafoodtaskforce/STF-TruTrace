import { Injectable } from '@angular/core';
import { Http, Response, Headers, RequestOptions, ResponseContentType } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import { Document } from '../../models/document';
import { DocumentTag } from '../../models/documentTag';
import { User } from '../../models/user';
import { Group } from '../../models/group';
import { DocumentType } from '../../models/documentType';
import { SlimLoadingBarService } from 'ng2-slim-loading-bar';



import { ScrollToService, ScrollToConfigOptions } from '@nicky-lenaers/ngx-scroll-to';
import { NoteData } from '../../models/noteData';
import { DocumentsComponent } from './documents.component';


// import global data
import * as AppGlobals from '../../config/globals';
import { LocaleUtils } from 'app/utils/locale.utils';
import { ServerUtils } from 'app/utils/server.utils';
import { DynamicFieldDefinition } from 'app/models/dynamicFieldDefinition';
import { DynamicFieldData } from 'app/models/dynamicFieldData';
import { ApplicationErrorData } from 'app/models/applicationErrorData';
import { Page } from 'app/models/page';
import { UserAdminTable } from '../tables/components/smartTables/userAdmin/userAdmin.component';


@Injectable()
export class DocumentService {

  /**
   * RESTFUL URL Definitions
   */

  //
  // Docs 
  readonly ALL_DOCS_BY_TAG_URL: string = '/document/fetchallbytag';
  //readonly UPLOAD_DOC_AS_PDF_URL: string = '/document/upload';
  readonly UPLOAD_DOC_AS_PDF_URL: string = '/document/files';
  readonly EXPORT_TRACE_AS_PDF_URL: string = '/document/traceexport?doc_id=[:1]';
  readonly EXPORT_TRACE_GPS_DATA_URL: string = '/document/tracegpsexport?doc_id=[:1]';
  readonly ALL_USER_RECIPIENTS_URL: string = '/document/fetchrecipientsforuser';
  readonly ALL_DOC_URL: string = '/document/fetchall_v2';
  readonly ALL_TRACE_DOC_URL: string = '/document/fetchtracedataexport_v2';
  readonly DELETE_DOC_URL: string = '/document/delete/[:1]';
  readonly MARK_DOC_AS_READ_URL: string = '/document/markread?doc_id=[:1]&user_name=[:2]';
  readonly DOCUMENT_TRACE_BY_ID_URL: string = '/document/docbyid?doc_id=[::]&recursive=true';
  readonly ALL_DOC_TYPES_URL: string = '/document/alltypes';
  readonly SET_DOC_STATUS_URL: string = '/document/status?doc_id=[:1]&user_name=[:2]&doc_status=[:3]&action_timestamp=[:4]';
  readonly RECALL_DOC_URL: string = '/document/recall?doc_id=[:1]&user_name=[:2]';
  readonly ADD_DOC_NOTES_URL: string = '/document/addnotes';
  readonly DELETE_DOC_PAGES_URL: string = '/document/delete/pages/[:1]';

  //
  // Users
  readonly ALL_USER_URL: string = '/user/fetchall';


  //
  // Tags
  readonly ALL_TAGS_URL: string = '/tag/fetchall';

  //
  // Orgs
  readonly GROUPS_BY_ORGANIZATION_ID_URL: string = '/organization/fetchflat?org_id=[::]';

  

  

  //
  // 
  documentIcons = [
    {type: 'Bill of Lading', icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABcklEQVRoQ+2Z/W3CMBDFn5sBYIMyQoDs0Y4AG7AJbEBHgD0SyAjtBC0DREYWUJWKYPt8sRPp8jcX3++9+0CxwsAfNfD8IQCpHXRzID9OkDVraORQmAQlrfGBQ7EMesefYDuASf6lOUJhzHUoGCHsAPNyB+CNLfnbi5gg7ACz8ptFfa2WUHp7JwQDhB1gXmoW9atCYVYtuCHiAhglmCHiAzBDpAFghEgHwASRFoABIj1AIEQ/AAIg+gPQBmH2x5OnXwCPIHoDQF3nAsD1X0gceKxAvCYWB7gdsEwHb8Hbeq2zKSQA/zwSB9qKVvaAdzvfB0gTE5vLW3fiOfRNLGM09Rj1rhFiQGdNTMzHO6wzANceCN0jAkAcb7+lIg6Ef1b5ATDybj6OAI0vHIqnd3Iui6ybKyY3wD2q4j3sw9blhrJO4MIJTZajnn6GAZjoC8Tmes366iYe8VembBRqNNnKlrw5wV5CxDxihQlALKXbzhm8A2fOK/Axnp6lCwAAAABJRU5ErkJggg=='},
    {type: 'Vessel documents', icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABlElEQVRoQ+2ZQU7DMBBF/0gUiU3KDegRehNgEdb0BtwEbgDrZgGchB4BTgDJBolUMnJEJZCaTjwe24lw1/bMvP89dtshTPxDE68fGSC1g4Mc+HzEom1xa4AlERZeRRs8FFdYecX4tZkFsMV/bfFCwKlWUihCsABNhScA52rF7wIpQbAAdYV3FfUNViDc/xFCAYIFaCoYDfWLEtSsca0NERXACqENER1AGyIJgCZEMgAtiKQAGhDJAXwhRgHgAzEagD4I+34ceodGBbAPYjQA0tc8A2h9F8oO9CgQrYmzA9oOcLeDq+J9vcblER8hLnAG+FGAE+r/OuB6RKTrgzkgLch1XzAALrBrofkW6lNMqkx2IPQ16qqwdD3Xa+J3QFqQ675gAFzgXaG+vye4PGIHuMAZIHQTT8aBeo0PIsxdm09jvQHe5uXhmdyQHggzYhpG+FyUuPD6Y6sb8rXYxHbBGNTHMyxPLvHqBWA3d2PWLe66MStwNkw82Sp7bAjYzI5wwxVvM7BHSFZGvF0ZIJ7W+zNN3oFvn5jpMZgHr4AAAAAASUVORK5CYII='},
    {type: 'Farm Documents', icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABaUlEQVRoQ+2Z0U3FMAxFTyaADWAENoERYAM2gQ1gBJgERuBNAExQFMiTnhB9TmynaYXz1Y/E8bk3qas6sfGRNp4/ATDawSoHJjgH7oALfp4t4zHBjSXA4VoRoCT/Apx6bQq4QdQAPAGXjsnvQ7lA1AC8O6mfj83DLyHMEDUAk4f6CdIE194QiwJkIbwhFgfwhhgC4AkxDMALYiiAB8RwACvEKgAsEKsBmIPI9eNYHVoVwF8QqwHQVvMAmL6r/7gRDoQDxtPX7QhJgVvznnNa2kddB6TAAVAUkIT6vw60HhHt/G4OaBNqXdcNQArcmmi8heYU0yoTDvR+jbYqrJ0v3TV1HdAm1LquG4AUeJ+o9WtW2kftgBQ4AHpf4i058AGctF4+p/m7JPTkau5ArxZTDeNzgivrj63clXwd4MJn7oomeDMBlL9lGeK+tFnPaqQzzNkVwW6l5PMe4hEyJLLI0gBYROYjm2zegS9iPpAxXlxxwgAAAABJRU5ErkJggg=='},
    {type: 'Feed Documents', icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABb0lEQVRoQ+2Z4W0CMQyFnyegG9AR2KQdoWzQTdoNYIQyCYwAExQmSJUqSAhx58R2kjvh+8WPxPH3XpxwZ8LMH5p5/nCA3g5mORACXgF8AVgB/781z5YIa02A27ksQEp+D+DFalEAZhA5AD8A3gyTv4YygcgB+DVSP26bzZ0QaogcgGChPhEoBHxYQzQFiEJYQzQHsIboAmAJ0Q3ACqIrgAVEdwAtxCQANBCTARiCiPfH2D00KYBHEJMBkN7mDhACTP4LuQMDCjQrYnfA2gHudChVfKjWuHXEW4gL7ABJAU6o53WgdItIx1dzQJpQ6bxqAFzg0kT9FBpSTKqMO1D7GC1VWDqeqzXxPSBNqHReNQAu8DVR7fsEt47YAS6wA9Qu4jk5cAawKC0+o/EnovGeXE4N1Gox5TDuiPCu/bAVu5KHDi5cYleUCEcVQPpaFiG+U5t1mSOdYswpCfbJJR/XYLeQIpEmUx2gicwji8zegT8jGs4xL4njsgAAAABJRU5ErkJggg=='},
    {type: 'Fishmeal Documents', icon: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAABlElEQVRoQ+2Z0U3DMBCG/5PavsIGTTboCN4ARqAbsAlsACPABtmAbpAwAfCcSEaGVgKJ9Ozz2UmE82zf3ff/PjuJCQt/aOH1owBM7aCXA62xFWG4A+wOoCqmaIJ93DabfUyMn3NZgGPxLwAu9ZLqQXgA9E8EXGkVf4qj5QQL0Jn+TUN9C9oT7MNv++Od8AGwGupXzZpaM9xoQ2QFcEJoQ2QH0IaYBEATYjIALYhJATQgJgeIhZgFQAzEbADGINz5ce4cmhXAXxCzAZCe5gWgM73Ku1BxYESBbE1cHNB2gNsdQhUf6zUuj3gJcYELwFEBTqj/60DoEpGOT+aAtKDQeckAuMChhZZdaEwxqTLFgdTbaKjC0vFcr4nPAWlBofOSAXCBT4XGfk9wecQOcIELQOomXowDrenfCbgIbT6N8Rb2tW42Z+/k2B5oTZorJh9ACzzXzfo66seWu+QDhkNuFyzwAax2dUNdFMD337IviHt3zUqgrY960jFu2QB0AFa3XPEuB7uEpIXkmlcAcik9lmfxDnwCkz8wQOP7hdEAAAAASUVORK5CYII='},
  ];

  //
  // Internationalization mappings
  //
  //


  //
  // English
  internationalize_en = [
    {name: 'doc_feed_[Document_Feed]', content: 'Document Feed'},
    {name: 'doc_feed_[pages]', content: 'pages'},
    {name: 'doc_feed_[Trace]', content: 'Trace'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'Viewing: My Docs'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'Viewing: All Docs'},

    {name: 'filter_[Sorting By Date]', content: 'Sorting By Date'},
    {name: 'filter_[Usernames]', content: 'User Names'},
    {name: 'filter_[Doc Types]', content: 'Doc Types'},
    {name: 'filter_[Documents]', content: 'Documents'},
    {name: 'filter_[Lot # Search]', content: 'Tag Search'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'Reset Filter'},
    {name: 'filter_[Newest]', content: 'Newest'},
    {name: 'filter_[Oldest]', content: 'Oldest'},

    //
    // Not mapped to new 
    {name: 'filter_[Partial Match]', content: 'Partial Match'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'Search'},
    {name: 'filter_[Close Lot # Search]', content: 'Close Document Tag Search'},
    {name: 'filter_[Lot # 1]', content: 'Doc Info'},
    {name: 'filter_[Lot # 2]', content: 'Doc Info'},
    {name: 'filter_[Lot # 3]', content: 'Doc Info'},
    {name: 'filter_[Lot # 4]', content: 'Doc Info'},
    {name: 'filter_[Lot # 5]', content: 'Doc Info'},
    {name: 'filter_[Lot # 6]', content: 'Doc Info'},
    {name: 'filter_[Lot # 7]', content: 'Doc Info'},
    {name: 'filter_[Lot # 8]', content: 'Doc Info'},
    {name: 'filter_[Lot # 9]', content: 'Doc Info'},
    {name: 'filter_[Lot # 10]', content: 'Doc Info'},
    {name: 'filter_[Lot 1]', content: 'Doc Info'},
    {name: 'filter_[Lot 2]', content: 'Doc Info'},
    {name: 'filter_[Lot 3]', content: 'Doc Info'},
    {name: 'filter_[Lot 4]', content: 'Doc Info'},
    {name: 'filter_[Lot 5]', content: 'Doc Info'},
    {name: 'filter_[Lot 6]', content: 'Doc Info'},
    {name: 'filter_[Lot 7]', content: 'Doc Info'},
    {name: 'filter_[Lot 8]', content: 'Doc Info'},
    {name: 'filter_[Lot 9]', content: 'Doc Info'},
    {name: 'filter_[Lot 10]', content: 'Doc Info'},
    {name: 'filter_[Lot # Search Close]', content: 'Close Tag Search'},
    {name: 'filter_[Lot # Search Add Row]', content: 'Add Tag Row'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},
    // end

    {name: 'doc_details_[Document Details]', content: 'Document Details'},
    {name: 'doc_details_[owner]', content: 'owner'},
    {name: 'doc_details_[created]', content: 'creation date'},
    {name: 'doc_details_[location]', content: 'location'},
    {name: 'doc_details_[organization]', content: 'organization'},
    {name: 'doc_details_[linked docs]', content: 'linked docs'},
    {name: 'doc_details_[Doc Pages]', content: 'Doc Pages'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'Doc Pages'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'recipients'},

    {name: 'doc_details_tabs[Linked Docs]', content: 'Linked Docs'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'Back-up Docs'},
    {name: 'doc_details_tabs[Document Tags]', content: 'Document Tags'},
    {name: 'doc_details_tabs[Document Fields]', content: 'Document Info'},
    //
    // Not mapped to new 
    {name: 'doc_details_tabs[Document Notes]', content: 'Document Notes'},
    // end

    {name: 'doc_details_tabs[Document Type]', content: 'Document Type'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'Linked Documents'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'Backup Documents'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'Tags for this document'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'Document Info'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'Notes for this document'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'Document Type'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'Owner'},
    {name: 'doc_details_tabs_content[Organization]', content: 'Organization'},
    {name: 'doc_details_tabs_content[Created]', content: 'Created'},
    {name: 'doc_details_tabs_content[Pages]', content: 'Pages'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'Page'}, 

    {name: 'doc_trace[Details]', content: 'Trace Details'},
    {name: 'doc_trace_close[Close]', content: 'Close'},
    {name: 'doc_trace_show_all[Show]', content: 'Show All Docs'},

    // Tag Management
    {name: 'tag_[Search]', content: 'Search'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'Create New Doc'},
    {name: 'edit_[existing_doc]', content: 'Edit Document'},

    {name: 'user_[document_types]', content: 'Document Type'},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},

    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'Accept Document'}, 
    {name: 'recall_document[close_dialog]', content: 'Recall'},
    {name: 'submit_document[close_dialog]', content: 'Submit'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'Are you sure you want to ACCEPT this document?'},
    {name: 'share_app[close_dialog]', content: 'Cancel'},
    {name: 'confirm_title[close_dialog]', content: 'Confirm'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'Reasons for Rejection'},
    {name: 'document_notes_header[rejection_dialog]', content: 'Rejection Header'},
    {name: 'document_notes_details[rejection_dialog]', content: 'Rejection Details'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'Write your rejection comments...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'Cancel'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'Save and Reject'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'ACCEPT'}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'REJECT'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'RECALL'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'SUBMIT'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'SAVE'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'CANCEL'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'DELETE'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'REJECTED'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'ACCEPTED'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'PENDING'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'SUBMITTED'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'DRAFT'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'RESUBMITTED'}, 

    //
    // Doc Import

    {name: 'document_upload_upload_files_list_number', content: 'Files to Upload'}, 
    {name: 'document_upload_Tags_Number', content: 'Tags'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'Recipients'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'Linked Docs'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'Backup Docs'}, 
    {name: 'document_upload_upload_button', content: 'Upload'}, 
    {name: 'document_upload_Upload_Header', content: 'IMPORT PDF DOCUMENT'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'Document Info'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'Newest'}, 
    {name: 'document_filter_sort_oldest', content: 'Oldest'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- All Users'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- All Doc Types'}, 
    {name: 'document_import_choose_doc_type', content: '-- Choose Doc Type'},
    {name: 'document_import_choose_group', content: '-- Choose Organization'}, 
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'csv_batch_user_import_choose_doc_type', content: '-- Choose Doc Type'}, 
    {name: 'document_search_tag_to_link', content: 'Enter Tag'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'The following documents match your search tag'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'No documents'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'Auto Translate'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'Lock Stages'},
    {name: 'stage.display.name', content: 'Stage Name'},

    // Document Preview
    {name: 'document.preview.pages', content: 'Disable Page Preview'},
    {name: 'document.preview.pages.header', content: 'Page Preview'}, 
    {name: 'document.preview.pages.doc.type.linked', content: 'Linked Documents'},
    {name: 'document.preview.pages.doc.type.backup', content: 'Backup Documents'},

    // Other
    {name: 'export_gps_data_button_label', content: 'Data Export'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'Export Trace'},
    {name: 'manage_pdf_document_label_doc_type', content: 'Document Type'},
    {name: 'manage_pdf_document_button_save', content: 'Save'},
    {name: 'manage_pdf_document_button_submit', content: 'Submit'},
    {name: 'manage_pdf_document_button_cancel', content: 'Cancel'},
    {name: 'manage_pdf_document_button_choose_file', content: 'Choose File'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'No File Chosen'},
    {name: 'manage_pdf_document_tags_button_search', content: 'Search'},
    {name: 'manage_pdf_document_tags_button_new', content: 'New'},
    {name: 'manage_pdf_document_label_uploading', content: 'Uploading...'},

  ];

  //
  // Indonesian
  internationalize_in = [
    {name: 'doc_feed_[Document_Feed]', content: 'Konten Dokumen'},
    {name: 'doc_feed_[pages]', content: 'Halaman'},
    {name: 'doc_feed_[Trace]', content: 'Jejak'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'Menampilkan: Dokumen Saya'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'Menampilkan: Semua Dok'},

    {name: 'filter_[Sorting By Date]', content: 'Mengurutkan Berdasarkan Tanggal'},
    {name: 'filter_[Usernames]', content: 'Nama Pengguna'},
    {name: 'filter_[Doc Types]', content: 'Jenis Dok'},
    {name: 'filter_[Documents]', content: 'Dokumen'},
    {name: 'filter_[Lot # Search]', content: 'Pencarian Tanda'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'Atur Ulang Penyaringan'},
    {name: 'filter_[Newest]', content: 'Terbaru'},
    {name: 'filter_[Oldest]', content: 'Terlama'},
    {name: 'filter_[Partial Match]', content: 'Pencocokan Parsial'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'Pencarian'},
    {name: 'filter_[Close Lot # Search]', content: 'Tutup Pencarian Tanda'},
    {name: 'filter_[Lot # 1]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 2]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 3]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 4]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 5]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 6]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 7]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 8]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 9]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # 10]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 1]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 2]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 3]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 4]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 5]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 6]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 7]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 8]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 9]', content: 'Masukan tanda'},
    {name: 'filter_[Lot 10]', content: 'Masukan tanda'},
    {name: 'filter_[Lot # Search Close]', content: 'Tutup Pencarian Tanda'},
    {name: 'filter_[Lot # Search Add Row]', content: 'Tambahkan Baris Tag'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'Rincian Dokumen'},
    {name: 'doc_details_[owner]', content: 'Pemilik'},
    {name: 'doc_details_[created]', content: 'Tanggal pembuatan'},
    {name: 'doc_details_[location]', content: 'Lokasi'},
    {name: 'doc_details_[organization]', content: 'Organisasi'},
    {name: 'doc_details_[linked docs]', content: 'Dok Tertaut'},
    {name: 'doc_details_[Doc Pages]', content: 'Halaman Dok'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'Halaman Dok'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'Penerima'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'Dok Tertaut'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'Cadangkan dok'},
    {name: 'doc_details_tabs[Document Tags]', content: 'Tandai Dokumen'},
    {name: 'doc_details_tabs[Document Fields]', content: 'Document Info'},
    {name: 'doc_details_tabs[Document Notes]', content: 'Catatan Dokumen'},
    {name: 'doc_details_tabs[Document Type]', content: 'Jenis Dokumen'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'Dokumen Tertaut'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'Data Cadangan'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'Tanda untuk Dokumen ini'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'Document Info'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'Catatan untuk Dokumen ini'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'Jenis Dokumen'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'Pemilik'},
    {name: 'doc_details_tabs_content[Organization]', content: 'Organisasi'},
    {name: 'doc_details_tabs_content[Created]', content: 'Dibuat'},
    {name: 'doc_details_tabs_content[Pages]', content: 'Halaman'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'Laman'}, 

    {name: 'doc_trace[Details]', content: 'Perincian Pelacakan'},
    {name: 'doc_trace_close[Close]', content: 'Tutup'},
    {name: 'doc_trace_show_all[Show]', content: 'Tampilkan Semua Dokumen'},

    // Tag Management
    {name: 'tag_[Search]', content: ' Pencarian'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'Impor Dok PDF'},
    {name: 'edit_[existing_doc]', content: 'Edit Document'},

    {name: 'user_[document_types]', content: 'Jenis Dokumen'},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},

    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'Terima dokumen'}, 
    {name: 'recall_document[close_dialog]', content: 'Penarikan'},
    {name: 'submit_document[close_dialog]', content: 'Menyerahkan'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'Apakah Anda yakin ingin MENERIMA dokumen ini?'},
    {name: 'share_app[close_dialog]', content: 'Batalkan'},
    {name: 'confirm_title[close_dialog]', content: 'Konfirmasi'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'Alasan Penolakan'},
    {name: 'document_notes_header[rejection_dialog]', content: 'Kop Penolakan'},
    {name: 'document_notes_details[rejection_dialog]', content: 'Perincian Penolakan'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'Tulis komentar penolakan Anda…'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'Batalkan'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'Simpan dan Tolak'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'Menerima'}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'Menolak'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'Penarikan'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'Menyerahkan'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'SAVE'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'CANCEL'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'DELETE'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'DITOLAK'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'DITERIMA'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'TERTUNDA'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'DIAJUKAN'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'KONSEP'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'DIKIRIM KEMBALI'}, 

    //
    // Doc Import
    {name: 'document_upload_upload_files_list_number', content: 'Files to Upload'}, 
    {name: 'document_upload_Tags_Number', content: 'Tandai'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'Penerima'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'Dok Tertaut'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'Dok terlampir'}, 
    {name: 'document_upload_upload_button', content: 'Unggah'}, 
    {name: 'document_upload_Upload_Header', content: 'Impor Dokumen PDF'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'Document Info'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'Terbaru'}, 
    {name: 'document_filter_sort_oldest', content: 'Terlama'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- Seluruh Pengguna'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- Semua Jenis Dok'}, 
    {name: 'document_import_choose_doc_type', content: '-- Pilih Jenis Dok'},
    {name: 'document_import_choose_group', content: '-- Choose Organization'},  
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'document_search_tag_to_link', content: 'Masukan tanda'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'Dokumen-dokumen berikut cocok dengan tag pencarian Anda'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'Tidak ada dokumen'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'Auto Translate'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'Lock Stages'},
    {name: 'stage.display.name', content: 'Stage Name'},

    // Document Preview
    {name: 'document.preview.pages', content: 'Matikan Pratinjau Halaman'},
    {name: 'document.preview.pages.header', content: 'Pratinjau Halaman'}, 
    {name: 'document.preview.pages.doc.type.linked', content: 'Dokumen Tertaut'},
    {name: 'document.preview.pages.doc.type.backup', content: 'Dokumen Cadangan'},

    // Other
    {name: 'export_gps_data_button_label', content: 'Data Export'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'Export Trace'},
    {name: 'manage_pdf_document_label_doc_type', content: 'Document Type'},
    {name: 'manage_pdf_document_button_save', content: 'Save'},
    {name: 'manage_pdf_document_button_submit', content: 'Submit'},
    {name: 'manage_pdf_document_button_cancel', content: 'Cancel'},
    {name: 'manage_pdf_document_button_choose_file', content: 'Choose File'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'No File Chosen'},
    {name: 'manage_pdf_document_tags_button_search', content: 'Search'},
    {name: 'manage_pdf_document_tags_button_new', content: 'New'},   
    {name: 'manage_pdf_document_label_uploading', content: 'Uploading...'},     

  ];

  //
  // Vietnamese
  internationalize_vi = [
    {name: 'doc_feed_[Document_Feed]', content: 'Hồ sơ thức ăn'},
    {name: 'doc_feed_[pages]', content: 'Nhiều trang'},
    {name: 'doc_feed_[Trace]', content: 'Truy xuất'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'Xem: văn bản của tôi'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'Xem: tất cả văn bản'},

    {name: 'filter_[Sorting By Date]', content: 'Xếp theo ngày'},
    {name: 'filter_[Usernames]', content: 'Tên người dùng'},
    {name: 'filter_[Doc Types]', content: 'Kiểu văn bản'},
    {name: 'filter_[Documents]', content: 'Tài liệu'},
    {name: 'filter_[Lot # Search]', content: 'Tìm kiếm theo thẻ'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'Thiết lập lại bộ lọc'},
    {name: 'filter_[Newest]', content: 'Mới nhất'},
    {name: 'filter_[Oldest]', content: 'Cũ nhất'},
    {name: 'filter_[Partial Match]', content: 'Khớp một phần'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'Tìm kiếm'},
    {name: 'filter_[Close Lot # Search]', content: 'Đóng thẻ tìm kiếm'},
    {name: 'filter_[Lot # 1]', content: 'Thẻ'},
    {name: 'filter_[Lot # 2]', content: 'Thẻ'},
    {name: 'filter_[Lot # 3]', content: 'Thẻ'},
    {name: 'filter_[Lot # 4]', content: 'Thẻ'},
    {name: 'filter_[Lot # 5]', content: 'Thẻ'},
    {name: 'filter_[Lot # 6]', content: 'Thẻ'},
    {name: 'filter_[Lot # 7]', content: 'Thẻ'},
    {name: 'filter_[Lot # 8]', content: 'Thẻ'},
    {name: 'filter_[Lot # 9]', content: 'Thẻ'},
    {name: 'filter_[Lot # 10]', content: 'Thẻ'},
    {name: 'filter_[Lot 1]', content: 'Thẻ'},
    {name: 'filter_[Lot 2]', content: 'Thẻ'},
    {name: 'filter_[Lot 3]', content: 'Thẻ'},
    {name: 'filter_[Lot 4]', content: 'Thẻ'},
    {name: 'filter_[Lot 5]', content: 'Thẻ'},
    {name: 'filter_[Lot 6]', content: 'Thẻ'},
    {name: 'filter_[Lot 7]', content: 'Thẻ'},
    {name: 'filter_[Lot 8]', content: 'Thẻ'},
    {name: 'filter_[Lot 9]', content: 'Thẻ'},
    {name: 'filter_[Lot 10]', content: 'Thẻ'},
    {name: 'filter_[Lot # Search Close]', content: 'Đóng thẻ tìm kiếm'},
    {name: 'filter_[Lot # Search Add Row]', content: 'Thêm thẻ mới'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'Chi tiết tài liệu'},
    {name: 'doc_details_[owner]', content: 'Chủ sở hữu'},
    {name: 'doc_details_[created]', content: 'Ngày khởi tạo'},
    {name: 'doc_details_[location]', content: 'Địa điểm'},
    {name: 'doc_details_[organization]', content: 'Tổ chức'},
    {name: 'doc_details_[linked docs]', content: 'Văn bản được liên kết'},
    {name: 'doc_details_[Doc Pages]', content: 'Trang hồ sơ'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'Trang hồ sơ'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'Người nhận tài liệu'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'Văn bản được liên kết'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'Sao lưu văn bản'},
    {name: 'doc_details_tabs[Document Tags]', content: 'Thẻ tài liệu'},
    {name: 'doc_details_tabs[Document Fields]', content: 'Thông tin hồ sơ'},
    {name: 'doc_details_tabs[Document Notes]', content: 'Ghi chú tài liệu'},
    {name: 'doc_details_tabs[Document Type]', content: 'Kiểu tài liệu'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'Tài liệu được liên kết'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'Sao lưu tài liệu'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'Các thẻ cho tài liệu này'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'Thông tin hồ sơ'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'Ghi chú cho tài liệu này'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'Kiểu tài liệu'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'Chủ sở hữu'},
    {name: 'doc_details_tabs_content[Organization]', content: 'Tổ chức'},
    {name: 'doc_details_tabs_content[Created]', content: 'Đã được tạo'},
    {name: 'doc_details_tabs_content[Pages]', content: 'Nhiều trang'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'Trang'}, 

    {name: 'doc_trace[Details]', content: 'Chi tiết truy xuất'},
    {name: 'doc_trace_close[Close]', content: 'Đóng'},
    {name: 'doc_trace_show_all[Show]', content: 'Hiển thị tất cả tài liệu'},

        // Tag Management
        {name: 'tag_[Search]', content: 'Tìm kiếm'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'Tạo hồ sơ mới'},
    {name: 'edit_[existing_doc]', content: 'Edit Document'},

    {name: 'user_[document_types]', content: 'Kiểu tài liệu'},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},



    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'Chấp nhận hồ sơ'}, 
    {name: 'recall_document[close_dialog]', content: 'Thu hồi'},
    {name: 'submit_document[close_dialog]', content: 'Nộp'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'Bạn có muốn CHẤP NHẬN hồ sơ này?'},
    {name: 'share_app[close_dialog]', content: 'Hủy'},
    {name: 'confirm_title[close_dialog]', content: 'Xác nhận'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'Lý do từ chối'},
    {name: 'document_notes_header[rejection_dialog]', content: 'Tiêu đề từ chối'},
    {name: 'document_notes_details[rejection_dialog]', content: 'Chi tiết từ chối'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'Viết ý kiến từ chối...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'Hủy'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'Lưu lại và loại bỏ'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'Chấp nhận'}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'Từ chối'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'Thu hồi'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'Nộp'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'SAVE'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'CANCEL'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'DELETE'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'Bị từ chối'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'ĐÃ CHẤP NHẬN'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'Đang chờ xử lý'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'Đã nộp'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'Bản nháp'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'Gởi lại'}, 

    //
    // Doc Import
    {name: 'document_upload_upload_files_list_number', content: 'Số tập tin đã chọn'}, 
    {name: 'document_upload_Tags_Number', content: 'Thẻ'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'Người nhận'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'Văn bản được liên kết'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'Văn bản được đính kèm'}, 
    {name: 'document_upload_upload_button', content: 'Tải lên'}, 
    {name: 'document_upload_Upload_Header', content: 'Nhập tài liệu PDF'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'Thông tin hồ sơ'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'Mới nhất'}, 
    {name: 'document_filter_sort_oldest', content: 'Cũ nhất'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- Tất cả người dùng'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- Tất cả các kiểu văn bản'}, 
    {name: 'document_import_choose_doc_type', content: '-- Chọn loại văn bản'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- Tất cả các loại hồ sơ'}, 
    {name: 'document_import_choose_group', content: '-- Choose Organization'}, 
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'document_search_tag_to_link', content: 'Nhập thẻ'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'Các tài liệu sau khớp với thẻ tìm kiếm của bạn'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'Không có tài liệu'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'Auto Translate'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'Lock Stages'},
    {name: 'stage.display.name', content: 'Stage Name'},

    // Document Preview
    {name: 'document.preview.pages', content: 'Tắt bản xem trước trang'},
    {name: 'document.preview.pages.header', content: 'Xem trước trang'},
    {name: 'document.preview.pages.doc.type.linked', content: 'Tài liệu được Liên kết'},
    {name: 'document.preview.pages.doc.type.backup', content: 'Tài liệu sao lưu'}, 

    // Other
    {name: 'export_gps_data_button_label', content: 'Xuất dữ liệu'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'Xuất báo cáo truy xuất'},  
    {name: 'manage_pdf_document_label_doc_type', content: 'Document Type'},
    {name: 'manage_pdf_document_button_save', content: 'Save'},
    {name: 'manage_pdf_document_button_submit', content: 'Submit'},
    {name: 'manage_pdf_document_button_cancel', content: 'Cancel'},
    {name: 'manage_pdf_document_button_choose_file', content: 'Chọn tập tin'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'No File Chosen'},
    {name: 'manage_pdf_document_tags_button_search', content: 'Search'},
    {name: 'manage_pdf_document_tags_button_new', content: 'New'},
    {name: 'manage_pdf_document_label_uploading', content: 'Uploading...'},      

  ];

  //
  // Spanish
  internationalize_sp = [
    {name: 'doc_feed_[Document_Feed]', content: 'Documentos'},
    {name: 'doc_feed_[pages]', content: 'páginas'},
    {name: 'doc_feed_[Trace]', content: 'Rastreo'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'Mis Documentos'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'Todos Los Documentos'},

    {name: 'filter_[Sorting By Date]', content: 'Seleccionar por Fecha'},
    {name: 'filter_[Usernames]', content: 'Nombre de los Usuarios'},
    {name: 'filter_[Doc Types]', content: 'Tipos de Documento'},
    {name: 'filter_[Documents]', content: 'Documentos'},
    {name: 'filter_[Lot # Search]', content: 'Búsqueda de Etiqueta'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'Restablecer Filtro'},
    {name: 'filter_[Newest]', content: 'Lo más Nuevo'},
    {name: 'filter_[Oldest]', content: 'Lo más antiguo'},
    {name: 'filter_[Partial Match]', content: 'Encaje Parcial'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'Buscar'},
    {name: 'filter_[Close Lot # Search]', content: 'Cerrar búsqueda de etiqueta de documento'},
    {name: 'filter_[Lot # 1]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 2]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 3]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 4]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 5]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 6]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 7]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 8]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 9]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # 10]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 1]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 2]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 3]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 4]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 5]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 6]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 7]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 8]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 9]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot 10]', content: 'Etiqueta De Documento'},
    {name: 'filter_[Lot # Search Close]', content: 'Cerrar búsqueda de etiquetas'},
    {name: 'filter_[Lot # Search Add Row]', content: 'Añadir  etiqueta de fila'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'Detalles del documento'},
    {name: 'doc_details_[owner]', content: 'Propietario'},
    {name: 'doc_details_[created]', content: 'Fecha de creación'},
    {name: 'doc_details_[location]', content: 'Localidad'},
    {name: 'doc_details_[organization]', content: 'Organización'},
    {name: 'doc_details_[linked docs]', content: 'Documentos enlazados'},
    {name: 'doc_details_[Doc Pages]', content: 'Páginas del documento'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'Páginas del documento'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'destinatarios'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'Documentos enlazados'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'Documentos de apoyo'},
    {name: 'doc_details_tabs[Document Tags]', content: 'Etiquetas de Documento'},
    {name: 'doc_details_tabs[Document Fields]', content: 'Información del Documento'},
    {name: 'doc_details_tabs[Document Notes]', content: 'Notas del documento'},
    {name: 'doc_details_tabs[Document Type]', content: 'Tipo de Documento'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'Documentos Enlazados'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'Documentos De Apoyo'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'Etiquetas para este documento'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'Información del Documento'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'Notas para este Documento'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'Tipo de Documento'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'Propietario'},
    {name: 'doc_details_tabs_content[Organization]', content: 'Organización'},
    {name: 'doc_details_tabs_content[Created]', content: 'Creado(a)'},
    {name: 'doc_details_tabs_content[Pages]', content: 'Páginas'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'Página'}, 

    {name: 'doc_trace[Details]', content: 'Detalles de Rastreo'},
    {name: 'doc_trace_close[Close]', content: 'Cerrar'},
    {name: 'doc_trace_show_all[Show]', content: 'Mostrar todos los documentos'},

    // Tag Management
    {name: 'tag_[Search]', content: 'Buscar'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'Importar PDF'},
    {name: 'edit_[existing_doc]', content: 'Edit Document'},

    {name: 'user_[document_types]', content: 'Document Type'},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},

    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'Aceptar Documento'}, 
    {name: 'recall_document[close_dialog]', content: 'Recuperación'},
    {name: 'submit_document[close_dialog]', content: 'Remitir'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'Estás seguro de que quieres ACEPTAR este documento?'},
    {name: 'share_app[close_dialog]', content: 'Cancelar'},
    {name: 'confirm_title[close_dialog]', content: 'Confirmar'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'Motivos del Rechazo'},
    {name: 'document_notes_header[rejection_dialog]', content: 'Encabezado del Rechazo'},
    {name: 'document_notes_details[rejection_dialog]', content: 'Detalles del Rechazo'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'Escribe tus comentarios de rechazo...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'Cancelar'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'Guardar y Rechazar'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'ACEPTAR'}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'RECHAZAR'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'RECUPERACIÓN'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'REMITIR'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'SALVAR'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'CANCELAR'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'ELIMINAR'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'RECHAZADO'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'ACEPTADO'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'PENDIENTE'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'REMITIDO'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'BORRADOR'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'REENVIADO'}, 

    //
    // Doc Import

    {name: 'document_upload_upload_files_list_number', content: 'Archivos para cargar'}, 
    {name: 'document_upload_Tags_Number', content: 'Etiquetas'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'Destinatarios'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'Documentos Enlazados'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'Documentos Adjuntos'}, 
    {name: 'document_upload_upload_button', content: 'Cargar'}, 
    {name: 'document_upload_Upload_Header', content: 'IMPORTAR DOCUMENTO EN PDF'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'Información Del Documento'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'Lo más Nuevo'}, 
    {name: 'document_filter_sort_oldest', content: 'Lo más antiguo'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- Todos Los Usuarios'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- Todo Tipo De Documentos'}, 
    {name: 'document_import_choose_doc_type', content: '-- Elija Tipo De Documento'}, 
    {name: 'document_import_choose_group', content: '-- Choose Organization'}, 
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'document_search_tag_to_link', content: 'Ingrese Etiqueta'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'Los siguientes documentos coinciden con su etiqueta de búsqueda'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'Sin Documentos'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'Traducción automática'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'Etapas de Aseguramiento'},
    {name: 'stage.display.name', content: 'Stage Name'},

    // Document Preview
    {name: 'document.preview.pages', content: 'Deshabilitar la vista previa de la página'},  
    {name: 'document.preview.pages.header', content: 'Vista previa de la página'},   
    {name: 'document.preview.pages.doc.type.linked', content: 'Documentos vinculados'},
    {name: 'document.preview.pages.doc.type.backup', content: 'Documentos de respaldo'}, 

    // Other
    {name: 'export_gps_data_button_label', content: 'Exportación de datos'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'Rastro del Exportador'},    
    {name: 'manage_pdf_document_label_doc_type', content: 'Document Type'},
    {name: 'manage_pdf_document_button_save', content: 'Salvar'},
    {name: 'manage_pdf_document_button_submit', content: 'Enviar'},
    {name: 'manage_pdf_document_button_cancel', content: 'Cancelar'},
    {name: 'manage_pdf_document_button_choose_file', content: 'Elija el archivo'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'Ningún archivo elegido'},
    {name: 'manage_pdf_document_tags_button_search', content: 'Buscar'},
    {name: 'manage_pdf_document_tags_button_new', content: 'Nueva'},   
    {name: 'manage_pdf_document_label_uploading', content: 'Subiendo...'}, 

  ];

  //
  // Thai
  internationalize_th = [
    {name: 'fishing_logbook_document_type', content: 'สมุดบันทึกการทําการประมง'},
    {name: 'ship_license_document_type', content: 'ใบอนุญาตให้ใช้เรือ'},
    {name: 'feed_lot_sheet_document_type', content: 'เอกสารติดตามล๊อตอาหารสัตว์'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'เอกสารติดตามล็อตปลาป่น'},
    {name: 'captain_statement_type', content: 'คำแถลงของกัปตัน'},
    {name: 'movement_document_document_type', content: 'หนังสือกำกับการจำหน่ายสัตว์น้ำ'},
    {name: 'vessel_list', content: 'รายการเรือ'},
    {name: 'dof_labor_certification', content: 'ใบรับรองแรงงานกรมประมง'},
    {name: 'farmer_id_card', content: 'บัตรประจำตัวเกษตรกร'},
    {name: 'code_of_conduct_document', content: 'หลักจริยธรรม'},
    {name: 'hatchery_license_document', content: 'ใบอนุญาตประกอบกิจการเพาะเลี้ยงลูกพันธุ์สัตว์น้ำ'},
    {name: 'vessel_registration_document', content: 'การจดทะเบียนเรือ'},
    {name: 'vessel_driver_license_document', content: 'ใบอนุญาตเดินเรือ'},
    {name: 'farm_license_document', content: 'สพอ.1'},
    {name: 'farmer_license_tor_bor_document', content: 'ทบ.1'},
    {name: 'factory_license_document', content: 'ใบอนุญาตโรงงาน'},


    {name: 'doc_feed_[Document_Feed]', content: 'รายการเอกสาร'},
    {name: 'doc_feed_[pages]', content: 'เลขหน้า'},
    {name: 'doc_feed_[Trace]', content: 'แกะรอย'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'เอกสารของฉัน'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'เอกสารการให้อาหาร'},

    {name: 'filter_[Sorting By Date]', content: 'เรียงตามวันที่'},
    {name: 'filter_[Usernames]', content: 'ชื่อผู้ใช้'},
    {name: 'filter_[Doc Types]', content: 'ประเภทเอกสาร'},
    {name: 'filter_[Documents]', content: 'จำนวนเอกสาร'},
    {name: 'filter_[Lot # Search]', content: 'ค้นหาล็อต'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Close Lot # Search]', content: 'ปิดค้นหาล็อต'},
    {name: 'filter_[Reset Filter]', content: 'รีเซ็ตตัวกรอง'},
    {name: 'filter_[Newest]', content: 'ล่าสุด'},
    {name: 'filter_[Oldest]', content: 'เพรง'},
    {name: 'filter_[Partial Match]', content: 'จับคู่บางส่วน'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'ค้นหา'},

    {name: 'filter_[Lot # 1]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 2]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 3]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 4]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 5]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 6]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 7]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 8]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 9]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # 10]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 1]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 2]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 3]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 4]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 5]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 6]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 7]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 8]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 9]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot 10]', content: 'แท็กเอกสาร'},
    {name: 'filter_[Lot # Search Close]', content: 'ปิด'},
    {name: 'filter_[Lot # Search Add Row]', content: 'เพิ่มแถว'},
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'รายละเอียดเอกสาร'},
    {name: 'doc_details_[owner]', content: 'เจ้าของ'},
    {name: 'doc_details_[created]', content: 'วันที่สร้าง'},
    {name: 'doc_details_[location]', content: 'ที่ตั้ง'},
    {name: 'doc_details_[organization]', content: 'องค์การ'},
    {name: 'doc_details_[linked docs]', content: 'เอกสารที่เชื่อมโยง'},
    {name: 'doc_details_[Doc Pages]', content: 'หน้าเอกสาร'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'หน้าเอกสาร'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'ผู้รับเอกสาร'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'เอกสารที่เชื่อมโยง'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'เอกสารสำรอง'},
    {name: 'doc_details_tabs[Document Tags]', content: 'แท็กเอกสาร'},
    {name: 'doc_details_tabs[Document Fields]', content: 'Document Info'},
    {name: 'doc_details_tabs[Document Notes]', content: 'เอกสารหมายเหตุ'},
    {name: 'doc_details_tabs[Document Type]', content: 'ประเภทเอกสาร'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'เอกสารที่เชื่อมโยง'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'เอกสารสำรอง'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'แท็กสำหรับเอกสารนี้'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'Document Info'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'หมายเหตุสำหรับเอกสารนี้'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'ประเภทเอกสาร'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'เจ้าของ'},
    {name: 'doc_details_tabs_content[Organization]', content: 'องค์การ'},
    {name: 'doc_details_tabs_content[Created]', content: 'วันที่สร้าง'},
    {name: 'doc_details_tabs_content[Pages]', content: 'หน้า'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'เลขหน้า'},

    {name: 'doc_trace[Details]', content: 'แกะรอยรายละเอียด'},
    {name: 'doc_trace_close[Close]', content: 'ปิด'},
    {name: 'doc_trace_show_all[Show]', content: 'แสดงเอกสารทั้งหมด'},

    // Tag Management
    {name: 'tag_[Search]', content: 'ค้นหา'},

    // Document Types 
    //
    {name: 'vessel_captains_statement', content: 'รายงานของกัปตัน'},
    {name: 'vessel_crew_documents', content: 'เอกสารลูกเรือ'},
    {name: 'vessel_dolphin_safe_book', content: 'คำแถลงการณ์เพื่อการอนุรักษ์โลมา'},
    {name: 'vessel_fishing_log_book', content: 'สมุดบันทึกการทําการประมง'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'ใบรับรองแหล่งกำเนิดการประมง'},
    {name: 'vessel_mcpd', content: 'หนังสือกำกับการซื้อขายสัตว์น้ำจากประมงทะเล'},
    {name: 'vessel_port_in_out', content: 'เอกสารการแจ้งเรือเข้า-ออกท่าเทียบเรือ'},

    {name: 'port_docking_bill', content: 'ตั๋วเทียบท่า'},
    {name: 'port_docking_logbook', content: 'สมุดบันทึกการเทียบท่า'},
    {name: 'port_info_of_conveyance', content: 'ข้อมูลการขนส่งและถ่ายโอนสัตว์น้ำ'},
    {name: 'port_crew_documents', content: 'เอกสารลูกเรือ'},
    {name: 'port_port_in_out', content: 'ท่าเรือเข้า / ออก'},
    {name: 'port_weighing_records', content: 'บันทีกการชั่งน้ำหนัก'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'อาหารปลา MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'ใบรับรองแหล่งกำเนิดปลาป่น'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'หนังสือกำกับปลาป่นนำเข้า'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'เอกสารแสดงรายการบรรจุปลาป่น'},
    {name: 'fishmeal_plant_captain_statement', content: 'รายงานของกัปตัน'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'รายงานการช่วยเหลือโลมา'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'ใบรับรองแหล่งกำเนิดการประมง'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'หนังสือกำกับการจำหน่ายอาหารปลา'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'หนังสือกำกับการขนส่งล็อตอาหารสัตว์'},
    {name: 'feed_mill_feed_ingredient_list', content: 'รายการวัตถุดิบอาหารสัตว์'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'เอกสารห่วงโซ่อุปทานของอาหารปลา'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'รายการการบรรจุอาหารปลา'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'ใบสรุปผลรับรองอาหารปลา'},
    {name: 'feed_mill_lot_traceability', content: 'ประวัติย้อนหลังล็อตอาหาร'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'หนังสือกำกับการซื้อขายปลาป่น A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'เอกสารการขนส่งอาหารปลา'},

    {name: 'hatchery_fry_movement_document', content: 'หนังสือกำกับการจำหน่ายลูกพันธุ์สัตว์น้ำ'},
    {name: 'hatchery_good_aquaculture_practices', content: 'ใบรับรองมาตรฐานการปฏิบัติทางการเพาะเลี้ยงสัตว์น้ำที่ดี (GAP)'},

    {name: 'farm_fry_movement_document', content: 'เอกสารการขนย้าย'},
    {name: 'farm_movement_document', content: 'เอกสารการขนย้ายลูกปลา'},
    {name: 'farm_pond_feed_info_sheet', content: 'ใบแจ้งข้อมูลอาหารสัตว์'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'ใบรับรองการเพาะเลี้ยงสัตว์น้ำที่ถูกต้อง'},

    {name: 'shrimp_broker_movement_document', content: 'เอกสารการขนย้าย'},

    {name: 'processor_shrimp_lot_traceability', content: 'ประวัติย้อนหลังล็อตกุ้ง'},
    {name: 'processor_shipping_documents', content: 'เอกสารการขนส่ง'},
    {name: 'processor_bill_of_lading', content: 'ใบตราส่งสินค้าทางทะเล'},
    {name: 'processor_commercial_invoice', content: 'ใบวางบิล'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'หนังสือกํากับการทวนสอบล็อตอาหารกุ้ง'},
    {name: 'processor_movement_document', content: 'เอกสารการขนย้าย'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'นำเข้าเอกสาร PDF'},
    {name: 'edit_[existing_doc]', content: 'Edit Document'},

    {name: 'user_[document_types]', content: 'ประเภทเอกสาร'}, 
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},
    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'ยอมรับเอกสาร'},
    {name: 'recall_document[close_dialog]', content: 'Recall'},
    {name: 'submit_document[close_dialog]', content: 'Submit'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'ท่านต้องการยอมรับเอกสารฉบับนี้ใช่หรือไม่'},
    {name: 'share_app[close_dialog]', content: 'ยกเลิก'},
    {name: 'confirm_title[close_dialog]', content: 'ยืนยัน'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'เหตุผลในการปฏิเสธเอกสาร'},
    {name: 'document_notes_header[rejection_dialog]', content: 'ชื่อเรื่อง'},
    {name: 'document_notes_details[rejection_dialog]', content: 'ข้อความ'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'เขียนความคิดเห็นปฏิเสธของคุณ ...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'ยกเลิก'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'บันทึกและปฏิเสธ'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'ยอมรับ'},
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'ปฏิเสธ'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'RECALL'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'SUBMIT'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'SAVE'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'CANCEL'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'DELETE'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'ปฏิเสธแล้ว'},
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'ยอมรับแล้ว'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'อยู่ระหว่างการดำเนินการ'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'นำเสนอแล้ว'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'ร่าง'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'นำเสนอซ้ำอีกครั้งแล้ว'}, 

    //
    // Doc Import
    {name: 'document_upload_upload_files_list_number', content: 'Files to Upload'}, 
    {name: 'document_upload_Tags_Number', content: 'แท็ก'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'ผู้รับเอกสาร'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'เอกสารที่เชื่อมต่อแล้ว'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'เอกสารที่เชื่อมต่อแล้ว'}, 
    {name: 'document_upload_upload_button', content: 'อัปโหลด'}, 
    {name: 'document_upload_Upload_Header', content: 'นำเข้าเอกสาร'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'Document Info'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'ล่าสุด'}, 
    {name: 'document_filter_sort_oldest', content: 'ที่เก่าแก่ที่สุด'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- ผู้ใช้ทั้งหมด'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- เอกสารทุกประเภท'}, 
    {name: 'document_import_choose_doc_type', content: '-- เลือกประเภทเอกสาร'}, 
    {name: 'document_import_choose_group', content: '-- Choose Organization'},
    {name: 'document_import_choose_stage', content: '-- Choose Stage'},  
    {name: 'document_search_tag_to_link', content: 'Search Tags'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'The following documents match your search tag'},
    {name: 'document_search_tag_documents_none_found_to_link', content: 'No documents'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'Auto Translate'}, 
    {name: 'stage.managements[Lock Stages]', content: 'Lock Stages'}, 
    {name: 'stage.display.name', content: 'Stage Name'},

    
    // Document Preview
    {name: 'document.preview.pages', content: 'ปิดใช้งานการแสดงตัวอย่างหน้า'},  
    {name: 'document.preview.pages.header', content: 'ตัวอย่างหน้า'},  
    {name: 'document.preview.pages.doc.type.linked', content: 'เอกสารที่เชื่อมโยง'},
    {name: 'document.preview.pages.doc.type.backup', content: 'เอกสารสำรอง'}, 

    // Other
    {name: 'export_gps_data_button_label', content: 'Export GPS Data'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'Export Trace'},    
    {name: 'manage_pdf_document_label_doc_type', content: 'Document Type'},
    {name: 'manage_pdf_document_button_save', content: 'Save'},
    {name: 'manage_pdf_document_button_submit', content: 'Submit'},
    {name: 'manage_pdf_document_button_cancel', content: 'Cancel'},
    {name: 'manage_pdf_document_button_choose_file', content: 'Choose File'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'No File Chosen'},
    {name: 'manage_pdf_document_tags_button_search', content: 'Search'},
    {name: 'manage_pdf_document_tags_button_new', content: 'New'},    
    {name: 'manage_pdf_document_label_uploading', content: 'Uploading...'},

  ];

  //
  // Hindi
  internationalize_hi = [
    {name: 'doc_feed_[Document_Feed]', content: 'दस्तावेज़ फ़ीड'},
    {name: 'doc_feed_[pages]', content: 'पृष्ठों'},
    {name: 'doc_feed_[Trace]', content: 'ट्रेस'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'देखना: मेरे डॉक्स'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'देखना: सभी डॉक्स'},

    {name: 'filter_[Sorting By Date]', content: 'तिथि के अनुसार छँटाई'},
    {name: 'filter_[Usernames]', content: 'उपयोगकर्ता नाम'},
    {name: 'filter_[Doc Types]', content: 'डॉक्टर के प्रकार'},
    {name: 'filter_[Documents]', content: 'दस्तावेज़'},
    {name: 'filter_[Lot # Search]', content: 'टैग खोज'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'फ़िल्टर रीसेट करें'},
    {name: 'filter_[Newest]', content: 'नवीनतम'},
    {name: 'filter_[Oldest]', content: 'सबसे पुराना'},
    {name: 'filter_[Partial Match]', content: 'आंशिक मैच'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'खोज'},
    {name: 'filter_[Close Lot # Search]', content: 'दस्तावेज़ टैग खोज बंद करें'},
    {name: 'filter_[Lot # 1]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 2]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 3]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 4]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 5]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 6]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 7]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 8]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 9]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # 10]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 1]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 2]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 3]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 4]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 5]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 6]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 7]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 8]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 9]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot 10]', content: 'दस्तावेज़ टैग'},
    {name: 'filter_[Lot # Search Close]', content: 'टैग खोज बंद करें'},
    {name: 'filter_[Lot # Search Add Row]', content: 'टैग पंक्ति जोड़ें'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'दस्तावेज़ विवरण'},
    {name: 'doc_details_[owner]', content: 'मालिक'},
    {name: 'doc_details_[created]', content: 'रचना तिथि'},
    {name: 'doc_details_[location]', content: 'स्थान'},
    {name: 'doc_details_[organization]', content: 'संगठन'},
    {name: 'doc_details_[linked docs]', content: 'लिंक किए गए दस्तावेज़'},
    {name: 'doc_details_[Doc Pages]', content: 'दस्तावेज़ पृष्ठ'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'दस्तावेज़ पृष्ठ'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'प्राप्तकर्ताओं'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'लिंक किए गए दस्तावेज़'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'बैकअप दस्तावेज़'},
    {name: 'doc_details_tabs[Document Tags]', content: 'दस्तावेज़ टैग'},
    {name: 'doc_details_tabs[Document Fields]', content: 'दस्तावेज़ जानकारी'},
    {name: 'doc_details_tabs[Document Notes]', content: 'दस्तावेज़ नोट'},
    {name: 'doc_details_tabs[Document Type]', content: 'दस्तावेज़ का प्रकार'},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'लिंक किए गए दस्तावेज़'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'बैकअप दस्तावेज़'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'इस दस्तावेज़ के लिए टैग'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'दस्तावेज़ जानकारी'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'इस दस्तावेज़ के लिए नोट्स'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'दस्तावेज़ का प्रकार'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'मालिक'},
    {name: 'doc_details_tabs_content[Organization]', content: 'संगठन'},
    {name: 'doc_details_tabs_content[Created]', content: 'बनाया था'},
    {name: 'doc_details_tabs_content[Pages]', content: 'पृष्ठों'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'पृष्ठ'}, 

    {name: 'doc_trace[Details]', content: 'ट्रेस विवरण'},
    {name: 'doc_trace_close[Close]', content: 'बंद करे'},
    {name: 'doc_trace_show_all[Show]', content: 'सभी डॉक्स दिखाएं'},

    // Tag Management
    {name: 'tag_[Search]', content: 'खोज'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'नया दस्तावेज़ बनाएँ'},
    {name: 'edit_[existing_doc]', content: 'दस्तावेज़ संपादित करें'},

    {name: 'user_[document_types]', content: 'दस्तावेज़ का प्रकार'},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},

    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'दस्तावेज़ स्वीकार करें'}, 
    {name: 'recall_document[close_dialog]', content: 'याद'},
    {name: 'submit_document[close_dialog]', content: 'प्रस्तुत'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'क्या आप वाकई इस दस्तावेज़ को स्वीकार करना चाहते हैं?'},
    {name: 'share_app[close_dialog]', content: 'रद्द करना'},
    {name: 'confirm_title[close_dialog]', content: 'पुष्टि करें'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'अस्वीकृति के कारण'},
    {name: 'document_notes_header[rejection_dialog]', content: 'अस्वीकृति हैडर'},
    {name: 'document_notes_details[rejection_dialog]', content: 'अस्वीकृति विवरण'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'अपनी अस्वीकृति टिप्पणी लिखें...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'रद्द करना'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'सहेजें और अस्वीकार करें'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'स्वीकार करना'}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'अस्वीकार'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'याद'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'प्रस्तुत'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'सहेजें'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'रद्द करना'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'हटाएं'},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'अस्वीकृत'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'स्वीकार किए जाते हैं'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'लंबित'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'प्रस्तुत'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'प्रारूप'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'पुन: प्रस्तुत'}, 

    //
    // Doc Import

    {name: 'document_upload_upload_files_list_number', content: 'अपलोड करने के लिए फ़ाइलें'}, 
    {name: 'document_upload_Tags_Number', content: 'टैग'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'प्राप्तकर्ताओं'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'लिंक किए गए डॉक्स'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'बैकअप दस्तावेज़'}, 
    {name: 'document_upload_upload_button', content: 'डालना'}, 
    {name: 'document_upload_Upload_Header', content: 'पीडीएफ दस्तावेज़ आयात करें'}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'दस्तावेज़ जानकारी'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'नवीनतम'}, 
    {name: 'document_filter_sort_oldest', content: 'सबसे पुराना'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- सभी उपयोगकर्ताओं'}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- सभी डॉक्टर प्रकार'}, 
    {name: 'document_import_choose_doc_type', content: '-- दस्तावेज़ प्रकार चुनें'}, 
    {name: 'document_import_choose_group', content: '-- Choose Organization'}, 
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'document_search_tag_to_link', content: 'टैग दर्ज करें'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'निम्नलिखित दस्तावेज़ आपके खोज टैग से मेल खाते हैं'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'कोई दस्तावेज नहीं'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'ऑटो अनुवाद'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'चरण बंद करो'},
    {name: 'stage.display.name', content: 'Stage Name'},

    
    // Document Preview
    {name: 'document.preview.pages', content: 'पृष्ठ पूर्वावलोकन अक्षम करें'},   
    {name: 'document.preview.pages.header', content: 'पेज पूर्वावलोकन'},    
    {name: 'document.preview.pages.doc.type.linked', content: 'लिंक किए गए दस्तावेज़'},
    {name: 'document.preview.pages.doc.type.backup', content: 'बैकअप दस्तावेज़'}, 

    // Other
    {name: 'export_gps_data_button_label', content: 'डेटा निर्यात'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'निर्यात ट्रेस'},
    {name: 'manage_pdf_document_label_doc_type', content: 'दस्तावेज़ का प्रकार'},
    {name: 'manage_pdf_document_button_save', content: 'सहेजें'},
    {name: 'manage_pdf_document_button_submit', content: 'प्रस्तुत'},
    {name: 'manage_pdf_document_button_cancel', content: 'रद्द करना'},
    {name: 'manage_pdf_document_button_choose_file', content: 'फ़ाइल का चयन'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'कोई फ़ाइल नहीं चुनी गई'},
    {name: 'manage_pdf_document_tags_button_search', content: 'खोज'},
    {name: 'manage_pdf_document_tags_button_new', content: 'New'},
    {name: 'manage_pdf_document_label_uploading', content: 'फ़ाइलें अपलोड कर रहा है...'},

  ];

  //
  // Telugu
  internationalize_te = [
    {name: 'doc_feed_[Document_Feed]', content: 'డాక్యుమెంట్ ఫీడ్'},
    {name: 'doc_feed_[pages]', content: 'పేజీలు'},
    {name: 'doc_feed_[Trace]', content: 'ట్రెస్'},
    {name: 'doc_feed_[FilterMyDocs]', content: 'వీక్షణ: నా డాక్స్'},
    {name: 'doc_feed_[FilterAllDocs]', content: 'వీక్షణ: అన్ని డాక్స్'},

    {name: 'filter_[Sorting By Date]', content: 'తేదీ ప్రకారం క్రమబద్ధీకరించడం'},
    {name: 'filter_[Usernames]', content: 'వినియోగదారు పేర్లు'},
    {name: 'filter_[Doc Types]', content: 'డాక్యుమెంట్ రకాలు '},
    {name: 'filter_[Documents]', content: 'డాక్యుమెంట్లు '},
    {name: 'filter_[Lot # Search]', content: 'ట్యాగ్ సర్చ్'},
    {name: 'filter_[Doc Info Search]', content: 'Doc Info Search'},
    {name: 'filter_[Reset Filter]', content: 'ఫిల్టర్‌ను రీసెట్ చేయండి'},
    {name: 'filter_[Newest]', content: 'సరికొత్త'},
    {name: 'filter_[Oldest]', content: 'పురాతన'},
    {name: 'filter_[Partial Match]', content: 'పాక్షిక మ్యాచ్'},
    {name: 'filter_[Date Range for Docs]', content: 'Date Range'},
    {name: 'filter_[Date From For Docs]', content: 'Date From'},
    {name: 'filter_[Date To For  Docs]', content: 'Date To'},
    {name: 'filter_[Search]', content: 'సర్చ్'},
    {name: 'filter_[Close Lot # Search]', content: 'డాక్యుమెంట్ ట్యాగ్ శోధనను మూసివేయండి'},
    {name: 'filter_[Lot # 1]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 2]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 3]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 4]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 5]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 6]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 7]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 8]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 9]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # 10]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 1]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 2]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 3]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 4]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 5]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 6]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 7]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 8]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 9]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot 10]', content: 'డాక్యుమెంట్ ట్యాగ్'},
    {name: 'filter_[Lot # Search Close]', content: 'ట్యాగ్ శోధనను మూసివేయండి'},
    {name: 'filter_[Lot # Search Add Row]', content: 'ట్యాగ్  వరుసను జోడించండి'}, 
    {name: 'filter_[Doc Info Search Close]', content: 'Close Search'},
    {name: 'filter_[Doc Info Search Add Row]', content: 'Add Row'},

    {name: 'doc_details_[Document Details]', content: 'డాక్యుమెంట్ వివరాలు'},
    {name: 'doc_details_[owner]', content: 'యజమాని'},
    {name: 'doc_details_[created]', content: 'సృష్టి తేదీ'},
    {name: 'doc_details_[location]', content: 'స్థానం'},
    {name: 'doc_details_[organization]', content: 'సంస్థ'},
    {name: 'doc_details_[linked docs]', content: 'లింక్ చేయబడిన డాక్యుమెంట్లు'},
    {name: 'doc_details_[Doc Pages]', content: 'డాక్యుమెంట్ కాగితాలు'},
    {name: 'doc_details_tabs[Doc Pages]', content: 'డాక్యుమెంట్ కాగితాలు'},
    {name: 'doc_details_tabs[Doc Recipients]', content: 'గ్రహీతలు'},
    {name: 'doc_details_tabs[Linked Docs]', content: 'లింక్ చేయబడిన డాక్యుమెంట్లు'},
    {name: 'doc_details_tabs[Back-up Docs]', content: 'బ్యాక్-అప్ డాక్స్'},
    {name: 'doc_details_tabs[Document Tags]', content: 'డాక్యుమెంట్ టాగ్లు'},
    {name: 'doc_details_tabs[Document Fields]', content: 'పత్ర సమాచారం'},
    {name: 'doc_details_tabs[Document Notes]', content: 'డాక్యుమెంట్ గమనికలు'},
    {name: 'doc_details_tabs[Document Type]', content: 'డాక్యుమెంట్  రకం '},
    {name: 'doc_details_tabs_content[Linked Docs]', content: 'లింక్ చేయబడిన డాక్యుమెంట్లు'},
    {name: 'doc_details_tabs_content[Backup Docs]', content: 'బ్యాక్-అప్ డాక్స్'},
    {name: 'doc_details_tabs_content[Tags for Doc]', content: 'ఈ డాక్యుమెంట్ కోసం టాగ్లు'},
    {name: 'doc_details_tabs_content[Fields for Doc]', content: 'పత్ర సమాచారం'},
    {name: 'doc_details_tabs_content[Notes for Doc]', content: 'ఈ పత్రం కోసం గమనికలు'},
    {name: 'doc_details_tabs_content[Document Type]', content: 'డాక్యుమెంట్  రకం'},
    {name: 'doc_details_tabs_content[Document Info ID]', content: 'Doc Info Id'},
    {name: 'doc_details_tabs_content[Owner]', content: 'యజమాని'},
    {name: 'doc_details_tabs_content[Organization]', content: 'సంస్థ'},
    {name: 'doc_details_tabs_content[Created]', content: 'రూపొందించబడింది'},
    {name: 'doc_details_tabs_content[Pages]', content: 'పేజీలు'},
    {name: 'doc_details_tabs_content_images[Page]', content: 'పేజీ'}, 

    {name: 'doc_trace[Details]', content: 'వివరాలు ట్రేస్ చేయండి'},
    {name: 'doc_trace_close[Close]', content: 'క్లోజ్'},
    {name: 'doc_trace_show_all[Show]', content: 'అన్ని పత్రాలను చూపించు'},

    // Tag Management
    {name: 'tag_[Search]', content: 'సర్చ్'},

    // Document Types 
    //
    {name: 'fishing_logbook_document_type', content: 'Fishing Logbook'},
    {name: 'ship_license_document_type', content: 'Ship License'},
    {name: 'feed_lot_sheet_document_type', content: 'Feed Lot Trace'},
    {name: 'fishmeal_lot_traceability_document_type', content: 'Fishmeal Lot Trace'},
    {name: 'captain_statement_type', content: 'Captains Statement'},
    {name: 'movement_document_document_type', content: 'Movement Document'},
    {name: 'vessel_list', content: 'Vessel List'},
    {name: 'dof_labor_certification', content: 'DOF Labor Certification'},
    {name: 'farmer_id_card', content: 'Farmer ID Card'},
    {name: 'code_of_conduct_document', content: 'Code of Conduct'},
    {name: 'hatchery_license_document', content: 'Hatchery License'},
    {name: 'vessel_registration_document', content: 'Vessel Registration'},
    {name: 'vessel_driver_license_document', content: 'Vessel Driver License'},
    {name: 'farm_license_document', content: 'Farm License'},
    {name: 'farmer_license_tor_bor_document', content: 'Farmer License (Tor Bor 1)'},
    {name: 'factory_license_document', content: 'Factory License'},

    {name: 'vessel_captains_statement', content: 'Captains Statement'},
    {name: 'vessel_crew_documents', content: 'Crew Documents'},
    {name: 'vessel_dolphin_safe_book', content: 'Dolphin Safe Statement'},
    {name: 'vessel_fishing_log_book', content: 'Fishing Logbook'},
    {name: 'vessel_fisheries_cert_of_origin', content: 'Fisheries Cert of Origin'},
    {name: 'vessel_mcpd', content: 'Marine Catch Purchasing Document'},
    {name: 'vessel_port_in_out', content: 'Port-In/Port-Out Document'},

    {name: 'port_docking_bill', content: 'Docking Bill'},
    {name: 'port_docking_logbook', content: 'Docking Logbook'},
    {name: 'port_info_of_conveyance', content: 'Info of Conveyance'},
    {name: 'port_crew_documents', content: 'Crew Documents'},
    {name: 'port_port_in_out', content: 'Port In / Out'},
    {name: 'port_weighing_records', content: 'Weighing Record'},

    {name: 'fish_broker_mcpd', content: 'MCPD'},

    {name: 'fishmeal_plant_mcpd', content: 'Fishmeal MCPD'},
    {name: 'fishmeal_plant_summary_certification', content: 'Fishmeal Certification Of Origin'},
    {name: 'fishmeal_plant_supply_chain_doc_foreign_fishmeal', content: 'Foreign Fishmeal Documents'},
    {name: 'fishmeal_plant_packing_list_foreign_fishmeal', content: 'Fishmeal Packing List'},
    {name: 'fishmeal_plant_captain_statement', content: 'Captains Statement'},
    {name: 'fishmeal_plant_dolphin_safe_statement', content: 'Dolphin Safe Statement'},
    {name: 'fishmeal_plant_fisheries_cert_of_origin', content: 'Fisheries Certification Of Origin'},

    {name: 'fishmeal_broker_fishmeal_mcpd', content: 'Fishmeal MCPD'},

    {name: 'feed_mill_feed_lot_shipping_documents', content: 'Feed Lot Shipping Document'},
    {name: 'feed_mill_feed_ingredient_list', content: 'Feed Ingredient List'},
    {name: 'feed_mill_fishmeal_supplychain_document', content: 'Fishmeal Supply Chain Doc'},
    {name: 'feed_mill_fishmeal_packing_list', content: 'Fishmeal Packing List'},
    {name: 'feed_mill_fishmeal_summary_certification', content: 'Fishmeal Summary Cert'},
    {name: 'feed_mill_lot_traceability', content: 'Lot Traceability'},
    {name: 'feed_mill_fishmeal_mcpd', content: 'Fishmeal MCPD A,B,C,D'},

    {name: 'feed_broker_feedlot_shipping_documents', content: 'Feed Lot Shipping Doc'},

    {name: 'hatchery_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'hatchery_good_aquaculture_practices', content: 'Good Aquaculture Practices Cert'},

    {name: 'farm_fry_movement_document', content: 'Fry Movement Document'},
    {name: 'farm_movement_document', content: 'Movement Document'},
    {name: 'farm_pond_feed_info_sheet', content: 'Feed Information Form'},
    {name: 'farm_good_aquaculture_practices_cert', content: 'Good Aquaculture Practices Cert'},

    {name: 'shrimp_broker_movement_document', content: 'Movement Document'},

    {name: 'processor_shrimp_lot_traceability', content: 'Shrimp Lot Traceability'},
    {name: 'processor_shipping_documents', content: 'Shipping Documents'},
    {name: 'processor_bill_of_lading', content: 'Bill of Lading'},
    {name: 'processor_commercial_invoice', content: 'Commercial Invoice'},
    {name: 'processor_batch_sheet_for_lot_of_shrimp', content: 'Shrimp Lot Traceability'},
    {name: 'processor_movement_document', content: 'Movement Document'},

    {name: 'importer_bill_of_lading', content: 'Bill Of Lading'},
    {name: 'importer_retail_package_document', content: 'Retail Package'},
    {name: 'importer_container_loading_report', content: 'Container Loading Report'},
    {name: 'processor_phys_org_color_odor_checklist', content: 'Phys/Org/Color/Odor Checklist'},
    {name: 'processor_packing_list', content: 'Packing List'},
    {name: 'processor_rm_inspect_report', content: 'R/M Inspect Report'},
    {name: 'processor_package_inspect_report', content: 'Package Inspect Report'},
    {name: 'feed_inspection_document', content: 'Feed Inspection'},
    {name: 'feed_pellet_mill_report', content: 'Pellet Mill Report'},
    {name: 'feed_production_run_time_document', content: 'Production Run time'},
    {name: 'feed_raw_material_report', content: 'Raw Material Report'},

    {name: 'import_[pdfDocument]', content: 'క్రొత్త పత్రాన్ని సృష్టించండి'},
    {name: 'edit_[existing_doc]', content: 'పత్రాన్ని సవరించండి'},

    {name: 'user_[document_types]', content: 'డాక్యుమెంట్  రకం '},
    {name: 'user_[organization_name]', content: 'Organization Name'},
    {name: 'user_[csv_batch_upload]', content: 'User Upload Batch File'},
    {name: 'user_[org_batch_upload]', content: 'Organization Upload Batch File'},

    //
    // Reject/Accept Dialogs
    {name: 'accept_document[close_dialog]', content: 'డాక్యుమెంట్ అంగీకరించు '}, 
    {name: 'recall_document[close_dialog]', content: 'రీకాల్'},
    {name: 'submit_document[close_dialog]', content: 'సబ్మిట్'},
    {name: 'document_acceptance_workflow_dialog_accept[close_dialog]', content: 'మీరు ఈ డాక్యుమెంట్ ని అంగీకరించాలనుకుంటున్నారా?'},
    {name: 'share_app[close_dialog]', content: 'రద్దు'},
    {name: 'confirm_title[close_dialog]', content: 'ధృవపరచు'},
    // 
    {name: 'document_notes_title[rejection_dialog]', content: 'తిరస్కరణకు కారణాలు'},
    {name: 'document_notes_header[rejection_dialog]', content: 'తిరస్కరణ శీర్షిక'},
    {name: 'document_notes_details[rejection_dialog]', content: 'తిరస్కరణ వివరాలు'},
    {name: 'document_notes_details_dalog_default[rejection_dialog]', content: 'మీ తిరస్కరణ వ్యాఖ్యలను వ్రాయండి ...'},
    {name: 'document_notes_details_dialog_cancel[rejection_dialog]', content: 'రద్దు'},
    {name: 'document_notes_details_dialog_save[rejection_dialog]', content: 'సేవ్ చేసి తిరస్కరించండి'},
    // buttons
    {name: 'document_acceptance_workflow_button_accept[button]', content: 'అంగీకరించు '}, 
    {name: 'document_acceptance_workflow_button_reject[button]', content: 'తిరస్కరించు'}, 
    {name: 'document_acceptance_workflow_button_recall[button]', content: 'రీకాల్'},
    {name: 'document_acceptance_workflow_button_submit[button]', content: 'సబ్మిట్'},
    {name: 'document_acceptance_workflow_button_save[button]', content: 'సేవ్'},
    {name: 'document_acceptance_workflow_button_cancel[button]', content: 'రద్దు'},
    {name: 'document_acceptance_workflow_button_delete[button]', content: 'డిలీట్  చేయు '},

    // doc status
    {name: 'document_acceptance_workflow_status_REJECTED[doc _card]', content: 'తిరస్కరించబడింది'}, 
    {name: 'document_acceptance_workflow_status_ACCEPTED[doc _card]', content: 'అంగీకరించబడింది'}, 
    {name: 'document_acceptance_workflow_status_PENDING[doc _card]', content: 'పెండింగ్లో'},
    {name: 'document_acceptance_workflow_status_SUBMITTED[doc _card]', content: 'సబ్మిట్ చేయబడింది'}, 
    {name: 'document_acceptance_workflow_status_DRAFT[doc _card]', content: 'డ్రాఫ్ట్'},
    {name: 'document_acceptance_workflow_status_RESUBMITTED[doc _card]', content: 'రీసబ్మిట్ చేయబడింది'}, 

    //
    // Doc Import

    {name: 'document_upload_upload_files_list_number', content: 'అప్‌లోడ్ చేయడానికి ఫైల్‌లు'}, 
    {name: 'document_upload_Tags_Number', content: 'టాగ్లు'}, 
    {name: 'document_upload_Recipients_NUmber', content: 'గ్రహీతలు'}, 
    {name: 'document_upload_Linked_Docs_NUmber', content: 'లింక్ చేసిన పత్రాలు'}, 
    {name: 'document_upload_Backup_Docs_NUmber', content: 'బ్యాకప్ పత్రాలు'}, 
    {name: 'document_upload_upload_button', content: 'అప్లోడ్'}, 
    {name: 'document_upload_Upload_Header', content: 'PDF డాక్యుమెంట్ దిగుమతి చేయండి '}, 
    {name: 'csv_user_batch_download_template_Header', content: 'CSV USER BATCH UPLOAD TEMPLATE'},
    {name: 'csv_org_batch_download_template_Header', content: 'CSV ORGANIZATION BATCH UPLOAD TEMPLATE'},
    {name: 'csv_user_batch_upload_Header', content: 'CSV USER BATCH UPLOAD'},
    {name: 'csv_org_batch_upload_Header', content: 'CSV ORGANIZATION BATCH UPLOAD'},
    {name: 'document_doc_info_Header', content: 'పత్ర సమాచారం'}, 

    // misc
    {name: 'document_filter_sort_newest', content: 'సరికొత్త'}, 
    {name: 'document_filter_sort_oldest', content: 'పురాతన'}, 
    {name: 'document_filter_sort_by_user_all', content: '-- అందరు వినియోగదారులు '}, 
    {name: 'document_filter_sort_by_doc_types_all', content: '-- అన్ని రకాల పత్రాలు'}, 
    {name: 'document_import_choose_doc_type', content: '-- డాక్ రకాన్ని ఎంచుకోండి'}, 
    {name: 'document_import_choose_group', content: '-- Choose Organization'}, 
    {name: 'document_import_choose_stage', content: '-- Choose Stage'}, 
    {name: 'document_search_tag_to_link', content: 'ట్యాగ్‌ను నమోదు చేయండి'}, 
    {name: 'document_search_tag_documents_found_to_link', content: 'కింది పత్రాలు మీ శోధన ట్యాగ్‌కు సరిపోతాయి'}, 
    {name: 'document_search_tag_documents_none_found_to_link', content: 'పత్రాలు లేవు'}, 

    // Stages Managements
    {name: 'stage.managements[Auto Translation]', content: 'ఆటో అనువాదం'}, // -->
    {name: 'stage.managements[Lock Stages]', content: 'సురక్షిత దశలు'},
    {name: 'stage.display.name', content: 'Stage Name'},

    
    // Document Preview
    {name: 'document.preview.pages', content: 'పేజీ పరిదృశ్యాన్ని నిలిపివేయండి'}, 
    {name: 'document.preview.pages.header', content: 'పేజీ పరిదృశ్యం'},  
    {name: 'document.preview.pages.doc.type.linked', content: 'లింక్ చేసిన పత్రాలు'},
    {name: 'document.preview.pages.doc.type.backup', content: 'బ్యాకప్ పత్రాలు'},   

    // Other
    {name: 'export_gps_data_button_label', content: 'డేటా ఎగుమతి'},
    {name: 'show_trace_map_button_label', content: 'Show Map'},
    {name: 'hide_trace_map_button_label', content: 'Hide Map'},
    {name: 'export_trace_data_button_label', content: 'ఎగుమతి ట్రేస్'},
    {name: 'manage_pdf_document_label_doc_type', content: 'డాక్యుమెంట్  రకం '},
    {name: 'manage_pdf_document_button_save', content: 'సేవ్'},
    {name: 'manage_pdf_document_button_submit', content: 'సబ్మిట్'},
    {name: 'manage_pdf_document_button_cancel', content: 'రద్దు'},
    {name: 'manage_pdf_document_button_choose_file', content: 'ఫైల్‌ను ఎంచుకోండి'},
    {name: 'manage_pdf_document_label_No_file_chosen', content: 'ఫైల్ ఎంపిక చెయ్యలేదు'},
    {name: 'manage_pdf_document_tags_button_search', content: 'సర్చ్'},
    {name: 'manage_pdf_document_tags_button_new', content: 'కొత్త'},
    {name: 'manage_pdf_document_label_uploading', content: 'ఫైళ్ళను అప్‌లోడ్ చేస్తోంది...'},

  ];

  public docUploadFlag: boolean = false;
  public csvUserBatchUploadFlag: boolean = false;
  public tagLinkDocSearchFlag: boolean = false;



  headerTraceabilityColors= [
    'shape-gray',
     'shape-light-green',
     'shape-light-blue',
     'shape-blue',
     'shape-light-green2',
     'shape-light-green2',
  ];

/** 
  headerTraceabilityColors= [
    '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray',
     '.shape-gray'
  ];
  */

  constructor(private http: Http, private _scrollToService: ScrollToService) {

  }

  /**
   * Get all the docuemnts from the backend
   * Add header option for username infromation to specify which user is getting the data
   */
  getAllDocuments(): Observable<Document[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL DOCUMENTS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_DOC_URL), options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

    /**
   * Get all the trace documents from the backend
   * Add header option for username infromation to specify which user is getting the data
   */
     getAllTraceDocuments(): Observable<Document[]> {
      let headers = new Headers({ 'Content-Type': 'text/plain' });
      headers.append('user-name', localStorage.getItem('username'));
      let options = new RequestOptions({ headers: headers });
  
      console.log('[Document Service] GET ALL TRACE DOCUMENTS RESTFUL '.concat(JSON.stringify(options)));
  
      return this.http.get(this.getServerURI().concat(this.ALL_TRACE_DOC_URL), options)
          .map( (res: Response) => res.json() )
          .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
    }

    /**
   * Get all the document types from the backend
   * Add header option for username infromation to specify which user is getting the data
   */
  getAllDocumentTypes(): Observable<DocumentType[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL DOCUMENT TYPES RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_DOC_TYPES_URL), options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

      /**
   * Get all the tags from the backend
   * Add header option for username infromation to specify which user is getting the data
   */
  getAllTags(): Observable<DocumentTag[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL TAGS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_TAGS_URL), options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  getServerURI() {
    //console.log('[Document Service] TEST JSON READ '.concat(JSON.stringify(this.serverConfig)));
    return localStorage.getItem(ServerUtils.BACK_END_SERVER_URL)
    //return this.serverConfig.accessUrl;
    //return AppGlobals.SERVER_URI;
  }

  getAllUsers(): Observable<User[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL USERS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_USER_URL), options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Get all teh recipients for the logged in user
   */
  getAllUserRecipients(): Observable<User[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL USER RECIPIENTS RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_USER_RECIPIENTS_URL), options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  getDocumentTypeIcon(type: string) {
    let result: string;
    for(var i=0 ; i < this.documentIcons.length; i++){
      if (this.documentIcons[i].type === type) {
        result = this.documentIcons[i].icon;
      }
    }

    return result;
  }

  markDocAsRead(doc: Document) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.MARK_DOC_AS_READ_URL)
        .replace('[:1]', '' + doc.syncID )
        .replace('[:2]', '' + localStorage.getItem('username'));

    console.log('[Document Service] MARK AS READ '.concat(URL_TO_CALL));

    return this.http.post(URL_TO_CALL, '', options)
        .map( (response: Response) => {
            console.log('[Document Service] POST MARK As READ '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  AddDocumentNote(note:NoteData, doc: Document) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    headers.append('doc-id', ''+doc.id);

    
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.ADD_DOC_NOTES_URL);

    console.log('[Document Service] ADD DOC NOTE '.concat(URL_TO_CALL));
    let body = JSON.stringify(note);
    console.log('[Document Service] ADD Document NOTE BODY'.concat(body));

    return this.http.post(URL_TO_CALL, body, options)
        .map( (response: Response) => {
            console.log('[Document Service] ADD Document NOTE '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  setDocumentStatus(doc: Document) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.SET_DOC_STATUS_URL)
        .replace('[:1]', '' + doc.syncID )
        .replace('[:2]', '' + localStorage.getItem('username'))
        .replace('[:3]', '' + doc.status)
        .replace('[:4]', '' + doc.updationTimestamp);

    console.log('[Document Service] SET STATUS '.concat(URL_TO_CALL));

    return this.http.post(URL_TO_CALL, '', options)
        .map( (response: Response) => {
            console.log('[Document Service] SET STATUS '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  exportTraceAsPDF(id : number) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.EXPORT_TRACE_AS_PDF_URL)
        .replace('[:1]', '' + id );

    console.log('[Document Service] Export Trace to PDF '.concat(URL_TO_CALL));

    return this.http.get(URL_TO_CALL, options)
        .map( (response: Response) => {
            console.log('[Document Service] get PDF Export '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  recallDocument(doc: Document) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.RECALL_DOC_URL)
        .replace('[:1]', '' + doc.syncID )
        .replace('[:2]', '' + localStorage.getItem('username'))

    console.log('[Document Service] RECALL '.concat(URL_TO_CALL));

    return this.http.post(URL_TO_CALL, '', options)
        .map( (response: Response) => {
            console.log('[Document Service] RECALL '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  deleteDocument(doc: Document) {
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.DELETE_DOC_URL)
        .replace('[:1]', '' + doc.syncID );

    console.log('[Document Service] DELETE '.concat(URL_TO_CALL));

    return this.http.delete(URL_TO_CALL, options)
        .map( (response: Response) => {
            console.log('[Document Service] DELETE '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  getDocumentGroupName(doc: Document) {
    let user: User;
    let result: string = 'N/A';
    user = JSON.parse(localStorage.getItem('user'));
    for(var i=0 ; i < user.userOrganizations[0].subGroups.length; i++) {
      if (user.userOrganizations[0].subGroups[i].id === doc.groupId) {
        result = user.userOrganizations[0].subGroups[i].name;
      }
    }
    return result;
  }

  getDocumentGroupGPS(doc: Document) {
    let user: User;
    let result: string = 'N/A';
    user = JSON.parse(localStorage.getItem('user'));
    for(var i=0 ; i < user.userOrganizations[0].subGroups.length; i++) {
      if (user.userOrganizations[0].subGroups[i].id === doc.groupId) {
        result = user.userOrganizations[0].subGroups[i].gpsCoordinates;
      }
    }
    return result;
  }

  getHeaderTraceabilityClassColor(lookUpIndex: number) {
    console.log('[Document Service] TRACEBILITY COLOR INDEX '.concat(""+lookUpIndex) + ' ' + this.headerTraceabilityColors[lookUpIndex]);
    return 'shape-gray';
    // return this.headerTraceabilityColors[lookUpIndex];
  }

  getCellTraceabilityClassColor(isComplete:boolean) {
    if(isComplete){
      return 'shape-green-organization';
    }else{
      return 'shape-red-organization';
    }
    // return this.headerTraceabilityColors[lookUpIndex];
  }

  getDocumentTraceById(id: number): Observable<Document[]>  {
    // create the full URL
    let URL_TO_CALL = this.getServerURI().concat(this.DOCUMENT_TRACE_BY_ID_URL).replace('[::]', '' + id);
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET DOCUMENT TRACE '.concat(JSON.stringify(options)));

    return this.http.get(URL_TO_CALL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));

  }

  getGroupsByOrganizationId(id: number): Observable<Group> {
    // create the full URL
    let URL_TO_CALL = this.getServerURI().concat(this.GROUPS_BY_ORGANIZATION_ID_URL).replace('[::]', '' + id);
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });

    return this.http.get(URL_TO_CALL, options)
        .map( (res: Response) => res.json() )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));

  }

  switchLanguage(lang: string){
    localStorage.setItem('language', lang);
  }

  getLanguage(){
    if(localStorage.getItem('language') === null){
      this.switchLanguage('English');
    }

    return localStorage.getItem('language');
  }

  /**
   * Internaionalize the given string key into a value
   * @param token 
   */
  internationalizeString(token: string){
    let result: string;
    
    if(localStorage.getItem('language') === 'Thai'){
      
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_th.length; i++){
        if (this.internationalize_th[i].name === token) {
          result = this.internationalize_th[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'th') != null){
        return LocaleUtils.getInternationalizedString(token, 'th');
      }
      
      return result;
    }


    if(localStorage.getItem('language') === 'Vietnamese'){
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_vi.length; i++){
        if (this.internationalize_vi[i].name === token) {
          result = this.internationalize_vi[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'vi') != null){
        result = LocaleUtils.getInternationalizedString(token, 'vi')
        console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
        return result;
      }
      
      //console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
      return result;
    }

    if(localStorage.getItem('language') === 'Spanish'){
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_sp.length; i++){
        if (this.internationalize_sp[i].name === token) {
          result = this.internationalize_sp[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'es') != null){
        result = LocaleUtils.getInternationalizedString(token, 'es')
        console.log('[Spanish] RESULTS'.concat(JSON.stringify(result)));
        return result;
      }
      
      //console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
      return result;
    }

    if(localStorage.getItem('language') === 'English'){

      //
      // Check locale data first
      let tempResult = LocaleUtils.getInternationalizedString(token, 'en');
      if(tempResult != null && tempResult != LocaleUtils.defaultValueForMissingKey){
        return LocaleUtils.getInternationalizedString(token, 'en');
      }
                  //
      // go from static data
      for(var i=0 ; i < this.internationalize_en.length; i++){
        if (this.internationalize_en[i].name === token) {
          result = this.internationalize_en[i].content;
          return result;
        }
      }

      return LocaleUtils.defaultValueForMissingKey;
    
    }

    if(localStorage.getItem('language') === 'Bahasa'){
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_in.length; i++){
        if (this.internationalize_in[i].name === token) {
          result = this.internationalize_in[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'in') != null){
        result = LocaleUtils.getInternationalizedString(token, 'in')
        console.log('[Bahasa] RESULTS'.concat(JSON.stringify(result)));
        return result;
      }
      
      //console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
      return result;
    
    }


    if(localStorage.getItem('language') === 'Hindi'){
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_hi.length; i++){
        if (this.internationalize_hi[i].name === token) {
          result = this.internationalize_hi[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'hi') != null){
        result = LocaleUtils.getInternationalizedString(token, 'hi')
        console.log('[Bahasa] RESULTS'.concat(JSON.stringify(result)));
        return result;
      }
      
      //console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
      return result;
    
    }

    if(localStorage.getItem('language') === 'Telugu'){
      //
      // go from static data
      for(var i=0 ; i < this.internationalize_te.length; i++){
        if (this.internationalize_te[i].name === token) {
          result = this.internationalize_te[i].content;
          return result;
        }
      }

      //
      // Check locale data first
      if(LocaleUtils.getInternationalizedString(token, 'te') != null){
        result = LocaleUtils.getInternationalizedString(token, 'te')
        console.log('[Bahasa] RESULTS'.concat(JSON.stringify(result)));
        return result;
      }
      
      //console.log('[Vietnamese] RESULTS'.concat(JSON.stringify(result)));
      return result;
    
    }
      return result;

  }
  reverseInternationalizeString(token: string){
    let result: string;
    if(localStorage.getItem('language') === 'Thai'){
      
      for(var i=0 ; i < this.internationalize_th.length; i++){
        if (this.internationalize_th[i].content === token) {
          result = this.internationalize_en[i].content;
          return result;
        }
      }
      
    }
    return token;
  }

  /**
   * Search for all documents (for linking) by a tag
   * @param tag  - the tag to search by
   */
  getAllLinkableDocsByTag(tag:string): Observable<Document[]> {
    let headers = new Headers({ 'Content-Type': 'text/plain' });
    headers.append('user-name', localStorage.getItem('username'));
    headers.append('custom_tag', tag);
    let options = new RequestOptions({ headers: headers });

    console.log('[Document Service] GET ALL DOCS By TAG RESTFUL '.concat(JSON.stringify(options)));

    return this.http.get(this.getServerURI().concat(this.ALL_DOCS_BY_TAG_URL), options)
        .map( (res: Response) => {
          // signal the end of the searching
          
          return res.json();
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Server Error' ));
  }

  /**
   * Get the actual string back into the identifier from the id
   * 
   * @param token the token to find and reverse
   */
  reverseInternationalizedNameStringToKeyString(token: string){
    let result: string = "";

    //
    // Check the local storage first
    result = LocaleUtils.fetchResourceKeyByValue(token);
    if(result != ""){
      return result;
    }

    if(localStorage.getItem('language') === 'Thai'){
      
      for(var i=0 ; i < this.internationalize_th.length; i++){
        if (this.internationalize_th[i].content === token) {
          result = this.internationalize_th[i].name;
          return result;
        }
      }
      
    }
    if(localStorage.getItem('language') === 'English'){
      
      for(var i=0 ; i < this.internationalize_en.length; i++){
        if (this.internationalize_en[i].content === token) {
          result = this.internationalize_en[i].name;
          return result;
        }
      }
      
    }

    if(localStorage.getItem('language') === 'Vietnamese'){
      
      for(var i=0 ; i < this.internationalize_vi.length; i++){
        if (this.internationalize_vi[i].content === token) {
          result = this.internationalize_vi[i].name;
          return result;
        }
      }
    }
    if(localStorage.getItem('language') === 'Spanish'){
      
      for(var i=0 ; i < this.internationalize_sp.length; i++){
        if (this.internationalize_sp[i].content === token) {
          result = this.internationalize_sp[i].name;
          return result;
        }
      }
    }

    if(localStorage.getItem('language') === 'Bahasa'){
      
      for(var i=0 ; i < this.internationalize_in.length; i++){
        if (this.internationalize_in[i].content === token) {
          result = this.internationalize_in[i].name;
          return result;
        }
      }
    }


    if(localStorage.getItem('language') === 'Hindi'){
      
      for(var i=0 ; i < this.internationalize_hi.length; i++){
        if (this.internationalize_hi[i].content === token) {
          result = this.internationalize_hi[i].name;
          return result;
        }
      }
    }

    if(localStorage.getItem('language') === 'Telugu'){
      
      for(var i=0 ; i < this.internationalize_te.length; i++){
        if (this.internationalize_te[i].content === token) {
          result = this.internationalize_te[i].name;
          return result;
        }
      }
    }

    //
    // Chech through utilities
    
    return token;
  }

  getStatusInternationalizedStringCode(status : string){
    if(status === 'REJECTED') return 'document_acceptance_workflow_status_REJECTED[doc _card]';
    if(status === 'ACCEPTED') return 'document_acceptance_workflow_status_ACCEPTED[doc _card]';
    if(status === 'PENDING') return 'document_acceptance_workflow_status_PENDING[doc _card]';
    if(status === 'SUBMITTED') return 'document_acceptance_workflow_status_SUBMITTED[doc _card]';
    if(status === 'DRAFT') return 'document_acceptance_workflow_status_DRAFT[doc _card]';
    if(status === 'RESUBMITTED') return 'document_acceptance_workflow_status_RESUBMITTED[doc _card]';
  }

  /**
   * Event handler for PDF file upload and coverstion in the server
   * @param file  - the file to upload to the server
   * @param formData - the data needed to do the file conversion
   */
  onFileUpload(file: string, formData: FormData, slimLoader: SlimLoadingBarService, thisService: DocumentService, component: DocumentsComponent){
    // headers
    let headers = new Headers({ 'Content-Type': 'multipart/form-data' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // usrl
    const URL_TO_CALL = this.getServerURI().concat(this.UPLOAD_DOC_AS_PDF_URL);
    console.log('[Data Load Service] POST - Upload a NewDoc URL'.concat(JSON.stringify(URL_TO_CALL)));
    console.log('[Data Load Service] POST - Upload a NewDoc Options'.concat(JSON.stringify(options)));
    console.log('[Data Load Service] POST - Upload a NewDoc FORM DATA'.concat(JSON.stringify(formData)));

    var oReq = new XMLHttpRequest();
    oReq.open("POST", URL_TO_CALL, true);
    oReq.onload = function(oEvent) {
      if (oReq.status == 200) {
        console.log('[Data Load Service] POST - Upload a NewDoc SUCCESS');
        var newDoc: Document = JSON.parse(oReq.response);
        console.log('[Data Load Service] POST - Upload a NewDoc SUCCESS --->' + JSON.stringify(newDoc));
        slimLoader.complete();
        thisService.stopUploadProcess();
        
        //component.docImportOn = false;
        
        // add to the component
        var newDocs = component.documents.slice(0);
        newDocs.push(newDoc);
        //component.documents = newDocs;
        component.currentDocument = newDoc;
        //component.resetFilter(1);
        component.toggleCreateNewDocButton();
        component.getAllDocuments(true, 0);
      } else {
        console.log('[Data Load Service] POST - Upload a NewDoc ERROR');
        thisService.stopUploadProcess();
      }
  };

  oReq.send(formData);
    
    // build the post reuqest with all data
    /**
    return this.http.post(URL_TO_CALL, formData, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Upload a NewDoc '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
*/
  }


   /**
   * Event handler for PDF file upload and coverstion in the server
   * @param file  - the file to upload to the server
   * @param formData - the data needed to do the file conversion
   */
  onFileUpdateUpload(file: string, formData: FormData, slimLoader: SlimLoadingBarService, thisService: DocumentService, component: DocumentsComponent){
    // headers
    let headers = new Headers({ 'Content-Type': 'multipart/form-data' });
    headers.append('user-name', localStorage.getItem('username'));
    let options = new RequestOptions({ headers: headers });
    // usrl
    const URL_TO_CALL = this.getServerURI().concat(this.UPLOAD_DOC_AS_PDF_URL);
    console.log('[Data Load Service] POST - Upload a NewDoc URL'.concat(JSON.stringify(URL_TO_CALL)));
    console.log('[Data Load Service] POST - Upload a NewDoc Options'.concat(JSON.stringify(options)));
    console.log('[Data Load Service] POST - Upload a NewDoc FORM DATA'.concat(JSON.stringify(formData)));

    var oReq = new XMLHttpRequest();
    oReq.open("PUT", URL_TO_CALL, true);
    oReq.onload = function(oEvent) {
      if (oReq.status == 200) {
        console.log('[Data Load Service] PUT - Upload a NewDoc SUCCESS');
        var newDoc: Document = JSON.parse(oReq.response);
        console.log('[Data Load Service] PUT - Upload a NewDoc SUCCESS --->' + JSON.stringify(newDoc));
        slimLoader.complete();
        thisService.stopUploadProcess();

        // add to the component
        

        //component.documents = newDocs;
        
        component.toggleEditDocButton();
        component.getAllDocuments(true, newDoc.id);

      } else {
        console.log('[Data Load Service] PUT - Upload a NewDoc ERROR');
        thisService.stopUploadProcess();
      }
    };

  oReq.send(formData);
    
    // build the post reuqest with all data
    /**
    return this.http.post(URL_TO_CALL, formData, options)
        .map( (response: Response) => {
            console.log('[Data Load Service] POST - Upload a NewDoc '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
*/
  }

  downloadPDFTraceFile(id: number) {

    const URL_TO_CALL = this.getServerURI().concat(this.EXPORT_TRACE_AS_PDF_URL)
        .replace('[:1]', '' + id );
    return this.http
      .get(URL_TO_CALL, {
        responseType: ResponseContentType.Blob,

      })
      .map(res => {
        return {
          filename: 'filename.pdf',
          data: res.blob()
        };
      })
      .subscribe(res => {
          console.log('start download:',res);
          var url = window.URL.createObjectURL(res.data);
          var a = document.createElement('a');
          document.body.appendChild(a);
          a.setAttribute('style', 'display: none');
          a.href = url;
          a.download = res.filename;
          a.click();
          window.URL.revokeObjectURL(url);
          a.remove(); // remove the element
        }, error => {
          console.log('download error:', JSON.stringify(error));
        }, () => {
          console.log('Completed file download.')
        });
  }

  downloadCSVTraceGPSDataFile(id: number) {

    const URL_TO_CALL = this.getServerURI().concat(this.EXPORT_TRACE_GPS_DATA_URL)
        .replace('[:1]', '' + id );
    return this.http
      .get(URL_TO_CALL, {
        responseType: ResponseContentType.Blob,

      })
      .map(res => {
        return {
          filename: 'filename.csv',
          data: res.blob()
        };
      })
      .subscribe(res => {
          console.log('start download:',res);
          var url = window.URL.createObjectURL(res.data);
          var a = document.createElement('a');
          document.body.appendChild(a);
          a.setAttribute('style', 'display: none');
          a.href = url;
          a.download = res.filename;
          a.click();
          window.URL.revokeObjectURL(url);
          a.remove(); // remove the element
        }, error => {
          console.log('download error:', JSON.stringify(error));
        }, () => {
          console.log('Completed file download.')
        });
  }
  
  isUploadingDoc(){
    return this.docUploadFlag;
  }

  startUploadProcess(){
    this.docUploadFlag = true;
  }

  startCSVUserBatchUploadProcess(){
    this.csvUserBatchUploadFlag = true;
  }

  stopUploadProcess(){
    this.docUploadFlag = false;
  }

  isTagLinkDocSearching(){
    return this.tagLinkDocSearchFlag;
  }

  startTagLinkDocSearching(){
    this.tagLinkDocSearchFlag = true;
  }

  stopTagLinkDocSearching(){
    this.tagLinkDocSearchFlag = false;
  }

  getDocDynamicDefinitionsByType(docTypeId : number){
    // get the definition from session data
    var result  : DynamicFieldDefinition[] = Array<DynamicFieldDefinition>();

    var user : User = JSON.parse(localStorage.getItem('user'));
    for(var i=0 ; i < user.dynamicFieldDefinitions.length; i++) {
      if (user.dynamicFieldDefinitions[i].docTypeId === docTypeId) {
        result.push(user.dynamicFieldDefinitions[i]);
      }
    }

    return result;
  }

  getNewDocInfoDataByType(docTypeId : number){
    // get the definition from session data
    var result  : DynamicFieldData[] = Array<DynamicFieldData>();

    var user : User = JSON.parse(localStorage.getItem('user'));
    for(var i=0 ; i < user.dynamicFieldDefinitions.length; i++) {
      if (user.dynamicFieldDefinitions[i].docTypeId === docTypeId) {
        var data = new DynamicFieldData();
        data.dynamicFieldDefinitionId = user.dynamicFieldDefinitions[i].fieldTypeId
        data.fieldDisplayNameValue = user.dynamicFieldDefinitions[i].displayName
        result.push(data);
      }
    }

    return result;
  }

  getNewDocInfoDataErrors(docTypeId : number){
    // get the definition from session data
    var result  : ApplicationErrorData[] = Array<ApplicationErrorData>();

    var user : User = JSON.parse(localStorage.getItem('user'));
    for(var i=0 ; i < user.dynamicFieldDefinitions.length; i++) {
      if (user.dynamicFieldDefinitions[i].docTypeId === docTypeId) {
        var data = new ApplicationErrorData();
        result.push(data);
      }
    }

    return result;
  }

  deleteDocPages(doc: Document, pages: Page[]) {
    var pageIds: number[] = new Array<number>();
    //
    // extract the page ids
    

    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    for(var i=0 ; i < pages.length; i++) {
      pageIds.push(pages[i].id);
    }
    let body = JSON.stringify(pageIds);
    let options = new RequestOptions({ headers: headers, body: body });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.DELETE_DOC_PAGES_URL)
        .replace('[:1]', '' + doc.syncID )

    console.log('[Document Service] DELETE PAGES '.concat(URL_TO_CALL));

    return this.http.delete(URL_TO_CALL, options)
        .map( (response: Response) => {
            console.log('[Document Service] DELETE PAGES '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }

  deleteDocPage(doc: Document, pageId: number) {
    var pageIds: number[] = new Array<number>();
    let headers = new Headers({ 'Content-Type': 'application/json' });
    headers.append('user-name', localStorage.getItem('username'));
    pageIds.push(pageId);
    let body = JSON.stringify(pageIds);
    let options = new RequestOptions({ headers: headers, body: body });
    // create the URL
    const URL_TO_CALL = this.getServerURI().concat(this.DELETE_DOC_PAGES_URL)
        .replace('[:1]', '' + doc.syncID )

    console.log('[Document Service] DELETE PAGE '.concat(URL_TO_CALL));
    console.log('[Document Service] DELETE PAGE <options> '.concat(JSON.stringify(body)));

    return this.http.delete(URL_TO_CALL, options)
        .map( (response: Response) => {
            console.log('[Document Service] DELETE PAGE '.concat(JSON.stringify(response.json())));
            return response;
        } )
        .catch( (error: any) => Observable.throw(error.json().error || 'Unexpected Authentication Error' ));
  }
  

}

