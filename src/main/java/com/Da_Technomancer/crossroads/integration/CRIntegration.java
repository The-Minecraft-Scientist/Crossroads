package com.Da_Technomancer.crossroads.integration;

import com.Da_Technomancer.crossroads.integration.curios.CurioHelperSafe;
import com.Da_Technomancer.crossroads.items.CRItems;
import com.Da_Technomancer.essentials.integration.ESIntegration;

public class CRIntegration{

	public static void init(){
		ESIntegration.bookTabs.add(CRItems.TAB_CROSSROADS);
		ESIntegration.bookName = "book.crossroads_essentials.name";

		CurioHelperSafe.initIntegration();
	}
}
