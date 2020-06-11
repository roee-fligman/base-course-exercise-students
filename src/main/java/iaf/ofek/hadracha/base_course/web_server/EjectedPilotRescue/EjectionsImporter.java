package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.Data.CrudDataBase;
import iaf.ofek.hadracha.base_course.web_server.Data.Entity;
import iaf.ofek.hadracha.base_course.web_server.Utilities.ListOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EjectionsImporter {

	@Value("${ejections.server.url}")
	public String EJECTION_SERVER_URL;

	@Value("${ejections.namespace}")
	public String NAMESPACE;

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final RestTemplate restTemplate;
	private final CrudDataBase dataBase;
	private final ListOperations listOperations;
	private static final Double SHIFT_NORTH = 1.7;

	public EjectionsImporter(RestTemplateBuilder restTemplateBuilder, CrudDataBase dataBase,
			ListOperations listOperations, @Autowired AirplanesAllocationManager airplanesAllocationManager) {
		restTemplate = restTemplateBuilder.build();
		this.dataBase = dataBase;
		this.listOperations = listOperations;
		executor.scheduleAtFixedRate(this::updateEjections, 1, 1, TimeUnit.SECONDS);
	}

	private void updateEjections() {
		try {
			List<EjectedPilotInfo> ejectionsFromServer = getResponseEntity().getBody();
			shiftEjectedPilotLatitude(ejectionsFromServer);
			List<EjectedPilotInfo> updatedEjections = ejectionsFromServer;
			List<EjectedPilotInfo> previousEjections = dataBase.getAllOfType(EjectedPilotInfo.class);
			executeUpdateEjections(updatedEjections, previousEjections);
		} catch (RestClientException e) {
			System.err.println("Could not get ejections: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void executeUpdateEjections(List<EjectedPilotInfo> updatedEjections, List<EjectedPilotInfo> previousEjections) {
		List<EjectedPilotInfo> addedEjections = ejectionsToAdd(updatedEjections, previousEjections);
		List<EjectedPilotInfo> removedEjections = ejectionsToRemove(updatedEjections, previousEjections);
		addedEjections.forEach(dataBase::create);
		removedEjections.stream().map(EjectedPilotInfo::getId)
				.forEach(id -> dataBase.delete(id, EjectedPilotInfo.class));
	}

	private void shiftEjectedPilotLatitude(List<EjectedPilotInfo> ejectionsFromServer) {
		if (ejectionsFromServer != null) {
			for (EjectedPilotInfo ejectedPilotInfo : ejectionsFromServer) {
				ejectedPilotInfo.getCoordinates().lat += SHIFT_NORTH;
			}
		}
	}

	private ResponseEntity<List<EjectedPilotInfo>> getResponseEntity() {
		return restTemplate.exchange(EJECTION_SERVER_URL + "/ejections?name=" + NAMESPACE, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<EjectedPilotInfo>>() {
				});
	}

	private List<EjectedPilotInfo> ejectionsToRemove(List<EjectedPilotInfo> updatedEjections,
			List<EjectedPilotInfo> previousEjections) {
		return listOperations.subtract(previousEjections, updatedEjections, new Entity.ByIdEqualizer<>());
	}

	private List<EjectedPilotInfo> ejectionsToAdd(List<EjectedPilotInfo> updatedEjections,
			List<EjectedPilotInfo> previousEjections) {
		return listOperations.subtract(updatedEjections, previousEjections, new Entity.ByIdEqualizer<>());
	}
}
