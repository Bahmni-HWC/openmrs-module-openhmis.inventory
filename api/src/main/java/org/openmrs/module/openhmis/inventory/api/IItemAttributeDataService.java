/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and
 * limitations under the License.
 *
 * Copyright (C) OpenHMIS.  All Rights Reserved.
 */
package org.openmrs.module.openhmis.inventory.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.openhmis.commons.api.PagingInfo;
import org.openmrs.module.openhmis.commons.api.entity.IMetadataDataService;
import org.openmrs.module.openhmis.inventory.api.model.ItemAttribute;
import org.openmrs.module.openhmis.inventory.api.model.ItemAttributeType;
import org.openmrs.module.openhmis.inventory.api.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Interface that represents classes which perform data operations for {@link ItemAttribute}s.
 */
@Transactional
public interface IItemAttributeDataService extends IMetadataDataService<ItemAttribute> {

	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_ITEMS })
	List<ItemAttribute> getItemsByAttributeTypeAndValue(ItemAttributeType attributeType, String value,
	        boolean includeRetired, PagingInfo pagingInfo);

}
