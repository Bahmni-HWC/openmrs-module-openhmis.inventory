package org.openmrs.module.webservices.rest.resource;

import java.util.List;

import org.openmrs.module.openhmis.commons.api.entity.IObjectDataService;
import org.openmrs.module.openhmis.inventory.api.model.ItemStockSummary;
import org.openmrs.module.openhmis.inventory.model.InventoryStockTake;
import org.openmrs.module.openhmis.inventory.web.ModuleRestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Resource(name = ModuleRestConstants.INVENTORY_STOCK_TAKE_RESOURCE, supportedClass = InventoryStockTake.class,
        supportedOpenmrsVersions = { "1.9.*", "1.10.*", "1.11.*" })
public class InventoryStockTakeResource extends BaseRestObjectResource<InventoryStockTake> {
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		description.removeProperty("name");
		description.removeProperty("description");
		description.addProperty("operationNumber");
		description.addProperty("stockroom");
		description.addProperty("itemStockSummaryList");
		
		return description;
	}
	
	@PropertySetter(value = "operationNumber")
	public void setOperationNumber(InventoryStockTake instance, String operationNumber) {
		System.out.println("dddddddddddd");
		instance.setOperationNumber(operationNumber);
	}
	
	@PropertySetter(value = "itemStockSummaryList")
	public void setInventoryStockTakeList(InventoryStockTake instance, List<ItemStockSummary> list) {
		System.out.println("ffffffffffff");
		instance.setItemStockSummaryList(list);
	}
	
	@Override
	public InventoryStockTake newDelegate() {
		return new InventoryStockTake();
	}
	
	@Override
	public InventoryStockTake save(InventoryStockTake delegate) {
		System.out.println("lalalalalalal");
		return delegate;
	}
	
	@Override
	public Class<? extends IObjectDataService<InventoryStockTake>> getServiceClass() {
		return null;
	}
}