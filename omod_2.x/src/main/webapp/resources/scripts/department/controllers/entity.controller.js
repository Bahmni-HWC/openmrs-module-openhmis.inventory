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
(function() {
    'use strict';

    var base = angular.module('app.genericEntityController');
    base.controller("EntityController", EntityController);
    EntityController.$inject = ['$stateParams', '$injector', '$scope', '$filter', 'EntityRestFactory', 'DepartmentModel'];

    var ENTITY_NAME = "department";

    function EntityController($stateParams, $injector, $scope, $filter, EntityRestFactory, DepartmentModel) {
        var self = this;

        var module_name = 'inventory';
        var entity_name = emr.message("openhmis.inventory." + ENTITY_NAME + ".name");
        var cancel_page = 'entities.page';
        var rest_entity_name = ENTITY_NAME;

        // @Override
        self.setRequiredInitParameters = self.setRequiredInitParameters || function() {
                self.bindBaseParameters(module_name, rest_entity_name, entity_name, cancel_page);
            };

        /**
         * Initializes and binds any required variable and/or function specific to entity.page
         * @type {Function}
         */
        // @Override
        self.bindExtraVariablesToScope = self.bindExtraVariablesToScope
            || function(uuid) {
                if (angular.isDefined($scope.entity) && angular.isDefined($scope.entity.retired)
                    && $scope.entity.retired === true) {
                    $scope.retireOrUnretire = $filter('EmrFormat')(emr.message("openhmis.inventory.general.unretire"),
                        [self.entity_name]);
                } else {
                    $scope.retireOrUnretire = $filter('EmrFormat')(emr.message("openhmis.inventory.general.retire"),
                        [self.entity_name]);
                }

                /* bind variables.. */
                $scope.uuid = uuid;
            };

        /**
         * All post-submit validations are done here.
         * @return boolean
         */
        // @Override
        self.validateBeforeSaveOrUpdate = self.validateBeforeSaveOrUpdate || function() {
                if (!angular.isDefined($scope.entity.name) || $scope.entity.name === '') {
                    $scope.submitted = true;
                    return false;
                }

                return true;
            };

        /* ENTRY POINT: Instantiate the base controller which loads the page */
        $injector.invoke(base.GenericEntityController, self, {
            $scope: $scope,
            $filter: $filter,
            $stateParams: $stateParams,
            EntityRestFactory: EntityRestFactory,
            GenericMetadataModel: DepartmentModel
        });
    }
})();