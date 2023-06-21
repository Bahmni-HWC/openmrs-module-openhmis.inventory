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
package org.openmrs.module.openhmis.inventory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.openhmis.inventory.api.IItemDataService;
import org.openmrs.module.openhmis.inventory.api.model.Department;
import org.openmrs.module.openhmis.inventory.api.model.Item;
import org.openmrs.module.openhmis.inventory.api.model.ItemPrice;
import org.openmrs.module.openhmis.inventory.exception.InvalidInventoryDataException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import java.io.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.IOException;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */

public class InventoryModuleActivator extends BaseModuleActivator {

	private static final Log LOG = LogFactory.getLog(InventoryModuleActivator.class);
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATA_DELIMITER = ",";
	public static final String INVENTORY_ITEMS_FILE = "inv_item.csv";
	public static final String TRUE_VALUE_INT = "1";
	public static final String TRUE_VALUE_STR = "yes";

	IItemDataService iItemDataService;

	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	@Autowired
	@Transactional
	public void started() {
		LOG.info("OpenHMIS Inventory Module started");

	}

	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {

		LOG.info("OpenHMIS Inventory Module stopped");

	}

	/**
	 * @see BaseModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {

		LOG.info("OpenHMIS Inventory Module refreshed");
		iItemDataService = Context.getService(IItemDataService.class);

		processInventoryData();
	}

	private List<String> readInventoryFile() {
		InputStreamReader is = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(INVENTORY_ITEMS_FILE));
		List<String> fileEntries = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(is)) {
			String lineEntry;
			while ((lineEntry = br.readLine()) != null)
				fileEntries.add(lineEntry);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileEntries.remove(0);
		return fileEntries;
	}

	public void processInventoryData() {

		List<String> lineEntries = readInventoryFile();

		for (String lineEntry : lineEntries) {
			Item item = createItemEntity(lineEntry.split(DATA_DELIMITER));
			iItemDataService.save(item);
		}
	}

	private Item createItemEntity(String[] lineValues) {
		Item newItem = new Item();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			if (StringUtils.isNotEmpty(lineValues[0])) {
				newItem.setId(Integer.valueOf(lineValues[0]));
			}
			if (StringUtils.isEmpty(lineValues[1])) {
				throw new InvalidInventoryDataException("Department value missing in input file");
			}
			newItem.setName(lineValues[1]);
			newItem.setDescription(lineValues[2]);
			newItem.setDepartment(getDepartment(lineValues[3]));
			newItem.setDefaultPrice(getDefaultItemPrice(lineValues[4]));
			newItem.setHasExpiration(getBooleanFlagFromString(lineValues[5]));
			newItem.setConcept(getConcept(lineValues[6]));
			newItem.setCreator(getUser(lineValues[7], false));
			//set today's date if the date created is missing in the input file
			newItem.setDateCreated(getCurrentDateIfEmpty(lineValues[8]));
			newItem.setChangedBy(getUser(lineValues[9], true));
			newItem.setDateChanged(getCurrentDateIfEmpty(lineValues[10]));
			newItem.setRetired(getBooleanFlagFromString(lineValues[11]));
			newItem.setRetiredBy(getUser(lineValues[12], true));
			newItem.setDateRetired(StringUtils.isEmpty(lineValues[13]) ? null : sdf.parse(lineValues[13]));
			newItem.setRetireReason(lineValues[14]);
			if (StringUtils.isEmpty(lineValues[15])) {
				throw new InvalidInventoryDataException("UUID value missing in input file");
			}
			newItem.setUuid(lineValues[15]);
			newItem.setHasPhysicalInventory(getBooleanFlagFromString(lineValues[16]));
			newItem.setDefaultExpirationPeriod(StringUtils.isEmpty(lineValues[17]) ? null : Integer.valueOf(lineValues[17]));
			newItem.setConceptAccepted(getBooleanFlagFromString(lineValues[18]));
			newItem.setMinimumQuantity(StringUtils.isEmpty(lineValues[19]) ? null : Integer.valueOf(lineValues[19]));
			newItem.setBuyingPrice(BigDecimal.valueOf(Long.parseLong(lineValues[20])));

		} catch (NumberFormatException ex) {
			LOG.error("Invalid value found in input file", ex);
			throw new InvalidInventoryDataException("Invalid value found in input file" + ex.getMessage());
		} catch (ParseException e) {
			LOG.error("Invalid Date format for Creation Date field", e);
			throw new InvalidInventoryDataException("Invalid date format in input file" + e.getMessage());
		}
		return newItem;
	}

	private User getUser(String userId, boolean optional) {
		if (StringUtils.isEmpty(userId) && optional) {
			return null;
		}
		User creator = new User();
		creator.setId(Integer.valueOf(userId));
		return creator;
	}

	private Concept getConcept(String conceptId) {
		if (StringUtils.isEmpty(conceptId)) {
			return null;
		}
		Concept concept = new Concept();
		concept.setConceptId(Integer.valueOf(conceptId));
		return concept;
	}

	private boolean getBooleanFlagFromString(String lineValue) {
		return StringUtils.isNotEmpty(lineValue) &&
		        (TRUE_VALUE_INT.equalsIgnoreCase(lineValue) ||
		                TRUE_VALUE_STR.equalsIgnoreCase(lineValue) ||
		        Boolean.parseBoolean(lineValue));
	}

	private ItemPrice getDefaultItemPrice(String priceId) {
		if (StringUtils.isEmpty(priceId)) {
			throw new InvalidInventoryDataException("Default Price value missing in input file");
		}
		ItemPrice price = new ItemPrice();
		price.setId(Integer.valueOf(priceId));
		return price;
	}

	private Department getDepartment(String lineValue) {
		if (StringUtils.isEmpty(lineValue)) {
			throw new InvalidInventoryDataException("Department value missing in input file");
		}
		Department newDepartment = new Department();
		newDepartment.setId((Integer.valueOf(lineValue)));
		return newDepartment;
	}

	private Date getCurrentDateIfEmpty(String dateStr) throws ParseException {
		if (StringUtils.isEmpty(dateStr)) {
			return new Date();
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.parse(dateStr);
	}
}
