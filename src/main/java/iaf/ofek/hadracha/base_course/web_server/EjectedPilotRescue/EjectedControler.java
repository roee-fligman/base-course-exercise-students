package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import iaf.ofek.hadracha.base_course.web_server.Data.InMemoryMapDataBase;

@RestController
@RequestMapping("/ejectedPilotRescue")
public class EjectedControler {

	InMemoryMapDataBase database;
	AirplanesAllocationManager airplanesAllocationManager;

	public EjectedControler(@Autowired InMemoryMapDataBase database,
							@Autowired AirplanesAllocationManager airplanesAllocationManager) {
		this.airplanesAllocationManager = airplanesAllocationManager;
		this.database = database;
	}

	@GetMapping("/infos")
	public List<EjectedPilotInfo> SendEjectionToPilot() {
		return database.getAllOfType(EjectedPilotInfo.class);
	}

	@GetMapping("/takeResponsibility")
	public void TakeResponsibility(@RequestParam int ejectionId,
								  @CookieValue(value = "client-id", defaultValue = "") String clientId) {
		EjectedPilotInfo _ejectedPilotInfo = database.getByID(ejectionId, EjectedPilotInfo.class);
		if (_ejectedPilotInfo.rescuedBy == null) _ejectedPilotInfo.rescuedBy = clientId;
		database.update(_ejectedPilotInfo);
		airplanesAllocationManager.allocateAirplanesForEjection(_ejectedPilotInfo, clientId);
	}

}
