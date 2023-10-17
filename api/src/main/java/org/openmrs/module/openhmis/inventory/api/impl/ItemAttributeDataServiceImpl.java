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
package org.openmrs.module.openhmis.inventory.api.impl;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.openhmis.commons.api.PagingInfo;
import org.openmrs.module.openhmis.commons.api.entity.impl.BaseMetadataDataServiceImpl;
import org.openmrs.module.openhmis.commons.api.entity.security.IMetadataAuthorizationPrivileges;
import org.openmrs.module.openhmis.commons.api.f.Action1;
import org.openmrs.module.openhmis.inventory.api.IItemAttributeDataService;
import org.openmrs.module.openhmis.inventory.api.model.ItemAttribute;
import org.openmrs.module.openhmis.inventory.api.model.ItemAttributeType;
import org.openmrs.module.openhmis.inventory.api.security.BasicMetadataAuthorizationPrivileges;
import org.openmrs.module.openhmis.inventory.api.util.HibernateCriteriaConstants;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.criterion.Order;

import java.util.List;

/**
 * Data service implementation class for {@link ItemAttributeType}s.
 */
@Transactional
public class ItemAttributeDataServiceImpl extends BaseMetadataDataServiceImpl<ItemAttribute>
        implements IItemAttributeDataService {
	@Override
	protected IMetadataAuthorizationPrivileges getPrivileges() {
		return new BasicMetadataAuthorizationPrivileges();
	}

	@Override
	protected void validate(ItemAttribute entity) {
		return;
	}

	@Override
	protected Order[] getDefaultSort() {
		return new Order[] { Order.asc("id") };
	}

	public List<ItemAttribute> getItemsByAttributeTypeAndValue(final ItemAttributeType attributeType,
	        final String value,
	        final boolean includeRetired,
	        PagingInfo pagingInfo) {

		if (attributeType == null || value == null) {
			throw new NullPointerException("The attributeType and value must be defined");
		}

		return executeCriteria(ItemAttribute.class, pagingInfo, new Action1<Criteria>() {
			@Override
			public void apply(Criteria criteria) {
				criteria.add(Restrictions.eq(HibernateCriteriaConstants.ATTRIBUTE_TYPE, attributeType));
				criteria.add(Restrictions.eq("value", value));
				if (!includeRetired) {
					criteria.add(Restrictions.eq(HibernateCriteriaConstants.RETIRED, false));
				}
			}
		}, getDefaultSort());
	}

}
