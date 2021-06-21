package it.polimi.tiw.missions.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.missions.beans.Mission;
import it.polimi.tiw.missions.beans.User;
import it.polimi.tiw.missions.dao.MissionsDAO;
import it.polimi.tiw.missions.utils.ConnectionHandler;

@WebServlet("/GetMissionsData")
public class GetMissionsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetMissionsData() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		MissionsDAO missionsDAO = new MissionsDAO(connection);
		List<Mission> missions = new ArrayList<Mission>();

		try {
			missions = missionsDAO.findMissionsByUser(user.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover missions");
			return;
		}

		// Redirect to the Home page and add missions to the parameters
		
		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(missions);
		
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
