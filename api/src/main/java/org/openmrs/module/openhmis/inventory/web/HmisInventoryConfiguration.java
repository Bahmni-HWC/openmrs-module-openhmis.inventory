/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.openhmis.inventory.web;

import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * This is done as replacement of DefaultAnnotationHandlerMapping configured through spring context file
 */
@Configuration
public class HmisInventoryConfiguration {
	/**
	 * The DefaultAnnotationHandlerMapping class was deprecated and eventually removed in Spring 5 The recommended
	 * replacement class RequestMappingHandlerMapping was introduced in Spring 3.1.0 which is not available on OpenMRS
	 * platform versions 1.9.x and 1.1.0.x which run Spring 3.0.5 That's why we can't just statically replace this class in
	 * the webModuleApplicationContext.xml file.
	 */
	@Bean
	public AbstractHandlerMapping gettHandlerMapping() throws Exception {

		Class<?> clazz;
		if (ModuleUtil.compareVersion(OpenmrsConstants.OPENMRS_VERSION_SHORT, "2.4") < 0) {
			clazz = Context.loadClass("org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping");
		} else {
			clazz = Context.loadClass("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping");
		}

		return (AbstractHandlerMapping)clazz.newInstance();
	}
}
