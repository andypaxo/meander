package net.softwarealchemist.meander;

import net.softwarealchemist.meander.util.BoundingBox;
import net.softwarealchemist.meander.util.TriggerArea;

public class ChangeZoneTrigger extends TriggerArea {

	MeanderRenderer renderer;
	
	public ChangeZoneTrigger(BoundingBox area, MeanderRenderer renderer) {
		this.area = area;
		this.renderer = renderer;
	}

	@Override
	public void doAction() {
		
	}

}
