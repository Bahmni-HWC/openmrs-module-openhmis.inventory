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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.UserServiceImpl;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.openhmis.inventory.api.IItemDataService;
import org.openmrs.module.openhmis.inventory.api.impl.ItemDataServiceImpl;
import org.openmrs.module.openhmis.inventory.api.model.Department;
import org.openmrs.module.openhmis.inventory.api.model.Item;
import org.openmrs.module.openhmis.inventory.api.model.ItemPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class InventoryModuleActivator extends BaseModuleActivator {

	private static final Log LOG = LogFactory.getLog(InventoryModuleActivator.class);

	/*@Autowired
	private IItemDataService dataService;*/

	/*Logger logger = (Logger) LogManager.getLogger(InventoryModuleActivator.class);*/

	/**
	 * @see BaseModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {
		LOG.info("OpenHMIS Inventory Module refreshed");
	}

	/**
	 * @see BaseModuleActivator#started()
	 */
	@Override
	public void started() {
		try {
			getInventorydata();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.info("OpenHMIS Inventory Module started");
	}

	/**
	 * @see BaseModuleActivator#stopped()
	 */
	@Override
	public void stopped() {
		LOG.info("OpenHMIS Inventory Module stopped");

	}

	public void getInventorydata() throws IOException {

		// private final String baseUrl = 'http://localhost/openmrs/ws/rest/v2/inventory/item';
		final InputStream filepathabsolute = getClass().getClassLoader().getResourceAsStream("inv_item.csv");
		//CSVFile persistedUploadedFile = writeToLocalFile(file, filesDirectory);
		Scanner sc;
		InputStream is = getClass().getClassLoader().getResourceAsStream("inv_item.csv");

		//LOG.error("reading from resource....." + is);

		sc = new Scanner(new File("/openmrs/data/.openmrs-lib-cache/openhmis.inventory/inv_item.csv"));

		LOG.error("before list......." + sc);

		List<String> items = new ArrayList<>();

		sc.useDelimiter(","); //sets the delimiter pattern
		String lineData = "";
		sc.nextLine();//skip header line
		while (sc.hasNextLine()) {
			lineData = sc.nextLine();
			LOG.error("inside while............" + lineData);
			addItem(lineData);
			/*items.add(sc.toString());
			LOG.error("Adding items............." + items);*/
			//System.out.print(sc.next());  //find and returns the next complete token from this scanner
		}

		//invokeUsingRestTemplate();
		sc.close(); //closes the scanner
	}

	public void addItem(String lineData) {

		//String url = "http://openmrs:8080/openmrs/ws/rest/v2/inventory/item";
		/*String username = "superman";
		String password = "Admin123";
		String jsonData =
		        "{\"name\":\"crocin10\",\"department\":\"2c37c6c1-64c0-4ee7-ae30-bc068b6deae1\",\"hasExpiration\":false,\"defaultExpirationPeriod\":null,\"concept\":\"\",\"hasPhysicalInventory\":false,\"minimumQuantity\":null,\"buyingPrice\":null,\"codes\":[],\"prices\":[{\"name\":\"test\",\"price\":10}],\"defaultPrice\":{\"name\":\"test\",\"price\":10}}";
		*/
		Item newItem = new Item();
		UserService userService = new UserServiceImpl();

		Department newDepartment = new Department();
		Concept concept = new Concept();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ItemPrice price = new ItemPrice();
		String[] lineValues = lineData.split(",");
		newItem.setId(Integer.valueOf(lineValues[0]));
		newItem.setName(lineValues[1]);
		newItem.setDescription(lineValues[2]);
		newItem.setHasExpiration(Boolean.valueOf(lineValues[5]));
		concept.setConceptId(Integer.valueOf(lineValues[6]));
		newItem.setConcept(concept);
		User user = userService.getUser(Integer.valueOf(lineValues[7]));
		newItem.setCreator(user);
		try {
			newItem.setDateCreated(sdf.parse(lineValues[8]));
		} catch (ParseException e) {
			LOG.error("Invalid Date format for Creation Date field");
		}

		newDepartment.setId((Integer.valueOf(lineValues[3])));
		newItem.setDepartment(newDepartment);
		price.setPrice(BigDecimal.valueOf(Long.parseLong(lineValues[4])));
		newItem.setDefaultPrice(price);

		ItemDataServiceImpl dataService = new ItemDataServiceImpl();
		LOG.error("newItem......" + newItem);

		dataService.save(newItem);

	}
}
