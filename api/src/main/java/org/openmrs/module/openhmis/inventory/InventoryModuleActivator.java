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

import liquibase.util.csv.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.openhmis.inventory.api.IItemAttributeTypeDataService;
import org.openmrs.module.openhmis.inventory.api.IItemDataService;
import org.openmrs.module.openhmis.inventory.api.model.*;
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
    public static final String INVENTORY_ITEMS_FILE = "/etc/bahmni_config/openmrs/inventory/inv_item.csv";
    public static final String TRUE_VALUE_INT = "1";
    public static final String TRUE_VALUE_STR = "yes";

    private static final String E_AUSHADHA_ATTRIBUTE_UUID = "ab9f163c-b576-4a34-8cab-11b0348589f3";
    private static final String DRUG_ATTRIBUTE_UUID = "889d51e2-b0c5-4064-9db0-3cccc9bb7f6d";

    private IItemDataService iItemDataService;
    private ConceptService conceptService;
    private IItemAttributeTypeDataService iItemAttributeTypeDataService;

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
        iItemAttributeTypeDataService = Context.getService(IItemAttributeTypeDataService.class);
        conceptService = Context.getConceptService();
        processInventoryData();
    }

    private List<String[]> readInventoryFile() {
        List<String[]> fileEntries = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(INVENTORY_ITEMS_FILE))) {
            reader.readNext();
            String[] lineEntry;
            while ((lineEntry = reader.readNext()) != null) {
                fileEntries.add(lineEntry);
            }
        } catch (IOException e) {
            LOG.info("Error reading the inventory file at " + INVENTORY_ITEMS_FILE);
            e.printStackTrace();
        }
        return fileEntries;
    }

    public void processInventoryData() {

        List<String[]> lineEntries = readInventoryFile();
        ItemAttributeType eAushadhaItemAttribute = iItemAttributeTypeDataService.getByUuid(E_AUSHADHA_ATTRIBUTE_UUID);
        ItemAttributeType drugItemAttribute = iItemAttributeTypeDataService.getByUuid(DRUG_ATTRIBUTE_UUID);
        if (eAushadhaItemAttribute == null || drugItemAttribute == null) {
            LOG.error("Item Attribute Types not found");
            return;
        }

        for (String[] lineEntry : lineEntries) {
            LOG.warn("Processing inv_item line entry : " + lineEntry[1]);
            try {
                if (iItemDataService.getItemByUuid(lineEntry[0]) != null) {
                    LOG.error("Inventory Item already exists: " + lineEntry[1]);
                    continue;
                }
                Item item = createItemEntity(lineEntry, eAushadhaItemAttribute, drugItemAttribute);
                iItemDataService.save(item);
                LOG.info("Inventory Item Sucessfully saved: " + item.getUuid());
            } catch (Exception e) {
                LOG.error("Error while processing inv_item line entry: " + lineEntry[1], e);
            }

        }
    }

    private Item createItemEntity(String[] lineValues, ItemAttributeType eAushadhaItemAttributeType,
                                  ItemAttributeType drugItemAttributeType) {

        if (lineValues.length < 22) {
            throw new InvalidInventoryDataException("Invalid number of values in input file");
        }
        Item newItem = new Item();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            if (StringUtils.isEmpty(lineValues[1])) {
                throw new InvalidInventoryDataException("Name value missing in input file");
            }
            newItem.setUuid(lineValues[0]);
            newItem.setName(lineValues[1]);
            newItem.setDescription(lineValues[2]);
            newItem.setDepartment(getDepartment(lineValues[3]));
            newItem.setDefaultPrice(getDefaultItemPrice(lineValues[4], newItem));
            newItem.setHasExpiration(getBooleanFlagFromString(lineValues[5]));
            newItem.setConcept(getConcept(lineValues[6]));
            newItem.setCreator(getUser(lineValues[7], false));
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
            newItem.setHasPhysicalInventory(getBooleanFlagFromString(lineValues[15]));
            newItem.setDefaultExpirationPeriod(StringUtils.isEmpty(lineValues[16]) ? null : Integer.valueOf(lineValues[16]));
            newItem.setConceptAccepted(getBooleanFlagFromString(lineValues[17]));
            newItem.setMinimumQuantity(StringUtils.isEmpty(lineValues[18]) ? null : Integer.valueOf(lineValues[18]));
            newItem.setBuyingPrice(StringUtils.isEmpty(lineValues[19]) ? null : BigDecimal.valueOf(Long
                    .parseLong(lineValues[19])));
            String drugItemAttributeValue = lineValues[20];
            String eAushadhaItemAttributeValue = lineValues[21];
            if (!eAushadhaItemAttributeValue.isEmpty()) {
                ItemAttribute eAushadhaItemAttribute = new ItemAttribute();
                eAushadhaItemAttribute.setAttributeType(eAushadhaItemAttributeType);
                eAushadhaItemAttribute.setValue(eAushadhaItemAttributeValue);
                eAushadhaItemAttribute.setOwner(newItem);
                eAushadhaItemAttribute.setCreator(newItem.getCreator());
                newItem.addAttribute(eAushadhaItemAttribute);
            }
            if (!drugItemAttributeValue.isEmpty()) {
                ItemAttribute drugItemAttribute = new ItemAttribute();
                drugItemAttribute.setAttributeType(drugItemAttributeType);
                drugItemAttribute.setValue(getDrugIdByName(drugItemAttributeValue));
                drugItemAttribute.setOwner(newItem);
                drugItemAttribute.setCreator(newItem.getCreator());
                newItem.addAttribute(drugItemAttribute);
            }
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

    private Concept getConcept(String conceptName) {
        if (StringUtils.isEmpty(conceptName)) {
            return null;
        }
        try {
            return conceptService.getConcept(conceptName);
        } catch (Exception e) {
            LOG.error("Error while fetching concept for concept name: " + conceptName, e);
            throw new InvalidInventoryDataException("Error while fetching concept for concept name: " + conceptName);
        }

    }

    private String getDrugIdByName(String drugName) {
        if (StringUtils.isEmpty(drugName)) {
            return null;
        }
        try {
            return conceptService.getDrug(drugName).getDrugId().toString();
        } catch (Exception e) {
            LOG.error("Error while fetching drug id for drug name: " + drugName, e);
            throw new InvalidInventoryDataException("Error while fetching drug id for drug name: " + drugName);
        }
    }

    private boolean getBooleanFlagFromString(String lineValue) {
        return StringUtils.isNotEmpty(lineValue) &&
                (TRUE_VALUE_INT.equalsIgnoreCase(lineValue) ||
                        TRUE_VALUE_STR.equalsIgnoreCase(lineValue) ||
                        Boolean.parseBoolean(lineValue));
    }

    private ItemPrice getDefaultItemPrice(String price, Item item) {
        if (StringUtils.isEmpty(price)) {
            throw new InvalidInventoryDataException("Default Price value missing in input file");
        }
        ItemPrice itemPrice = new ItemPrice();
        itemPrice.setItem(item);
        itemPrice.setPrice(BigDecimal.valueOf(Long.parseLong(price)));
        return itemPrice;
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
