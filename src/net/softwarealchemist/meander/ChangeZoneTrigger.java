package net.softwarealchemist.meander;

import net.softwarealchemist.meander.util.BoundingBox;
import net.softwarealchemist.meander.util.TriggerArea;

public class ChangeZoneTrigger extends TriggerArea {

	MeanderRenderer renderer;
	private String zone;
	
	public ChangeZoneTrigger(BoundingBox area, MeanderRenderer renderer, String zone) {
		this.area = area;
		this.renderer = renderer;
		this.zone = zone;
	}

	@Override
	public void doAction() {
		renderer.changeZone(zone);
	}

}
