package com.mariajuliasales

import com.mariajuliasales.api.CandidateServlet
import com.mariajuliasales.api.EnterpriseServlet
import com.mariajuliasales.api.VacancyServlet
import com.mariajuliasales.controller.Controllers
import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat

static void main(String[] args) {
	Controllers controllers = Application.createControllers(Application.connectToDatabase())

	Tomcat tomcat = new Tomcat()
	tomcat.baseDir = System.getProperty('java.io.tmpdir')
	tomcat.port = 8080
	tomcat.connector

	Context context = tomcat.addContext('', null)

	Tomcat.addServlet(context, 'candidates', new CandidateServlet(controllers.candidates()))
	context.addServletMappingDecoded('/api/candidates', 'candidates')

	Tomcat.addServlet(context, 'enterprises', new EnterpriseServlet(controllers.enterprises()))
	context.addServletMappingDecoded('/api/enterprises', 'enterprises')

	Tomcat.addServlet(context, 'vacancies', new VacancyServlet(controllers.vacancies()))
	context.addServletMappingDecoded('/api/vacancies', 'vacancies')

	tomcat.start()
	println 'API do Linketinder rodando em http://localhost:8080/api'
	tomcat.server.await()
}
