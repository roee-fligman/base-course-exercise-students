package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iaf.ofek.hadracha.base_course.web_server.Data.CrudDataBase;


@RestController
@RequestMapping("ejectedPilotRescue")
public class EjectedControler {

    private CrudDataBase dataBase;
	
	public EjectedControler(@Autowired CrudDataBase dataBase) {
		this.dataBase = dataBase;
	}
	
	@GetMapping("/infos")
	public List<EjectedPilotInfo> sendEjectionToClient() {
		return dataBase.getAllOfType(EjectedPilotInfo.class);
	}

}
