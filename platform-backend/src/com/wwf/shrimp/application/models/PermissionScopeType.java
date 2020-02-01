package com.wwf.shrimp.application.models;

/**
 * The scope of the permission for a given permission.
 * For example "OWNER_ONLY" would mean that only the owner of 
 * the resource would have the associated permission.
 *
 * We can this of this as visibility within a profile hierarchy.
 * @author AleaActaEst
 *
 */
public enum PermissionScopeType {
	//Permission is only valid in the scope of the owner/creator
	OWNER_ONLY, 
	
	// Permission is valid in the scope of a group only
	GROUP, 
	
	// permission is valid for the group and all its super-groups (i.e. groups above it)
	SUPER_GROUP, 
	
	// The permission is global in scope.
	SUPER
}
