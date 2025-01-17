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
package org.openmrs.module.openhmis.inventory.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Privilege;
import org.openmrs.api.UserService;
import org.openmrs.module.openhmis.commons.model.RoleCreationViewModel;
import org.openmrs.module.openhmis.commons.web.controller.RoleCreationControllerBase;
import org.openmrs.module.openhmis.inventory.api.util.PrivilegeConstants;
import org.openmrs.module.openhmis.inventory.web.ModuleWebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * Controller for the inventory role creation page.
 */
@Controller
@RequestMapping(ModuleWebConstants.ROLE_CREATION_ROOT)
public class InventoryRoleController extends RoleCreationControllerBase {
	private static final Log LOG = LogFactory.getLog(InventoryRoleController.class);

	private UserService userService;

	@Autowired
	public InventoryRoleController(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserService getUserService() {
		return this.userService;
	}

	@Override
	public Set<Privilege> privileges() {
		return PrivilegeConstants.getDefaultPrivileges();
	}

	@RequestMapping(method = { RequestMethod.GET })
	@Override
	public void render(ModelMap model, HttpServletRequest request) throws IOException {
		super.render(model, request);
	}

	@RequestMapping(method = { RequestMethod.POST })
	@Override
	public void submit(HttpServletRequest request, RoleCreationViewModel viewModel,
	        Errors errors, ModelMap model) throws IOException {
		super.submit(request, viewModel, errors, model);
	}
}
